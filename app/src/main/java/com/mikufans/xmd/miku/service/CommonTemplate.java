package com.mikufans.xmd.miku.service;

import com.alibaba.fastjson.JSON;
import com.mikufans.xmd.miku.entiry.Anime;
import com.mikufans.xmd.miku.entiry.AnimeDetail;
import com.mikufans.xmd.miku.entiry.Episode;
import com.mikufans.xmd.miku.entiry.PlayInfo;
import com.mikufans.xmd.miku.entiry.PlayerData;
import com.mikufans.xmd.miku.entiry.Schedule;
import com.mikufans.xmd.miku.entiry.Source;
import com.mikufans.xmd.util.HttpUtil;
import com.mikufans.xmd.util.ValidateUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommonTemplate implements HtmlParser {
  private String baseUrl;
  private String blankReg = "[\\s\u3000]+";

  public String getBlankReg() {
    return blankReg;
  }

  public void setBlankReg(String blankReg) {
    this.blankReg = blankReg;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * 获取搜索结果
   *
   * @param searchUrl 搜索页url
   * @param page      页码
   * @param size      大小
   * @return List<Anime>
   */
  @Override
  public List<Anime> getSearchResult(String searchUrl, Integer page, Integer size) throws Exception {
    OkHttpClient client = HttpUtil.getClient();
    Request request = HttpUtil.getRequest(baseUrl + searchUrl);
    List<Anime> animeList = new ArrayList<>();

    try (Response response = client.newCall(request).execute()) {
      String html = ValidateUtil.validateResponse(response);
      if (html == null) {
        return animeList;
      }
      Document document = Jsoup.parse(html);
      Elements elements = document.select("div.public-list-box.search-box");
      for (Element element : elements) {
        String href = element.select("a.public-list-exp").attr("href");
        String cover = element.select("img.gen-movie-img").attr("data-src");
        String status = element.select("span.public-list-prb").text();
        String title = element.select("div.thumb-txt.cor4.hide").text();
        String type = element.select("div.thumb-else a").stream().map(Element::text).collect(Collectors.joining("•"));
        String director = element.select("div.thumb-director a:not(:first-child)").stream().map(Element::text).collect(Collectors.joining("•"));
        String actor = element.select("div.thumb-actor a:not(:first-child)").stream().map(Element::text).collect(Collectors.joining("•"));
        String description = element.select("span.cor5.thumb-blurb").text().replaceAll(blankReg, "");
        Anime anime = new Anime();
        anime.setId(href);
        anime.setName(title);
        anime.setDescription(description);
        anime.setDirector(director);
        anime.setActor(actor);
        anime.setType(type);
        anime.setStatus(status);
        anime.setCoverUrl(cover);
        animeList.add(anime);
      }
      return animeList;
    } catch (IOException e) {
//      log.error("获取搜索结果失败: {}", e.getMessage());
      throw new Exception("获取搜索结果失败", e);
    }
  }

  /**
   * 获取视频详情
   *
   * @param detailUrl 详情页url
   * @return AnimeDetail
   */
  @Override
  public AnimeDetail getAnimeDetail(String detailUrl) throws Exception {
    String fullUrl = baseUrl + detailUrl;
    OkHttpClient client = HttpUtil.getClient();
    Request request = HttpUtil.getRequest(fullUrl);

    try (Response response = client.newCall(request).execute()) {
      String s = ValidateUtil.validateResponse(response);
      if (s == null) {
        return null;
      }
      Document document = Jsoup.parse(s);
      Elements detailDiv = document.select("div.vod-detail.style-detail");
      String coverImg = detailDiv.select("img.lazy.lazy1.mask-1").attr("data-src");
      String title = detailDiv.select("h3.slide-info-name").text();
      String part1 = Optional.ofNullable(detailDiv.select("div.slide-info span.slide-info-remarks").first()).orElse(new Element("empty")).text();
      String part2 = detailDiv.select("div.slide-info a").stream().map(Element::text).collect(Collectors.joining("•"));
      String status = part2 + part1;
      String director = detailDiv.select("div.slide-info").get(1).select("a").stream().map(Element::text).collect(Collectors.joining("•"));
      String actor = detailDiv.select("div.slide-info").get(2).select("a").stream().map(Element::text).collect(Collectors.joining("•"));
      String type = detailDiv.select("a.deployment.none.cor5 span").stream().map(Element::text).collect(Collectors.joining());
      String description = detailDiv.select("div#height_limit").text().replaceAll(blankReg, "");
      Elements listBoxDiv = document.select("div.anthology-list-box.none");
      List<Source> sources = new ArrayList<>();
      for (Element element : listBoxDiv) {
        Source source = new Source();
        List<Episode> episodes = element.select("a").stream().map(item -> new Episode(item.attr("href"), null, item.text(), item.text(), item.attr("href"), null, null)).collect(Collectors.toList());
        source.setEpisodes(episodes);
        sources.add(source);
      }
      AnimeDetail animeDetail = new AnimeDetail();
      animeDetail.setSources(sources);
      Anime anime = new Anime();
      anime.setId(detailUrl);
      anime.setName(title);
      anime.setDescription(description);
      anime.setDirector(director);
      anime.setActor(actor);
      anime.setType(type);
      anime.setStatus(status);
      anime.setCoverUrl(coverImg);
      animeDetail.setAnime(anime);
      return animeDetail;
    } catch (IOException e) {
//            log.error("获取视频详情失败: {}", e.getMessage());
      throw new Exception("获取视频详情失败", e);
    }
  }

  /**
   * 获取剧集对应的视频播放信息
   *
   * @param episodeUrl 剧集页url
   * @return PlayInfo
   */
  @Override
  public PlayInfo getPlayInfo(String episodeUrl) throws Exception {
    String fullUrl = baseUrl + episodeUrl;
    OkHttpClient client = HttpUtil.getClient();
    Request request = HttpUtil.getRequest(fullUrl);

    try (Response response = client.newCall(request).execute()) {
      String html = ValidateUtil.validateResponse(response);
      if (html == null) {
        return null;
      }
      Document doc = Jsoup.parse(html);
      Elements scriptElements = doc.getElementsByTag("script");
      String playerData = null;
      for (Element script : scriptElements) {
        String scriptText = script.data();
        if (scriptText.contains("player_aaaa")) {
          playerData = scriptText;
//                    log.info("找到包含player_aaaa的script标签");
          break;
        }
      }
      String jsonStr = extractJsonFromScript(playerData);
      if (jsonStr == null) {
//                log.error("无法从script中提取JSON数据");
        return null;
      }
      PlayerData player = JSON.parseObject(jsonStr, PlayerData.class);
      PlayInfo playInfo = new PlayInfo();
      String decodeUrl = decodeUrl(player.getUrl());
      String decodeNextUrl = decodeUrl(player.getUrl_next());
      playInfo.setCurrentEpisodeUrl(decodeUrl);
      playInfo.setNextEpisodeUrl(decodeNextUrl);
      return playInfo;
    } catch (IOException e) {
//            log.error("获取播放信息失败: {}", e.getMessage());
      throw new RuntimeException("获取播放信息失败", e);
    }
  }

  @Override
  public String getRecommendations(String html) throws Exception {
    return null;
  }

  /**
   * 获取本周的番剧更新表
   *
   * @return List<Schedule>
   */
  @Override
  public List<Schedule> getWeeklySchedule() throws Exception {
    OkHttpClient client = HttpUtil.getClient();
    Request request = HttpUtil.getRequest(baseUrl);

    try (Response response = client.newCall(request).execute()) {
      String s = ValidateUtil.validateResponse(response);
      if (s == null) {
        return new ArrayList<>();
      }
      List<Schedule> schedules = new ArrayList<>();
      Document document = Jsoup.parse(s);
      for (int i = 0; i < 7; i++) {
        Schedule schedule = new Schedule();
        schedule.setDay(i + 1);
        schedule.setAnime(new ArrayList<>());
        String week = "div#week-module-" + (i + 1);
        Elements listBox = document.select(week + " div.public-list-box");
        for (Element box : listBox) {
//                    Elements tempBox = box.select("div.public-list-div");
          Elements temp = box.select("a.public-list-exp");
          String href = temp.attr("href");
          String title = temp.attr("name");
          String coverImg = box.select("img.lazy.gen-movie-img").attr("data-src");
          Elements status = box.select("div.public-list-subtitle");
          Anime anime = new Anime();
          anime.setId(href);
          anime.setName(title);
          anime.setCoverUrl(coverImg.contains("http") ? coverImg : baseUrl + coverImg);
          anime.setStatus(status.text().replaceAll(blankReg, ""));
          schedule.getAnime().add(anime);
        }
        schedules.add(schedule);
      }
      return schedules;
    } catch (IOException e) {
//            log.error("获取周更表失败: {}", e.getMessage());
      throw new Exception("获取周更表失败", e);
    }
  }


  /**
   * 解码URL（处理base64等编码）
   *
   * @param encodedUrl 编码的URL
   * @return 解码后的URL
   */
  private String decodeUrl(String encodedUrl) {
    if (encodedUrl.isEmpty()) {
      return encodedUrl;
    }
//        log.info("原始URL: {}", encodedUrl);
    String decodedUrl;
    try {
      // 首先尝试Base64解码
      try {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedUrl);
        decodedUrl = new String(decodedBytes, StandardCharsets.UTF_8);
      } catch (Exception base64Exception) {
//                log.error("Base64解码失败，尝试URL解码");
        // 如果Base64解码失败，则尝试URL解码
        try {
          decodedUrl = java.net.URLDecoder.decode(encodedUrl, "UTF-8");

        } catch (Exception urlException) {
//                    log.error("URL解码失败，保留原始URL");
          decodedUrl = encodedUrl;
        }
      }
      // 再次尝试URL解码（以防是双重编码）
      try {
        String doubleDecodedUrl = java.net.URLDecoder.decode(decodedUrl, "UTF-8");
        // 只有当解码后的内容不同时才更新
        if (!doubleDecodedUrl.equals(decodedUrl)) {
          decodedUrl = doubleDecodedUrl;
        }
      } catch (Exception e) {
//                log.error("二次URL解码失败，保留之前的结果");
        // 保持第一次解码的结果
      }
      return decodedUrl;
    } catch (Exception e) {
//            log.error("URL解码过程中发生错误: {}", encodedUrl, e);
      return encodedUrl; // 解码失败时返回原始URL
    }
  }

  /**
   * 从script标签中提取JSON数据
   *
   * @param scriptText script标签内容
   * @return JSON字符串
   */
  private String extractJsonFromScript(String scriptText) {
    if (scriptText == null) {
      return null;
    }
    Pattern pattern = Pattern.compile("var\\s+player_aaaa\\s*=\\s*(\\{.*?\\})\\s*;");
    Matcher matcher = pattern.matcher(scriptText);
    if (matcher.find()) {
      return matcher.group(1);
    }
    // 如果上面的模式没匹配到，尝试另一种模式
    pattern = Pattern.compile("var\\s+player_aaaa\\s*=\\s*(\\{.*?\\})\\s*$");
    matcher = pattern.matcher(scriptText);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }
}
