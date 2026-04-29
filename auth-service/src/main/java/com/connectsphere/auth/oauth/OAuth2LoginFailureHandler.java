package com.connectsphere.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final String failureRedirectUrl;

    public OAuth2LoginFailureHandler(
            @Value("${app.oauth.failure-redirect-url}") String failureRedirectUrl
    ) {
        this.failureRedirectUrl = failureRedirectUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String message = exception.getMessage() == null ? "OAuth login failed." : exception.getMessage();
        response.sendRedirect(
                failureRedirectUrl + "?oauthError=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
        );
    }
}
