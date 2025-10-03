package com.marimo.server.global.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@RequiredArgsConstructor
@Slf4j
public class DiscordWebhookAdapter {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    private final String webhookUrl;

    public void execute(ExecuteWebhookRequest message, Callback callback) {
        String json;

        try {
            json = OBJECT_MAPPER.writeValueAsString(message);
        } catch (IOException e) {
            log.error("Discord 웹훅 요청 직렬화 실패", e);
            throw new BusinessException(ErrorType.INTERNAL_SERVER_ERROR);
        }

        RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build();

        Call call = OK_HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }
}
