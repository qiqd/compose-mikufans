package com.mikufans.xmd.teto.service.impl;

import com.alibaba.fastjson.JSON;
import com.mikufans.xmd.miku.entiry.Anime;
import com.mikufans.xmd.miku.entiry.Schedule;
import com.mikufans.xmd.teto.entity.RequestType;
import com.mikufans.xmd.teto.entity.bangumi.DailySchedule;
import com.mikufans.xmd.teto.entity.bangumi.EpisodeResult;
import com.mikufans.xmd.teto.entity.bangumi.SubjectSearch;
import com.mikufans.xmd.teto.service.DiscoveryService;
import com.mikufans.xmd.util.HttpUtil;
import com.mikufans.xmd.util.ValidateUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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
    public List<Anime> fetchSearchResult(String keyword, Integer page, Integer size) throws IOException {
        String searchUrl = "/v0/search/subjects";
        HashMap<String, Object> body = new HashMap<>();
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("limit", page);
//        params.put("offset", size);
        body.put("keyword", keyword);
        body.put("sort", "rank");
        Map<String, Object> filter = new HashMap<>();
        filter.put("type", Collections.singletonList(2));
        body.put("filter", filter);
        Request request = HttpUtil.getRequest(baseUrl + searchUrl, RequestType.POST, body);

        try (Response response = client.newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            SubjectSearch subjectSearch = JSON.parseObject(s, SubjectSearch.class);
            List<SubjectSearch.Subject> subjects = subjectSearch.getData();
            return subjects.stream().map(item -> {
                Anime anime = new Anime();
                anime.setSubId(item.getId());
                anime.setName(item.getName());
                anime.setNameCn(item.getNameCn());
                anime.setAriDate(item.getDate());
                anime.setDescription(item.getSummary());
                anime.setType(item.getMetaTags().stream().collect(Collectors.joining(",")));
                anime.setTotalEpisodes(item.getEps());
                anime.setCoverUrl(item.getImages().getLarge());
                anime.setType(String.join(",", item.getMetaTags()));
                return anime;
            }).collect(Collectors.toList());
        }
    }


    @Override
    public String fetchDailyRecommend() throws IOException {
        return null;
    }

    /**
     * 每周更新
     *
     * @return List<DailySchedule>
     */
    @Override
    public List<Schedule> fetchWeeklyUpdate() throws IOException {
        String url = "/calendar";
        Request request = HttpUtil.getRequest(baseUrl + url);
        try (Response response = client.newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            List<DailySchedule> dailySchedules = JSON.parseArray(s, DailySchedule.class);

            return dailySchedules.stream().map(item -> {
                Schedule schedule = new Schedule();
                List<Anime> animeList = item.getItems().stream().map(i -> {
                    Anime anime = new Anime();
                    anime.setSubId(i.getId());
                    anime.setAriDate(i.getAir_date());
                    anime.setName(i.getName());
                    anime.setNameCn(i.getName_cn());
                    anime.setDescription(i.getSummary());
                    anime.setAriDate(i.getAir_date());
                    // 添加空值检查
                    if (i.getImages() != null) {
                        String large = i.getImages().getLarge();
                        anime.setCoverUrl(large != null ? large : i.getImages().getCommon());
                    } else {
                        anime.setCoverUrl(null); // 或设置默认图片链接
                    }
                    return anime;
                }).collect(Collectors.toList());
                schedule.setDay(item.getWeekday().getId());
                schedule.setAnime(animeList);
                return schedule;
            }).collect(Collectors.toList());
        }
    }

    /**
     * 获取剧集
     *
     * @param subjectId 条目id
     * @return EpisodeResult
     */
    @Override
    public EpisodeResult fetchEpisode(String subjectId) throws IOException {
        String url = "/v0/episodes";
        HashMap<String, Object> params = new HashMap<>();
        params.put("subject_id", subjectId);
        Request request = HttpUtil.getRequest(baseUrl + url, RequestType.GET, params);

        try (Response response = client.newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            return JSON.parseObject(s, EpisodeResult.class);
        }
    }

    /**
     * 获取条目详情
     *
     * @param subjectId 条目id
     * @return Anime
     */
    @Override
    public Anime fetchSubject(Integer subjectId) throws IOException {
        String url = "/v0/subjects/" + subjectId;
        Request request = HttpUtil.getRequest(baseUrl + url);
        try (Response response = HttpUtil.getClient().newCall(request).execute()) {
            String s = ValidateUtil.validateResponse(response);
            SubjectSearch.Subject subject = JSON.parseObject(s, SubjectSearch.Subject.class);
            Anime anime = new Anime();
            anime.setSubId(subject.getId());
            anime.setName(subject.getName());
            anime.setNameCn(subject.getNameCn());
            anime.setPlatform(subject.getPlatform());
            anime.setTotalEpisodes(subject.getEps());
            anime.setDescription(subject.getSummary());
            if (subject.getMetaTags() != null) {
                anime.setType(String.join(",", subject.getMetaTags()));
            }
            anime.setTotalEpisodes(subject.getEps());
            if (subject.getImages() != null) {
                anime.setCoverUrl(subject.getImages().getLarge());
            }
            return anime;
        }
    }
}
