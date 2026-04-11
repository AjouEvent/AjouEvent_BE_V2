package com.example.ajouevent_be_v2;

import com.example.ajouevent_be_v2.config.properties.FcmProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.ThreadManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FcmInitializer implements InitializingBean {

    private final FcmProperties fcmProperties;
    private final ThreadPoolTaskExecutor fcmDefaultExecutor;

    public FcmInitializer(
            FcmProperties fcmProperties,
            @Qualifier("fcmDefaultExecutor") ThreadPoolTaskExecutor fcmDefaultExecutor) {
        this.fcmProperties = fcmProperties;
        this.fcmDefaultExecutor = fcmDefaultExecutor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(fcmProperties.getCertification()).getInputStream()))
                            .setThreadManager(new ThreadManager() {
                                @Override
                                protected ExecutorService getExecutor(FirebaseApp app) {
                                    return fcmDefaultExecutor.getThreadPoolExecutor();
                                }

                                @Override
                                protected void releaseExecutor(FirebaseApp app, ExecutorService executor) {
                                    // Spring이 lifecycle을 관리하므로 여기서 종료하지 않음
                                }

                                @Override
                                protected ThreadFactory getThreadFactory() {
                                    return Executors.defaultThreadFactory();
                                }
                            })
                            .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
        }
    }
}
