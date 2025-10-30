package com.mikufans.xmd.util;


import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.mikufans.xmd.miku.entiry.WebsiteDelay;
import com.mikufans.xmd.miku.service.HtmlParser;
import com.mikufans.xmd.teto.entity.RequestType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpUtil {
    public static Context applicationContext;

    public static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .writeTimeout(100, TimeUnit.SECONDS).readTimeout(100, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Request newRequest = request.newBuilder()
//                            .addHeader("token", Optional.ofNullable(LocalStorage.INSTANCE.get(applicationContext, "token", String.class)).orElse(""))
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8").addHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7").build();
                    return chain.proceed(newRequest);
                })
                .build();
    }

    public static Request getRequest(String url) {
        Request.Builder builder = new Request.Builder();
//        configHeader(builder);
        builder.url(url);
        return builder.build();
    }

    public static Request getRequest(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        headers.forEach(builder::addHeader);
//        configHeader(builder);
        builder.url(url);
        return builder.build();
    }

    public static Request getRequest(String url, RequestType type, Map<String, Object> bodyOrParams) {
        Request.Builder builder = new Request.Builder();
//        configHeader(builder);/
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
//        configHeader(builder);
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
    public static List<WebsiteDelay> getDomainDelaysConcurrent(Map<String, HtmlParser> domains) {
        if (domains == null || domains.isEmpty()) {
            return new ArrayList<>();
        }

        final List<WebsiteDelay> result = new CopyOnWriteArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(Math.min(domains.size(), 10))) {
            List<CompletableFuture<Void>> futures = domains.entrySet().stream()
                    .map(domain -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String host = domain.getKey();
                            HtmlParser parser = domain.getValue();
                            int delay = pingWithDelay(host);
                            if (delay > 0) {
                                return new WebsiteDelay(parser, delay);
                            }
                            return null;
                        } catch (Exception e) {
                            return null;
                        }
                    }, executor))
                    .map(future -> future.thenAccept((websiteDelay) -> {
                        if (websiteDelay != null) {
                            result.add(websiteDelay);
                        }
                    }))
                    .collect(Collectors.toList());

            // 不调用 join()，让任务异步执行
            return result;
        }
    }

    /**
     * 使用Ping命令获取延迟
     *
     * @param host 目标主机
     * @return 延迟时间（毫秒），如果失败返回-1
     */
    public static int pingWithDelay(String host) {
        try {
            String url = "https://" + host;
            Request request = getRequest(url);
            long startTime = System.currentTimeMillis();

            // 使用 OkHttp 发起 GET 请求，并使用 try-with-resources 自动关闭 Response
            OkHttpClient client = getClient();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    long endTime = System.currentTimeMillis();
                    return (int) (endTime - startTime);
                }
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

}


