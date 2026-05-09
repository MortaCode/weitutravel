package com.myy.weitutravel.chat.service.handler;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class RecursiveSplitHandler {

    private static final int TARGET_CHUNK_SIZE = 512;   // 目标token数

    // 分隔符优先级：段落 > 换行 > 句子 > 分句 > 单词
    private final String[][] separators = {
            {"\n\n", "paragraph"},      // 段落分隔符
            {"\n", "line"},             // 换行分隔符
            {"。", "sentence"},         // 句子分隔符(中文)
            {".", "sentence"},          // 句子分隔符(英文)
            {"，", "clause"},           // 分句分隔符(中文)
            {",", "clause"},            // 分句分隔符(英文)
            {" ", "word"}               // 单词分隔符
    };

    /**
     * 递归分割超大块
     * @param documents 待分割的文档列表
     * @return 分割后的文档列表
     */
    public List<Document> recursiveSplit(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        for (Document doc : documents) {
            if (estimateTokenCount(doc.getText()) <= TARGET_CHUNK_SIZE) {
                result.add(doc);
            } else {
                result.addAll(recursiveSplitDocument(doc));
            }
        }
        return result;
    }

    /**
     * 递归分割单个文档
     */
    private List<Document> recursiveSplitDocument(Document document) {

        // 小于目标token大小，直接返回
        if (estimateTokenCount(document.getText()) <= TARGET_CHUNK_SIZE) {
            return Collections.singletonList(document);
        }

        // 根据分隔符分块
        for (String[] separatorInfo : separators) {
            String separator = separatorInfo[0];
            String level = separatorInfo[1];

            if (separator.isEmpty()) continue;

            String[] parts = document.getText().split(Pattern.quote(separator), -1);

            if (parts.length > 1) {
                //合并那些小于目标token的块
                List<Document> splitResult = trySplitWithSeparator(
                        document, parts, separator, level
                );

                if (splitResult != null && !splitResult.isEmpty()) {
                    List<Document> finalResult = new ArrayList<>();
                    for (Document subDoc : splitResult) {
                        // 递归处理每个子块
                        finalResult.addAll(recursiveSplitDocument(subDoc));
                    }
                    return finalResult;
                }
            }
        }

        // 兜底策略：按字符精确切分
        return splitByCharacterExact(document);
    }

    /**
     * 尝试使用指定分隔符拆分文档
     */
    private List<Document> trySplitWithSeparator(Document document, String[] parts,
                                                 String separator, String level) {
        List<Document> result = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int currentTokenCount = 0;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            // 跳过空内容
            if (part.trim().isEmpty() && i < parts.length - 1) {
                if (currentChunk.length() > 0) {
                    currentChunk.append(separator);
                }
                continue;
            }

            int partTokenCount = estimateTokenCount(part);

            // 单个部分本身大于目标大小，需要使用更细的分隔符
            if (partTokenCount > TARGET_CHUNK_SIZE) {
                // 先保存当前累积的块
                if (currentChunk.length() > 0) {
                    result.add(createDocument(document, currentChunk.toString().trim(), level));
                    currentChunk = new StringBuilder();
                    currentTokenCount = 0;
                }
                // 这个大的部分会在外层递归中被处理，这里跳过
                continue;
            }

            // 如果加上当前部分会超过目标大小，保存当前块并开始新块
            if (currentTokenCount + partTokenCount > TARGET_CHUNK_SIZE &&
                    currentChunk.length() > 0) {
                result.add(createDocument(document, currentChunk.toString().trim(), level));
                currentChunk = new StringBuilder();
                currentChunk.append(part);
                currentTokenCount = partTokenCount;
            } else {
                // 添加到当前块
                if (currentChunk.length() > 0) {
                    currentChunk.append(separator);
                }
                currentChunk.append(part);
                currentTokenCount += partTokenCount;
            }
        }

        // 添加最后一个块
        if (currentChunk.length() > 0) {
            result.add(createDocument(document, currentChunk.toString().trim(), level));
        }

        return result;
    }

    /**
     * 按字符精确切分（兜底策略）
     */
    private List<Document> splitByCharacterExact(Document document) {
        List<Document> result = new ArrayList<>();
        String content = document.getText();
        int contentLength = content.length();

        for (int i = 0; i < contentLength; i += TARGET_CHUNK_SIZE) {
            int end = Math.min(i + TARGET_CHUNK_SIZE, contentLength);
            String subContent = content.substring(i, end);

            // 尽量在空格或标点处切分，避免切在单词中间
            if (end < contentLength && end - i == TARGET_CHUNK_SIZE) {
                int breakPoint = findBreakPoint(content, end, Math.max(i, end - 20));
                if (breakPoint > i) {
                    end = breakPoint;
                    subContent = content.substring(i, end);
                }
            }

            result.add(createDocument(document, subContent, "character"));
        }

        return result;
    }

    /**
     * 查找合适的断点位置（避免切在单词中间）
     */
    private int findBreakPoint(String content, int currentEnd, int searchStart) {
        String searchArea = content.substring(searchStart, currentEnd);

        // 优先查找空格
        int lastSpace = searchArea.lastIndexOf(' ');
        if (lastSpace > 0) {
            return searchStart + lastSpace + 1;
        }

        // 查找标点符号
        String punctuations = "。，；：！？、.,;:!?";
        for (int i = searchArea.length() - 1; i >= 0; i--) {
            if (punctuations.indexOf(searchArea.charAt(i)) >= 0) {
                return searchStart + i + 1;
            }
        }

        return currentEnd;
    }

    /**
     *  继承原Document的MetaData，并生成新Document对象
     */
    private Document createDocument(Document original, String content, String splitLevel) {
        // 复制原Document的元数据
        Map<String, Object> metadata = new HashMap<>(original.getMetadata());

        // 添加分割相关的元数据
        metadata.put("split_level", splitLevel);
        metadata.put("original_length", original.getText().length());
        metadata.put("chunk_length", content.length());

        // 创建新的Document对象
        Document newDocument = new Document(content, metadata);

        return newDocument;
    }

    /**
     * 估算token数
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) return 0;
        String[] words = text.split("\\s+");
        int englishTokens = words.length;
        int chineseChars = text.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        return englishTokens + chineseChars;
    }
}
