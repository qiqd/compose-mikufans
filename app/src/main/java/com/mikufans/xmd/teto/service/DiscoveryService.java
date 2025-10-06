package com.mikufans.xmd.teto.service;

import com.mikufans.xmd.miku.entiry.Anime;
import com.mikufans.xmd.miku.entiry.Schedule;
import com.mikufans.xmd.teto.entity.bangumi.EpisodeResult;

import java.util.List;


public interface DiscoveryService {
  List<Anime> fetchSearchResult(String keyword, Integer page, Integer size);

  String fetchDailyRecommend();

  List<Schedule> fetchWeeklyUpdate();

  EpisodeResult fetchEpisode(String subjectId);
}
