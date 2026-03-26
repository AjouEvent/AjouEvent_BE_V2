package com.example.ajouevent_be_v2.service.calendar;

import com.example.ajouevent_be_v2.common.exception.auth.AuthErrorCode;
import com.example.ajouevent_be_v2.common.exception.auth.AuthException;
import com.example.ajouevent_be_v2.config.properties.GoogleProperties;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.clubevent.CalendarRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class CalendarCommandService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final GoogleProperties googleProperties;
    private final RestClient restClient;

    public CalendarCommandService(
        ClientRegistrationRepository clientRegistrationRepository,
        GoogleProperties googleProperties) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.googleProperties = googleProperties;
        this.restClient = RestClient.create();
    }

    public void addEvent(Member member, CalendarRequest request) {
        if (member.getGoogleCalendarRefreshToken() == null) {
            throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
        }
        String accessToken = refreshAccessToken(member.getGoogleCalendarRefreshToken());
        postCalendarEvent(member.getEmail(), accessToken, request);
    }

    private String refreshAccessToken(String refreshToken) {
        ClientRegistration registration =
            clientRegistrationRepository.findByRegistrationId(googleProperties.getRegistrationId());

        Map<?, ?> response = restClient.post()
            .uri(googleProperties.getTokenEndpoint())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(buildRefreshTokenBody(
                refreshToken,
                registration.getClientId(),
                registration.getClientSecret()))
            .retrieve()
            .body(Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
        }
        return (String) response.get("access_token");
    }

    private String buildRefreshTokenBody(String refreshToken, String clientId, String clientSecret) {
        return "grant_type=refresh_token"
            + "&refresh_token=" + refreshToken
            + "&client_id=" + clientId
            + "&client_secret=" + clientSecret;
    }

    private void postCalendarEvent(String userEmail, String accessToken, CalendarRequest request) {
        Map<String, Object> event = Map.of(
            "summary", request.summary() != null ? request.summary() : "",
            "description", request.description() != null ? request.description() : "",
            "start", Map.of("dateTime", request.startDate()),
            "end", Map.of("dateTime", request.endDate())
        );

        try {
            restClient.post()
                .uri(googleProperties.getCalendarApiUrl(), userEmail)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity();
            log.info("Google Calendar 일정 추가 완료 - email: {}, summary: {}", userEmail, request.summary());
        } catch (RestClientException e) {
            log.error("Google Calendar 일정 추가 실패 - email: {}", userEmail, e);
            throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
        }
    }
}
