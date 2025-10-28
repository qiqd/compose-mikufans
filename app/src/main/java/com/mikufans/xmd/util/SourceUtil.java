package com.mikufans.xmd.util;

import com.mikufans.xmd.miku.entiry.WebsiteDelay;
import com.mikufans.xmd.miku.service.HtmlParser;
import com.mikufans.xmd.miku.service.impl.Girigirilove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceUtil {
    public static final Map<String, HtmlParser> SOURCES = new HashMap<>();
    private static final List<WebsiteDelay> delays = new ArrayList<>();

    static {
        SOURCES.put("www.aafun.cc", new AAFun());
        SOURCES.put("bgm.girigirilove.com", new Girigirilove());
    }

    public static void initSources() {
        delays.addAll(HttpUtil.getDomainDelaysConcurrent(SOURCES));

    }

    public static List<WebsiteDelay> getSourceWithDelay() {
        return delays;
    }
}
