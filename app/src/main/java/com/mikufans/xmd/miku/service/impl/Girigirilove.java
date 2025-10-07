package com.mikufans.xmd.miku.service.impl;

import com.mikufans.xmd.miku.entiry.Anime;
import com.mikufans.xmd.miku.entiry.AnimeDetail;
import com.mikufans.xmd.miku.entiry.PlayInfo;
import com.mikufans.xmd.miku.entiry.Schedule;
import com.mikufans.xmd.miku.service.CommonTemplate;
import com.mikufans.xmd.util.HttpUtil;
import com.mikufans.xmd.util.ValidateUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Girigirilove extends CommonTemplate implements Serializable {


    public Girigirilove() {
        super();
        super.setBaseUrl("https://bgm.girigirilove.com");

    }

    public List<Schedule> schedule() throws Exception {
        return super.getWeeklySchedule();
    }


    @Override
    public List<Anime> getSearchResult(String keyword, Integer page, Integer size) throws Exception {
        String searchUrl = "/search/-------------/?wd=" + keyword;
        OkHttpClient client = HttpUtil.getClient();
        Request request = HttpUtil.getRequest(super.getBaseUrl() + searchUrl);
        List<Anime> animeList = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            String html = ValidateUtil.validateResponse(response);
            if (html == null) {
                return animeList;
            }
            Document document = Jsoup.parse(html);
            Elements elements = document.select("div.search-list");
            for (Element element : elements) {
                String href = element.select("div.detail-info a").attr("href");
                String cover = element.select("img.gen-movie-img").attr("data-src");
                String status = element.select("span.public-list-prb").text();
                String title = element.select("img.gen-movie-img").attr("alt");
                String type = element.select("div.thumb-else a").stream().map(Element::text).collect(Collectors.joining("•"));
                StringJoiner joiner = new StringJoiner("•");
                for (Element element1 : element.select("div.thumb-director a:not(:first-child)")) {
                    String text = element1.text();
                    joiner.add(text);
                }
                String director = joiner.toString();
                String actor = element.select("div.thumb-actor a:not(:first-child)").stream().map(Element::text).collect(Collectors.joining("•"));
                String description = element.select("span.cor5.thumb-blurb").text().replaceAll(super.getBlankReg(), "");
                Anime anime = new Anime();
                anime.setId(href);
                anime.setNameCn(title);
                anime.setDescription(description);
                anime.setDirector(director);
                anime.setActor(actor);
                anime.setType(type);
                anime.setStatus(status);
                anime.setCoverUrl(super.getBaseUrl() + cover);
                animeList.add(anime);
            }
            return animeList;
        } catch (IOException e) {
            throw new Exception("获取搜索结果失败", e);
        }
    }

    public AnimeDetail animeDetail(String videoId) throws Exception {
        return super.getAnimeDetail(videoId);
    }

    public PlayInfo playInfo(String episodeId) throws Exception {
        return super.getPlayInfo(episodeId);
    }
}
