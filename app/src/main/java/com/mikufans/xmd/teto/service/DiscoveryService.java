package com.mikufans.xmd.teto.service;

import com.mikufans.xmd.teto.entity.DailySchedule;
import com.mikufans.xmd.teto.entity.EpisodeResult;
import com.mikufans.xmd.teto.entity.SubjectSearch;

import java.util.List;


public interface DiscoveryService {
    SubjectSearch fetchSearchResult(String keyword, Integer page, Integer size);

    String fetchDailyRecommend();

    List<DailySchedule> fetchWeeklyUpdate();

    EpisodeResult fetchEpisode(String subjectId);
}
