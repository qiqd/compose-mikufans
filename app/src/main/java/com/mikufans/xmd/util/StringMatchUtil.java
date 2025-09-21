package com.mikufans.xmd.util;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;

public class StringMatchUtil {

    /**
     * 使用JaroWinkler算法寻找最佳匹配项
     *
     * @param candidates 候选字符串列表
     * @param target     目标字符串
     * @return 最佳匹配的字符串，如果没有匹配项则返回null
     */
    public static String findBestMatchWithJaroWinkler(List<String> candidates, String target) {
        if (candidates == null || candidates.isEmpty() || target == null) {
            return null;
        }

        JaroWinklerSimilarity similarityChecker = new JaroWinklerSimilarity();
        String bestMatch = null;
        double highestSimilarity = -1.0;

        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }

            double similarity = similarityChecker.apply(target, candidate);
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }

    /**
     * 使用Levenshtein算法寻找最佳匹配项
     *
     * @param candidates 候选字符串列表
     * @param target     目标字符串
     * @return 最佳匹配的字符串，如果没有匹配项则返回null
     */
    public static String findBestMatchWithLevenshtein(List<String> candidates, String target) {
        if (candidates == null || candidates.isEmpty() || target == null) {
            return null;
        }

        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        String bestMatch = null;
        int lowestDistance = Integer.MAX_VALUE;

        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }

            // Levenshtein距离越小表示越相似
            int distance = levenshteinDistance.apply(target, candidate);
            if (distance < lowestDistance) {
                lowestDistance = distance;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }

    /**
     * 使用模糊匹配寻找最佳匹配项（综合考虑相似度和距离）
     *
     * @param candidates 候选字符串列表
     * @param target     目标字符串
     * @return 最佳匹配的字符串，如果没有匹配项则返回null
     */
    public static String findBestFuzzyMatch(List<String> candidates, String target) {
        if (candidates == null || candidates.isEmpty() || target == null) {
            return null;
        }

        JaroWinklerSimilarity similarityChecker = new JaroWinklerSimilarity();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        String bestMatch = null;
        double bestScore = -1.0;

        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }

            // 计算Jaro-Winkler相似度（值越大越相似）
            double similarity = similarityChecker.apply(target, candidate);

            // 计算Levenshtein距离（值越小越相似）
            int distance = levenshteinDistance.apply(target, candidate);

            // 标准化距离分数（转换为0-1范围，1表示完全匹配）
            double distanceScore = 1.0 - (double) distance / Math.max(target.length(), candidate.length());

            // 综合评分（可以调整权重）
            double score = 0.7 * similarity + 0.3 * distanceScore;

            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }
}
