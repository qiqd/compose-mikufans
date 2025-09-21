package com.mikufans.xmd.util;


import com.alibaba.fastjson.JSON;
import com.mikufans.xmd.teto.entity.RequestType;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class HttpUtil {
    public static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    private static void configHeader(Request.Builder builder) {
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
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
}
