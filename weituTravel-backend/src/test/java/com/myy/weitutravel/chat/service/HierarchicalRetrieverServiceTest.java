package com.myy.weitutravel.chat.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HierarchicalRetrieverServiceTest {

    @Resource
    HierarchicalRetrieverService hierarchicalRetrieverService;


    @Test
    void addDocuments() {
        hierarchicalRetrieverService.addDocuments();
    }
}