package com.myy.weitutravel.common.config;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class DataSourceConfigTest {


    @Resource
    VectorStore pgVectorVectorStore;

    @Test
    void testClearVectorStore() {
        // 方法1: 删除所有数据（如果PgVectorVectorStore支持）
        pgVectorVectorStore.delete(List.of());  // 或者使用 deleteAll()

        // 方法2: 查询所有文档ID后删除（如果没有deleteAll方法）
        // List<Document> allDocs = pgVectorVectorStore.similaritySearch(
        //     SearchRequest.builder().query("任意关键词").topK(1000).build()
        // );
        // if (!allDocs.isEmpty()) {
        //     List<String> ids = allDocs.stream().map(Document::getId).collect(Collectors.toList());
        //     pgVectorVectorStore.delete(ids);
        // }

        log.info("向量存储数据已清空");
    }

    @Test
    void testAddTravelKnowledge() {
        List<Document> travelDocuments = List.of(
                // 北京景点知识
                new Document("故宫博物院：北京必游景点，明清两代皇家宫殿。开放时间：8:30-17:00（旺季），8:30-16:30（淡季）。门票价格：旺季60元，淡季40元。建议游玩时间：3-4小时。最佳参观路线：午门→太和殿→乾清宫→御花园→神武门。",
                        Map.of("category", "attraction", "city", "北京", "popularity", "high")),

                new Document("长城（八达岭段）：世界文化遗产，明代长城精华段落。开放时间：6:30-19:00（旺季），7:30-18:00（淡季）。门票价格：旺季40元，淡季35元。索道单程100元，往返140元。建议游玩时间：3-5小时。最佳季节：4-10月。",
                        Map.of("category", "attraction", "city", "北京", "type", "historic")),

                new Document("颐和园：中国现存最大的皇家园林。开放时间：6:30-20:00（旺季），7:00-19:00（淡季）。门票价格：旺季30元，淡季20元。联票：旺季60元，淡季50元。推荐景点：万寿山、昆明湖、长廊、佛香阁。",
                        Map.of("category", "attraction", "city", "北京", "severity", "garden")),

                // 上海景点知识
                new Document("上海迪士尼乐园：中国内地首个迪士尼主题公园。开放时间：8:30-21:30。门票价格：常规日435元，特别高峰日769元。推荐项目：创极速光轮、飞跃地平线、加勒比海盗。建议提前下载官方APP查看排队时间。",
                        Map.of("category", "attraction", "city", "上海", "type", "themePark", "popularity", "veryHigh")),

                new Document("外滩：上海的标志性景观，黄浦江畔。最佳游览时间：晚上19:00-22:00，夜景最美。可观赏陆家嘴金融区摩天大楼群。免费开放。建议游览路线：外滩→南京路步行街→人民广场。",
                        Map.of("category", "attraction", "city", "上海", "type", "landmark", "price", "free")),

                new Document("东方明珠塔：上海地标建筑，高468米。开放时间：9:00-21:00。门票价格：观光层199元，旋转餐厅自助餐328元起。最佳观景时间：傍晚，可以看日落+夜景。",
                        Map.of("category", "attraction", "city", "上海", "type", "observation")),

                // 杭州景点知识
                new Document("西湖：杭州最著名的景点，免费开放。推荐路线：断桥残雪→白堤→平湖秋月→苏堤→花港观鱼→雷峰塔。最佳季节：3-5月（春季赏花），9-11月（秋季赏桂）。可租自行车环湖游览约2-3小时。",
                        Map.of("category", "attraction", "city", "杭州", "type", "lake", "price", "free")),

                new Document("灵隐寺：杭州著名佛教寺庙，千年古刹。开放时间：7:00-18:00。门票价格：飞来峰景区45元，灵隐寺香花券30元。建议早上去，避开人流高峰。注意着装得体，保持安静。",
                        Map.of("category", "attraction", "city", "杭州", "type", "temple", "culture", "high"))

//                // 旅行攻略知识
//                new Document("北京三日游攻略：第一天：天安门广场→故宫→景山公园→王府井；第二天：八达岭长城→鸟巢→水立方（夜景）；第三天：颐和园→圆明园→清华/北大（外观）。住宿建议：前门/王府井附近，交通便利。美食推荐：烤鸭、炸酱面、豆汁焦圈。",
//                        Map.of("category", "guide", "city", "北京", "days", "3", "type", "itinerary")),
//
//                new Document("上海两日游攻略：第一天：南京路步行街→外滩→豫园→城隍庙；第二天：上海迪士尼乐园（全天）。交通建议：地铁是最佳方式，下载Metro大都会APP。美食推荐：小笼包、生煎包、本帮菜。",
//                        Map.of("category", "guide", "city", "上海", "days", "2", "type", "itinerary")),
//
//                // 优惠券/省钱技巧
//                new Document("景区门票省钱技巧：1. 提前3-7天在平台预订，通常有早鸟优惠；2. 关注平台秒杀活动，可领取5-20元优惠券；3. 购买联票比分次购买便宜15%-25%；4. 淡季出行，门票价格低30%-50%。",
//                        Map.of("category", "tips", "type", "discount", "applicable", "all")),
//
//                new Document("旅游优惠券使用规则：每人每单限用1张优惠券，不可叠加。秒杀优惠券需在1小时内下单支付，否则自动失效。满减券需达到指定金额（如满200减30）。特殊节假日优惠券力度更大。",
//                        Map.of("category", "tips", "type", "coupon", "rule", "important")),
//
//                // 天气/季节建议
//                new Document("北京最佳旅行季节：秋季（9-10月）天气凉爽，红叶最美；春季（4-5月）花开，但风沙较大；夏季（6-8月）炎热多雨；冬季（11-2月）寒冷干燥，但人少价低。",
//                        Map.of("category", "weather", "city", "北京", "type", "season")),
//
//                new Document("杭州旅行注意事项：夏季炎热潮湿，需防晒防蚊；梅雨季（6-7月）多雨，备雨具；春秋季最舒适。西湖景区周末人流量大，建议工作日前往。",
//                        Map.of("category", "tips", "city", "杭州", "type", "notice")),
//
//                // 交通攻略
//                new Document("北京交通指南：地铁覆盖主要景点，下载亿通行APP扫码乘车。高峰时段（7:30-9:30，17:30-19:30）拥堵。推荐购买一卡通或使用手机NFC支付。出租车起步价13元。",
//                        Map.of("category", "transport", "city", "北京", "type", "metro")),
//
//                new Document("上海交通攻略：地铁是最便捷方式，1-13号线基本覆盖所有景点。下载大都会APP或使用支付宝乘车码。浦东机场到市区可乘磁悬浮列车（8分钟，50元）或地铁2号线（1小时，8元）。",
//                        Map.of("category", "transport", "city", "上海", "type", "metro"))
        );

        // 批量添加文档到向量库
        pgVectorVectorStore.add(travelDocuments);
        log.info("已添加{}条旅行知识数据", travelDocuments.size());

        // 验证添加成功
        List<Document> verification = pgVectorVectorStore.similaritySearch(
                SearchRequest.builder().query("故宫门票多少钱").topK(3).build()
        );
        log.info("验证查询结果：{}", verification);

        Assertions.assertNotNull(verification);
        Assertions.assertFalse(verification.isEmpty());
    }


}