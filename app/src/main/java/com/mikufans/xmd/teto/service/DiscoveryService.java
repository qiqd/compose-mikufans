package com.mikufans.xmd.teto.service;

import com.mikufans.xmd.teto.entity.bangumi.DailySchedule;
import com.mikufans.xmd.teto.entity.bangumi.EpisodeResult;
import com.mikufans.xmd.teto.entity.bangumi.SubjectSearch;

import java.util.List;


public interface DiscoveryService {
    SubjectSearch fetchSearchResult(String keyword, Integer page, Integer size);

    String fetchDailyRecommend();

    List<DailySchedule> fetchWeeklyUpdate();

    EpisodeResult fetchEpisode(String subjectId);
}
