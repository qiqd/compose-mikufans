package com.mikufans.xmd.util;


import com.alibaba.fastjson.JSON;
import com.mikufans.xmd.miku.entiry.WebsiteDelay;
import com.mikufans.xmd.teto.entity.RequestType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class HttpUtil {
    public static OkHttpClient getClient() {
        return new OkHttpClient.Builder().connectTimeout(100, TimeUnit.SECONDS).writeTimeout(100, TimeUnit.SECONDS).readTimeout(100, TimeUnit.SECONDS).build();
    }

    private static void configHeader(Request.Builder builder) {
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8").addHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
    }

    public static Request getRequest(String url) {
        Request.Builder builder = new Request.Builder();
        configHeader(builder);
        builder.url(url);
        return builder.build();
    }

    public static Request getRequest(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        headers.forEach(builder::addHeader);
        configHeader(builder);
        builder.url(url);
        return builder.build();
    }

    public static Request getRequest(String url, RequestType type, Map<String, Object> bodyOrParams) {
        Request.Builder builder = new Request.Builder();
        configHeader(builder);
        builder.url(url);
        if (Objects.equals(type, RequestType.GET)) {
            String fullUrl = bodyOrParams.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
            builder.url(url + "?" + fullUrl);
        } else if (Objects.equals(type, RequestType.POST)) {
            builder.url(url).post(RequestBody.create(JSON.toJSONString(bodyOrParams), MediaType.parse("application/json")));
        }
        return builder.build();
    }

    public static Request getRequest(String url, Map<String, Object> body, Map<String, Object> params) {
        Request.Builder builder = new Request.Builder();
        configHeader(builder);
        String fullUrl = params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
        builder.url(url + "?" + fullUrl);
        builder.post(RequestBody.create(JSON.toJSONString(body), MediaType.parse("application/json")));
        return builder.build();
    }

    /**
     * 并发获取各域名的延迟列表
     *
     * @param domains 域名列表
     * @return 包含域名和延迟的Map列表
     */
    public static List<WebsiteDelay> getDomainDelaysConcurrent(List<String> domains) {
        if (domains == null || domains.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        // 创建线程池
        try (ExecutorService executor = Executors.newFixedThreadPool(Math.min(domains.size(), 10))) {

            try {
                // 并发执行网络探测
                List<CompletableFuture<WebsiteDelay>> futures = domains.stream().map(domain -> CompletableFuture.supplyAsync(() -> {
                    try {
                        long delay = networkProbe(domain);
                        return new WebsiteDelay(domain, delay);
                    } catch (Exception e) {
                        return null;
                    }
                }, executor)).collect(Collectors.toList());

                // 等待所有任务完成并收集结果
                return futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).filter(websiteDelay -> websiteDelay.getDelay() > 0).collect(Collectors.toList());
            } finally {
                executor.shutdown();
            }
        }
    }

    /**
     * 使用OkHttp进行网络探测并返回延迟时间（毫秒）
     *
     * @param domain 要探测的域名
     * @return 延迟时间（毫秒），如果失败返回-1
     */
    private static long networkProbe(String domain) {
        OkHttpClient client = HttpUtil.getClient();
        Request request = HttpUtil.getRequest("https://" + domain);

        long startTime = System.currentTimeMillis();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return System.currentTimeMillis() - startTime;
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

}

