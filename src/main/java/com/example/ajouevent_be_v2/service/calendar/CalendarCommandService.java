package com.example.ajouevent_be_v2.service.calendar;

import com.example.ajouevent_be_v2.common.exception.auth.AuthErrorCode;
import com.example.ajouevent_be_v2.common.exception.auth.AuthException;
import com.example.ajouevent_be_v2.config.properties.GoogleProperties;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.clubevent.CalendarRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarCommandService {

    private static final String TOKENS_DIR = "tokens";

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final GoogleProperties googleProperties;
    private final RestClientAuthorizationCodeTokenResponseClient tokenResponseClient =
        new RestClientAuthorizationCodeTokenResponseClient();
    private final RestClient restClient = RestClient.create();

    public void connect(OauthRequest request, String email) {
        ClientRegistration clientRegistration =
            clientRegistrationRepository.findByRegistrationId(googleProperties.getRegistrationId());

        String decodedCode = URLDecoder.decode(request.authorizationCode(), StandardCharsets.UTF_8);

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest
            .authorizationCode()
            .clientId(clientRegistration.getClientId())
            .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
            .redirectUri(request.redirectUri())
            .scopes(clientRegistration.getScopes())
            .state("state")
            .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse
            .success(decodedCode)
            .redirectUri(request.redirectUri())
            .state("state")
            .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
            clientRegistration,
            new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
        );

        try {
            var tokenResponse = tokenResponseClient.getTokenResponse(grantRequest);
            if (tokenResponse.getRefreshToken() == null) {
                throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
            }
            saveRefreshToken(email, tokenResponse.getRefreshToken().getTokenValue());
            log.info("Google 캘린더 연동 완료 - email: {}", email);
        } catch (OAuth2AuthorizationException e) {
            log.warn("캘린더 OAuth2 토큰 교환 실패: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_AUTHORIZATION_CODE);
        }
    }

    public void addEvent(String email, CalendarRequest request) {
        String refreshToken = loadRefreshToken(email);
        String accessToken = refreshAccessToken(refreshToken);
        postCalendarEvent(email, accessToken, request);
    }

    private void saveRefreshToken(String email, String refreshToken) {
        try {
            Path dir = Paths.get(TOKENS_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path tokenFile = dir.resolve(sanitize(email));
            Files.writeString(tokenFile, refreshToken);
        } catch (IOException e) {
            log.error("캘린더 refresh token 저장 실패 - email: {}", email, e);
            throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
        }
    }

    private String loadRefreshToken(String email) {
        Path tokenFile = Paths.get(TOKENS_DIR, sanitize(email));
        if (!Files.exists(tokenFile)) {
            throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
        }
        try {
            return Files.readString(tokenFile).strip();
        } catch (IOException e) {
            log.error("캘린더 refresh token 읽기 실패 - email: {}", email, e);
            throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
        }
    }

    private String sanitize(String email) {
        return email.replaceAll("[^a-zA-Z0-9_-]", "_");
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

    private void postCalendarEvent(String email, String accessToken, CalendarRequest request) {
        Map<String, Object> event = Map.of(
            "summary", request.summary() != null ? request.summary() : "",
            "description", request.description() != null ? request.description() : "",
            "start", Map.of("dateTime", request.startDate()),
            "end", Map.of("dateTime", request.endDate())
        );

        try {
            restClient.post()
                .uri(googleProperties.getCalendarApiUrl(), email)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity();
            log.info("Google Calendar 일정 추가 완료 - email: {}, summary: {}", email, request.summary());
        } catch (RestClientException e) {
            log.error("Google Calendar 일정 추가 실패 - email: {}", email, e);
            throw new AuthException(AuthErrorCode.CALENDAR_NOT_CONNECTED);
        }
    }
}
