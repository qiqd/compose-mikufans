package com.mikufans.xmd.miku.service.impl;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.mikufans.xmd.miku.entiry.Anime;
import com.mikufans.xmd.miku.entiry.AnimeDetail;
import com.mikufans.xmd.miku.entiry.Episode;
import com.mikufans.xmd.miku.entiry.PlayInfo;
import com.mikufans.xmd.miku.entiry.PlayerData;
import com.mikufans.xmd.miku.entiry.Schedule;
import com.mikufans.xmd.miku.entiry.Source;
import com.mikufans.xmd.miku.service.HtmlParser;
import com.mikufans.xmd.util.HttpUtil;
import com.mikufans.xmd.util.ValidateUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class AAFun implements HtmlParser {
    final static String baseUrl = "https://www.aafun.cc";


    @Override
    public List<Anime> getSearchResult(String keyword, Integer page, Integer size) {
        String searchUrl = "/feng-s.html?wd=" + keyword + "&submit=";
        // 执行网络请求并返回结果
        OkHttpClient client = HttpUtil.getClient();
        Request request = HttpUtil.getRequest(baseUrl + searchUrl);
        try (Response response = client.newCall(request).execute()) {
            String body = ValidateUtil.validateResponse(response);
            Document document = Jsoup.parse(body);
            Elements elements = document.select("div.hl-list-wrap");
            Elements li = elements.select("li.hl-list-item");
            return li.stream().map(item -> {
                Elements a = item.select("div.hl-item-div a");
                String status = item.select("span.hl-lc-1.remarks").text();
                Anime anime = new Anime();
                anime.setId(a.attr("href"));
                anime.setTitle(a.attr("title"));
                anime.setCoverUrl(a.attr("data-original"));
                anime.setStatus(status);
                return anime;
            }).collect(Collectors.toList());

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return Collections.emptyList();
//            throw new RuntimeException("解析搜索结果错误", e);
        }
    }

    @Override
    public AnimeDetail getVideoDetail(String videoId) {
        Request request = HttpUtil.getRequest(baseUrl + videoId);
        try (Response response = HttpUtil.getClient().newCall(request).execute()) {
            String body = ValidateUtil.validateResponse(response);
            AnimeDetail animeDetail = new AnimeDetail();
            Document document = Jsoup.parse(body);
            Elements div = document.select("div.hl-tabs-box");
            List<Source> sources = div.stream().map(item -> {
                Source source = new Source();
                List<Episode> episodes = item.select("li.hl-col-xs-4 a").stream().map(a -> {
                    Episode episode = new Episode();
                    episode.setId(a.attr("href"));
                    episode.setTitle(a.text());
                    episode.setPlayUrl(a.attr("href"));
                    return episode;
                }).collect(Collectors.toList());
                source.setEpisodes(episodes);
                return source;
            }).collect(Collectors.toList());
            //TODO 补充动漫详细信息

            Anime anime = new Anime();

            // 解析封面图片
            Elements picElements = document.select(".hl-dc-pic .hl-item-thumb");
            if (!picElements.isEmpty()) {
                Element picElement = picElements.first();
                String coverUrl = picElement.attr("data-original");
                anime.setCoverUrl(coverUrl);
            }

            // 解析标题
            Elements titleElements = document.select(".hl-dc-title");
            if (!titleElements.isEmpty()) {
                anime.setTitle(titleElements.first().text());
            }

            // 解析状态
            Elements statusElements = document.select(".hl-vod-data .hl-col-xs-12 span.hl-text-conch");
            if (!statusElements.isEmpty()) {
                anime.setStatus(statusElements.first().text());
            }

            // 解析主演
            Elements actorElements = document.select(".hl-vod-data .hl-col-xs-12").eq(2);
            if (!actorElements.isEmpty()) {
                Elements actorLinks = actorElements.select("a");
                StringBuilder actorBuilder = new StringBuilder();
                for (int i = 0; i < actorLinks.size(); i++) {
                    if (i > 0) {
                        actorBuilder.append(" ");
                    }
                    actorBuilder.append(actorLinks.get(i).text());
                }
                anime.setActor(actorBuilder.toString());
            }

            // 解析导演
            Elements directorElements = document.select(".hl-vod-data .hl-col-xs-12").eq(3);
            if (!directorElements.isEmpty()) {
                Elements directorLinks = directorElements.select("a");
                StringBuilder directorBuilder = new StringBuilder();
                for (int i = 0; i < directorLinks.size(); i++) {
                    if (i > 0) {
                        directorBuilder.append(" ");
                    }
                    directorBuilder.append(directorLinks.get(i).text());
                }
                anime.setDirector(directorBuilder.toString());
            }

            // 解析年份
            Elements yearElements = document.select(".hl-vod-data .hl-col-xs-12.hl-col-sm-4");
            if (yearElements.size() > 0) {
                Element yearElement = yearElements.get(0);
                String yearText = yearElement.text();
                // 提取年份数字
                String year = yearText.replaceAll("\\D+", "");
                if (!year.isEmpty()) {
                    try {
                        anime.setYear(Integer.parseInt(year));
                    } catch (NumberFormatException e) {
                        Log.e("解析年份失败: {}", yearText);
                    }
                }
            }

            // 解析类型
            Elements typeElements = document.select(".hl-vod-data .hl-col-xs-12.hl-col-sm-4");
            if (typeElements.size() > 2) {
                Element typeElement = typeElements.get(2);
                Elements typeLinks = typeElement.select("a");
                StringBuilder typeBuilder = new StringBuilder();
                for (int i = 0; i < typeLinks.size(); i++) {
                    if (i > 0) {
                        typeBuilder.append(" ");
                    }
                    typeBuilder.append(typeLinks.get(i).text());
                }
                anime.setType(typeBuilder.toString());
            }

            // 解析上映时间
            Elements releaseElements = document.select(".hl-vod-data .hl-col-xs-12.hl-col-sm-4");
            if (releaseElements.size() > 4) {
                Element releaseElement = releaseElements.get(4);
                String releaseText = releaseElement.text();
                anime.setUpdateTime(releaseText);
            }

//            // 解析语言
//            Elements languageElements = document.select(".hl-vod-data .hl-col-xs-12.hl-col-sm-4");
//            if (languageElements.size() > 5) {
//                Element languageElement = languageElements.get(5);
//                String languageText = languageElement.text();
//                // 去掉"语言："前缀
//                if (languageText.startsWith("语言：")) {
//                    languageText = languageText.substring(3);
//                }
//                anime.setLanguage(languageText);
//            }

            // 解析简介
            Elements descriptionElements = document.select(".hl-vod-data .hl-col-xs-12.blurb");
            if (!descriptionElements.isEmpty()) {
                String description = descriptionElements.first().text();
                // 去掉"简介："前缀
                if (description.startsWith("简介：")) {
                    description = description.substring(3);
                }
                anime.setDescription(description);
            }

            // 解析评分
            Elements ratingElements = document.select(".hl-score-nums span");
            if (!ratingElements.isEmpty()) {
                try {
                    double rating = Double.parseDouble(ratingElements.first().text());
                    anime.setRating(rating);
                } catch (NumberFormatException e) {
                    Log.e("解析评分失败: {}", ratingElements.first().text());
                }
            }

            // 设置ID
//            anime.setId(String.valueOf(animeId));
            // 设置动漫信息到动漫详情对象
            animeDetail.setAnime(anime);
            animeDetail.setSources(sources);
            return animeDetail;
        } catch (IOException e) {
//            log.error("解析详情页错误:{}", e.getMessage());
            throw new RuntimeException("解析详情页错误", e);
        }
    }

    @Override
    public PlayInfo getPlayInfo(String episodeId) {
        Request request = HttpUtil.getRequest(baseUrl + episodeId);
        try (Response response = HttpUtil.getClient().newCall(request).execute()) {
            String body = ValidateUtil.validateResponse(response);
            Document document = Jsoup.parse(body);
            Elements script = document.select("script[type='text/javascript']");
            List<Element> playerScript = script.stream().filter(item -> item.data().contains("var player_aaaa")).collect(Collectors.toList());
            String objectString = playerScript.get(0).data().substring(playerScript.get(0).data().indexOf("{"));
            PlayerData playerData = JSON.parseObject(objectString, PlayerData.class);
            String decodeUrl = URLDecoder.decode(playerData.getUrl(), StandardCharsets.UTF_8.name());

            String fullUrl = baseUrl + "/player/?url=" + decodeUrl;
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Host", "www.aafun.cc");
            headers.put("Referer", "https://www.aafun.cc" + episodeId);
            Request playerRequest = HttpUtil.getRequest(fullUrl, headers);

            try (Response playerResponse = HttpUtil.getClient().newCall(playerRequest).execute()) {
                String playerBody = ValidateUtil.validateResponse(playerResponse);
                Document parse = Jsoup.parse(playerBody);
                Element element = parse.select("script").stream().filter(item -> item.data().contains("const encryptedUrl")).findFirst().orElse(new Element("no"));
                String scriptContent = element.data();
                // 在包含相关逻辑的方法中：
                String encryptedUrl = null;
                String sessionKey = null;
                if (scriptContent.contains("const encryptedUrl")) {
                    // 使用正则表达式提取encryptedUrl的值
                    Pattern encryptedUrlPattern = Pattern.compile("const\\s+encryptedUrl\\s*=\\s*\"([^\"]+)\"");
                    Matcher encryptedUrlMatcher = encryptedUrlPattern.matcher(scriptContent);
                    if (encryptedUrlMatcher.find()) {
                        encryptedUrl = encryptedUrlMatcher.group(1);
                    }
                }
                if (scriptContent.contains("const sessionKey")) {
                    // 使用正则表达式提取sessionKey的值
                    Pattern sessionKeyPattern = Pattern.compile("const\\s+sessionKey\\s*=\\s*\"([^\"]+)\"");
                    Matcher sessionKeyMatcher = sessionKeyPattern.matcher(scriptContent);
                    if (sessionKeyMatcher.find()) {
                        sessionKey = sessionKeyMatcher.group(1);
                    }
                }
                String videoUrl = decryptAES(encryptedUrl, sessionKey);
//          String nextVideoUrl = decryptAES(encryptedUrl, sessionKey);
                String currentUrl = videoUrl.replaceFirst("http://", "https://");
                PlayInfo playInfo = new PlayInfo();
                playInfo.setCurrentEpisodeUrl(currentUrl);
                return playInfo;
            }
        } catch (IOException e) {
//            log.error("解析详情页错误:{}", e.getMessage());
            throw new RuntimeException("解析详情页错误", e);
        }
    }

    @Override
    public String getRecommendations(String html) {
        return null;
    }

    @Override
    public List<Schedule> getWeeklySchedule() {
        Request request = HttpUtil.getRequest(baseUrl);

        try (Response response = HttpUtil.getClient().newCall(request).execute()) {
            String body = ValidateUtil.validateResponse(response);
            Document document = Jsoup.parse(body);
            Elements weekBoxItem = document.select("div.hl-rb-vod.hl-week-item");
            Elements weeklyDiv = weekBoxItem.select("div.row div.hl-list-wrap");
            ArrayList<Schedule> schedules = new ArrayList<>();
            for (int i = 0; i < weeklyDiv.size(); i++) {
                List<Anime> anime = weeklyDiv.get(i).select("li.hl-list-item").stream().map(item -> {
                    Anime temp = new Anime();
                    Elements a = item.select("a.hl-item-thumb ");
                    temp.setId(a.attr("href"));
                    temp.setTitle(a.attr("title"));
                    temp.setCoverUrl(a.attr("data-original"));
                    return temp;
                }).collect(Collectors.toList());
                schedules.add(new Schedule(i, anime));
            }
            return schedules;
        } catch (IOException e) {
//            log.error("获取每日推荐错误:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Java版本的_decryptAES函数
     */
    private String decryptAES(String ciphertext, String key) {
        try {
            // 将密文从Base64解码
            byte[] rawBytes = Base64.getDecoder().decode(ciphertext);
            // 提取IV（前16字节）
            byte[] ivBytes = Arrays.copyOfRange(rawBytes, 0, 16);
            // 提取加密数据（剩余部分）
            byte[] encryptedBytes = Arrays.copyOfRange(rawBytes, 16, rawBytes.length);
            // 创建AES解密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            // 解密数据
            byte[] plainText = cipher.doFinal(encryptedBytes);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("URL解密失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
