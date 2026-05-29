package com.example.ajouevent_be_v2.service.auth;

import com.example.ajouevent_be_v2.common.exception.auth.AuthErrorCode;
import com.example.ajouevent_be_v2.common.exception.auth.AuthException;
import com.example.ajouevent_be_v2.config.properties.GoogleProperties;
import com.example.ajouevent_be_v2.dto.auth.GoogleOauthResult;
import com.example.ajouevent_be_v2.dto.auth.GooglePeopleResult;
import com.example.ajouevent_be_v2.dto.auth.GoogleUserInfoResult;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final GoogleProperties googleProperties;

    private final RestClientAuthorizationCodeTokenResponseClient tokenResponseClient =
        new RestClientAuthorizationCodeTokenResponseClient();
    private final DefaultOAuth2UserService userService = new DefaultOAuth2UserService();
    private final RestClient restClient = RestClient.create();

    public GoogleOauthResult getOauthResult(OauthRequest request) {
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
            String accessToken = tokenResponse.getAccessToken().getTokenValue();
            String refreshToken = tokenResponse.getRefreshToken() != null
                ? tokenResponse.getRefreshToken().getTokenValue()
                : null;

            OAuth2User oAuth2User = userService.loadUser(
                new OAuth2UserRequest(clientRegistration, tokenResponse.getAccessToken())
            );

            String department = fetchDepartmentFromPeopleApi(accessToken);

            return new GoogleOauthResult(
                new GoogleUserInfoResult(
                    oAuth2User.getAttribute("email"),
                    oAuth2User.getAttribute("name"),
                    department
                ),
                refreshToken
            );
        } catch (OAuth2AuthorizationException e) {
            log.warn("OAuth2 토큰 교환 실패: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_AUTHORIZATION_CODE);
        }
    }

    private String fetchDepartmentFromPeopleApi(String accessToken) {
        try {
            GooglePeopleResult result = restClient.get()
                .uri(googleProperties.getPeopleApiUrl())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GooglePeopleResult.class);

            List<GooglePeopleResult.Organization> organizations =
                result != null ? result.organizations() : null;

            if (organizations != null && !organizations.isEmpty()) {
                return organizations.get(0).department();
            }
        } catch (Exception e) {
            log.warn("Google People API에서 학과 정보를 가져오지 못했습니다: {}", e.getMessage());
        }
        return null;
    }
}
