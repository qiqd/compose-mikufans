package com.mikufans.xmd.util;

import com.mikufans.xmd.miku.entiry.WebsiteDelay;
import com.mikufans.xmd.miku.service.CommonTemplate;
import com.mikufans.xmd.miku.service.HtmlParser;
import com.mikufans.xmd.miku.service.impl.AAfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceUtil {
    private static final Map<String, HtmlParser> SOURCES = new HashMap<>();
    private static final List<WebsiteDelay> delays = new ArrayList<>();

    static {
        SOURCES.put("www.aafun.cc", new AAfun());
        SOURCES.put("bgm.girigirilove.com", new CommonTemplate());
    }

    public static void initSources() {
        delays.addAll(HttpUtil.getDomainDelaysConcurrent(SOURCES));

    }

    public static List<WebsiteDelay> getSourceWithDelay() {
        return delays;
    }

    public static void moveDelayToFirst(int index) {
        if (index <= 0 || index >= delays.size()) return;
        WebsiteDelay target = delays.remove(index);
        delays.add(0, target);
    }
}
