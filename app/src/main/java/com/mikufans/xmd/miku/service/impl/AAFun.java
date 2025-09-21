package com.mikufans.xmd.miku.service.impl;

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
            Elements li = document.select("li.hl-list-item");
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
//            log.error("解析搜索结果错误:{}", e.getMessage());
            throw new RuntimeException("解析搜索结果错误", e);
        }
    }

    @Override
    public AnimeDetail getVideoDetail(String videoId) {
        Request request = HttpUtil.getRequest(baseUrl + videoId);
        try (Response response = HttpUtil.getClient().newCall(request).execute()) {
            String body = ValidateUtil.validateResponse(response);
            AnimeDetail animeDetail = new AnimeDetail();
            Document document = Jsoup.parse(body);
            Elements div = document.select("div.hl-tabs-box ");
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
