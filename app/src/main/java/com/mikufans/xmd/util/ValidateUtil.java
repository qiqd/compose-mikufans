package com.mikufans.xmd.util;

import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

@Slf4j
public class ValidateUtil {
    public static String validateResponse(@NotNull Response response) throws IOException {
        if (!response.isSuccessful() || response.body() == null) {
//            log.error("Network request failed with status code: {}", response.code());
            throw new RuntimeException("Network request failed");
        }
        String body = response.body().string();
        Pattern pattern = Pattern.compile("\\\\u[0-9a-fA-F]{4}");
        boolean b = pattern.matcher(body).find();
        return b ? StringEscapeUtils.unescapeJava(body) : body;
    }

}
