package com.mikufans.xmd.miku.service;


import com.mikufans.xmd.miku.entiry.Anime;
import com.mikufans.xmd.miku.entiry.AnimeDetail;
import com.mikufans.xmd.miku.entiry.PlayInfo;
import com.mikufans.xmd.miku.entiry.Schedule;

import java.util.List;

/**
 * HTML解析接口规范
 */
public interface HtmlParser {

    /**
     * 解析视频搜索结果
     *
     * @param keyword 搜索关键词
     * @return List<Anime>
     * @throws Exception 解析异常
     */
    List<Anime> getSearchResult(String keyword, Integer page, Integer size) throws Exception;

    /**
     * 解析视频详情信息
     *
     * @param videoId 视频ID
     * @return AnimeDetail
     * @throws Exception 解析异常
     */
    AnimeDetail getVideoDetail(String videoId) throws Exception;

    /**
     * 解析播放信息
     *
     * @param episodeId 剧集id
     * @return PlayInfo
     * @throws Exception 解析异常
     */
    PlayInfo getPlayInfo(String episodeId) throws Exception;

    /**
     * 解析推荐视频
     *
     * @param html HTML内容
     * @return 推荐视频列表
     * @throws Exception 解析异常
     */
    String getRecommendations(String html) throws Exception;

    /**
     * 解析周更表
     *
     * @return 周更表
     * @throws Exception 解析异常
     */
    List<Schedule> getWeeklySchedule() throws Exception;
}
