package com.mikufans.xmd.teto.service.impl;

import com.alibaba.fastjson.JSON;
import com.mikufans.xmd.teto.entity.DailySchedule;
import com.mikufans.xmd.teto.entity.EpisodeResult;
import com.mikufans.xmd.teto.entity.RequestType;
import com.mikufans.xmd.teto.entity.SubjectSearch;
import com.mikufans.xmd.teto.service.DiscoveryService;
import com.mikufans.xmd.util.HttpUtil;
import com.mikufans.xmd.util.ValidateUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@Slf4j
public class RedDrillBit implements DiscoveryService {
    private static final String baseUrl = "https://api.bgm.tv";
    public OkHttpClient client = HttpUtil.getClient();

    /**
     * 搜索
     *
     * @param keyword 关键字
     * @param page    分页
     * @param size    分页大小
     * @return SubjectSearch
     */
    @Override
    public SubjectSearch fetchSearchResult(String keyword, Integer page, Integer size) {
        String searchUrl = "/v0/search/subjects";
        HashMap<String, Object> body = new HashMap<>();
        HashMap<String, Object> params = new HashMap<>();
        params.put("limit", page);
        params.put("offset", size);
        body.put("keyword", keyword);
        body.put("sort", "rank");
        Map<String, Object> filter = new HashMap<>();
        filter.put("type", Collections.singletonList(2));
        body.put("filter", filter);
        Request request = HttpUtil.getRequest(baseUrl + searchUrl, RequestType.POST, body);

        try (Response response = client.newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            return JSON.parseObject(s, SubjectSearch.class);
        } catch (IOException e) {
//            log.error("搜索结果获取失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Override
    public String fetchDailyRecommend() {
        return null;
    }

    /**
     * 每周更新
     *
     * @return List<DailySchedule>
     */
    @Override
    public List<DailySchedule> fetchWeeklyUpdate() {
        String url = "/calendar";
        Request request = HttpUtil.getRequest(baseUrl + url);

        try (Response response = client.newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            return JSON.parseArray(s, DailySchedule.class);
        } catch (IOException e) {
//            log.error("每周更新获取失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取剧集
     *
     * @param subjectId 条目id
     * @return EpisodeResult
     */
    @Override
    public EpisodeResult fetchEpisode(String subjectId) {
        String url = "/v0/episodes";
        HashMap<String, Object> params = new HashMap<>();
        params.put("subject_id", subjectId);
        Request request = HttpUtil.getRequest(baseUrl + url, RequestType.GET, params);

        try (Response response = client.newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            return JSON.parseObject(s, EpisodeResult.class);
        } catch (IOException e) {
//            log.error("剧集信息获取失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public SubjectSearch.Subject fetchSubject(Integer subjectId) {
        String url = "/v0/subjects/" + subjectId;
        Request request = HttpUtil.getRequest(baseUrl + url);
        try (Response response = HttpUtil.getClient().newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            return JSON.parseObject(s, SubjectSearch.Subject.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
