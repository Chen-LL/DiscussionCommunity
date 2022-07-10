package com.kuney.community.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤器
 *
 * @author kuneychen
 * @since 2022/6/13 14:36
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    private static final String REPLACEMENT = "***";

    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            String word;
            while ((word = bufferedReader.readLine()) != null) {
                root.insert(word);
            }
        } catch (IOException e) {
            log.error("读取敏感词文件失败：{}", e.getMessage());
        }
    }

    public String filter(String text) {
        StringBuilder result = new StringBuilder();
        TrieNode node = root;
        int begin = 0, end = 0;
        while (end < text.length()) {
            char c = text.charAt(end);
            // 跳过字符
            if (isSymbol(c)) {
                if (node == root) {
                    begin++;
                    result.append(c);
                }
                end++;
                continue;
            }
            node = node.next.get(c);
            if (node == null) {
                result.append(text.charAt(begin));
                end = ++begin;
                node = root;
            } else {
                if (node.end) {
                    result.append(REPLACEMENT);
                    begin = ++end;
                    node = root;
                } else {
                    end++;
                }
            }
        }
        result.append(text.substring(begin));
        return result.toString();
    }

    /**
     * 判断字符是否为符号
     *
     * @param c
     * @return
     */
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode {
        Map<Character, TrieNode> next;
        boolean end;

        public TrieNode() {
            next = new HashMap<>();
        }

        public void insert(String word) {
            TrieNode node = this;
            for (char c : word.toCharArray()) {
                if (!node.next.containsKey(c)) {
                    node.next.put(c, new TrieNode());
                }
                node = node.next.get(c);
            }
            node.end = true;
        }
    }
}
