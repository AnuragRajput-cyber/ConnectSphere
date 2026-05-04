package com.connectsphere.auth.oauth;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OAuth2UserProfileService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final ParameterizedTypeReference<List<Map<String, Object>>> EMAIL_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final RestClient restClient = RestClient.builder().build();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);
        Map<String, Object> attributes = new LinkedHashMap<>(oauthUser.getAttributes());
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();

        if ("github".equals(registrationId) && isBlank(stringValue(attributes.get(EMAIL_ATTRIBUTE)))) {
            attributes.put(EMAIL_ATTRIBUTE, fetchGithubEmail(userRequest.getAccessToken().getTokenValue()));
        }

        return new DefaultOAuth2User(
                oauthUser.getAuthorities(),
                attributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    private String fetchGithubEmail(String accessToken) {
        List<Map<String, Object>> emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(EMAIL_LIST_TYPE);

        if (emails == null || emails.isEmpty()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("github_email_missing"),
                    "GitHub did not return any email addresses."
            );
        }

        return emails.stream()
                .filter(emailEntry -> Boolean.TRUE.equals(emailEntry.get("verified")))
                .filter(emailEntry -> Boolean.TRUE.equals(emailEntry.get("primary")))
                .map(emailEntry -> stringValue(emailEntry.get(EMAIL_ATTRIBUTE)))
                .filter(email -> !isBlank(email))
                .findFirst()
                .or(() -> emails.stream()
                        .filter(emailEntry -> Boolean.TRUE.equals(emailEntry.get("verified")))
                        .map(emailEntry -> stringValue(emailEntry.get(EMAIL_ATTRIBUTE)))
                        .filter(email -> !isBlank(email))
                        .findFirst())
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("github_verified_email_missing"),
                        "GitHub did not return a verified email address."
                ));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
