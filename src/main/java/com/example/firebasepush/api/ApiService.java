package com.example.firebasepush.api;

import com.example.firebasepush.exception.PushException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.ApnsFcmOptions;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

    private final static String FIREBASE_CONFIG_PATH = "/fcm/파일명.json";

    /**
     * Firebase 초기화
     *
     * @return
     * @throws IOException
     */
    private static void initFirebase() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
            .fromStream(new ClassPathResource(FIREBASE_CONFIG_PATH).getInputStream())
            .createScoped(Arrays.asList(
                "https://www.googleapis.com/auth/firebase",
                "https://www.googleapis.com/auth/cloud-platform",
                "https://www.googleapis.com/auth/firebase.readonly"));
        googleCredentials.refreshAccessToken();

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(googleCredentials)
            .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }

    /**
     * 단일 기기에 푸쉬 전송
     *
     * @param apiDto
     */
    public MessageDto sendOne(ApiDto apiDto) {
        try {
            // 1) Firebase 초기화
            initFirebase();

            // 2) 푸쉬 메세지 생성
            Message message = makeOnePushMessage(apiDto);

            // 3) 푸쉬 발송
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            throw new PushException("푸쉬 발송에 실패했습니다.");
        }

        return MessageDto.builder()
            .message("success")
            .build();
    }

    /**
     * 푸쉬 메세지 생성 - 단일 기기
     *
     * @param apiDto
     * @return
     */
    private Message makeOnePushMessage(ApiDto apiDto) {
        //메세지 설정
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", Long.toString(apiDto.getPushSeq()));
        paramMap.put("title", apiDto.getTitle());
        paramMap.put("content", apiDto.getContent());
        paramMap.put("link", apiDto.getPushLink());
        paramMap.put("badge", "1");

        // IOS 설정
        ApnsConfig apnsConfig = ApnsConfig.builder()
            .putHeader("apns-priority", "5")
            .setAps(Aps.builder()
                .setContentAvailable(true)
                .setMutableContent(true)
                .setSound("default")
                .setBadge(1)
                .setAlert(ApsAlert.builder()
                    .setTitle(apiDto.getTitle())
                    .setBody(apiDto.getContent()).build())
                .build())
            .setFcmOptions(ApnsFcmOptions.builder()
                .build())
            .build();

        // Android 설정
        AndroidConfig androidConfig = AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .build();

        return Message.builder()
            .setNotification(Notification.builder()
                .setTitle(apiDto.getTitle())
                .setBody(apiDto.getContent())
                .build())
            .setToken(apiDto.getPushToken())
            .putAllData(paramMap)
            .setApnsConfig(apnsConfig)
            .setAndroidConfig(androidConfig)
            .build();
    }

    /**
     * 다중 기기에 푸쉬 전송
     *
     * @param targetTokenList
     * @param ApiDto
     * @return
     */
    public MessageDto sendAll(
        List<String> targetTokenList,
        ApiDto ApiDto
    ) {
        try {
            // 1) Firebase 초기화
            initFirebase();

            List<String> tokenList = targetTokenList.stream()
                .filter(Strings::isNotEmpty)
                .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(tokenList)) {
                // 2) 푸쉬 메세지 생성
                MulticastMessage message = makePushMessage(tokenList, ApiDto);

                // 3) 푸쉬 발송
                FirebaseMessaging.getInstance().sendMulticast(message);
            }
        } catch (Exception e) {
            throw new PushException("푸쉬 발송에 실패했습니다.");
        }

        return MessageDto.builder()
            .message("success")
            .build();
    }

    /**
     * 푸쉬 메세지 생성 - 여러 기기
     *
     * @param tokenList - 타겟 토큰 리스트
     * @param ApiDto
     * @return
     */
    private MulticastMessage makePushMessage(
        List<String> tokenList,
        ApiDto ApiDto
    ) {
        // 메세지 설정
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", Long.toString(ApiDto.getPushSeq()));
        paramMap.put("title", ApiDto.getTitle());
        paramMap.put("content", ApiDto.getContent());
        paramMap.put("link", ApiDto.getPushLink());
        paramMap.put("badge", "1");

        // IOS 설정
        ApnsConfig apnsConfig = ApnsConfig.builder()
            .putHeader("apns-priority", "5")
            .setAps(Aps.builder()
                .setContentAvailable(true)
                .setMutableContent(true)
                .setSound("default")
                .setBadge(1)
                .setAlert(ApsAlert.builder()
                    .setTitle(ApiDto.getTitle())
                    .setBody(ApiDto.getContent()).build())
                .build())
            .setFcmOptions(ApnsFcmOptions.builder()
                .build())
            .build();

        // Android 설정
        AndroidConfig androidConfig = AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .build();

        return MulticastMessage.builder()
            .addAllTokens(tokenList)
            .setNotification(Notification.builder()
                .setTitle(ApiDto.getTitle())
                .setBody(ApiDto.getContent())
                .build())
            .putAllData(paramMap)
            .setApnsConfig(apnsConfig)
            .setAndroidConfig(androidConfig)
            .build();
    }
}
