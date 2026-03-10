package com.example.ajouevent_be_v2.service.auth;

import com.example.ajouevent_be_v2.dto.auth.GooglePeopleResponse;
import com.example.ajouevent_be_v2.dto.auth.GoogleUserInfoResponse;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class OauthService {

    private static final String GOOGLE_REGISTRATION_ID = "google";
    private static final String GOOGLE_PEOPLE_API_URL =
        "https://people.googleapis.com/v1/people/me?personFields=organizations";

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestClientAuthorizationCodeTokenResponseClient tokenResponseClient;
    private final DefaultOAuth2UserService userService;
    private final RestClient restClient;

    public OauthService(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.tokenResponseClient = new RestClientAuthorizationCodeTokenResponseClient();
        this.userService = new DefaultOAuth2UserService();
        this.restClient = RestClient.create();
    }

    public GoogleUserInfoResponse getUserInfo(OauthRequest request) {
        ClientRegistration clientRegistration =
            clientRegistrationRepository.findByRegistrationId(GOOGLE_REGISTRATION_ID);

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

        var tokenResponse = tokenResponseClient.getTokenResponse(grantRequest);
        String accessToken = tokenResponse.getAccessToken().getTokenValue();

        OAuth2User oAuth2User = userService.loadUser(
            new OAuth2UserRequest(clientRegistration, tokenResponse.getAccessToken())
        );

        String department = fetchDepartmentFromPeopleApi(accessToken);

        return new GoogleUserInfoResponse(
            oAuth2User.getAttribute("email"),
            oAuth2User.getAttribute("name"),
            department
        );
    }

    private String fetchDepartmentFromPeopleApi(String accessToken) {
        try {
            GooglePeopleResponse response = restClient.get()
                .uri(GOOGLE_PEOPLE_API_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GooglePeopleResponse.class);

            List<GooglePeopleResponse.Organization> organizations =
                response != null ? response.organizations() : null;

            if (organizations != null && !organizations.isEmpty()) {
                return organizations.get(0).department();
            }
        } catch (Exception e) {
            log.warn("Google People API에서 학과 정보를 가져오지 못했습니다: {}", e.getMessage());
        }
        return null;
    }
}
