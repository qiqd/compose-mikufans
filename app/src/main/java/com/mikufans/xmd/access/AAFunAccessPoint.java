package com.mikufans.xmd.access;

import com.mikufans.xmd.miku.entiry.Anime;
import com.mikufans.xmd.miku.entiry.AnimeDetail;
import com.mikufans.xmd.miku.entiry.PlayInfo;
import com.mikufans.xmd.miku.entiry.Schedule;
import com.mikufans.xmd.miku.service.impl.AAFun;
import com.mikufans.xmd.teto.entity.DailySchedule;
import com.mikufans.xmd.teto.entity.SubjectSearch;
import com.mikufans.xmd.teto.service.impl.RedDrillBit;
import com.mikufans.xmd.util.StringMatchUtil;

import java.util.List;
import java.util.stream.Collectors;


public class AAFunAccessPoint {
    private Integer subjectId = -1;
    private final AAFun aaFun = new AAFun();
    private final RedDrillBit redDrillBit = new RedDrillBit();

    public SubjectSearch search(String keyword, Integer page, Integer size) {
        return redDrillBit.fetchSearchResult(keyword, page, size);
    }

    public List<Anime> getSearch(String keyword, Integer page, Integer size) {
        return aaFun.getSearchResult(keyword, page, size);
    }

    public List<DailySchedule> weekly() {
        return redDrillBit.fetchWeeklyUpdate();
    }

    public List<Schedule> getSchedule() {
        return aaFun.getWeeklySchedule();
    }

    public AnimeDetail getAnimeInfo(String title, Integer subjectId) {
        this.subjectId = subjectId;
        List<Anime> searchResult = aaFun.getSearchResult(title, 1, 50);
        List<String> titles = searchResult.stream().map(Anime::getTitle).collect(Collectors.toList());
        String bestMatchWithJaroWinkler = StringMatchUtil.findBestMatchWithJaroWinkler(titles, title);
        Anime bestResult = searchResult.stream().filter(anime -> anime.getTitle().equals(bestMatchWithJaroWinkler)).findFirst().orElse(new Anime());
        String animeId = bestResult.getId();
        return aaFun.getVideoDetail(animeId);
    }

    public PlayInfo getVideoUrl(String episodeId) {
        return aaFun.getPlayInfo(episodeId);
    }

}
