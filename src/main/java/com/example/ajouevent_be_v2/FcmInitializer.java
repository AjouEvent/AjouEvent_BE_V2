package com.example.ajouevent_be_v2;

import com.example.ajouevent_be_v2.config.properties.FcmProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmInitializer implements InitializingBean {

    private final FcmProperties fcmProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            FirebaseOptions options =
                    FirebaseOptions.builder()
                            .setCredentials(
                                    GoogleCredentials.fromStream(
                                            new ClassPathResource(fcmProperties.getCertification())
                                                    .getInputStream()))
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
