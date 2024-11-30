package com.dayzwiki.portal.service;

import com.dayzwiki.portal.dto.LoginDto;
import com.dayzwiki.portal.security.JWTAuthResponse;
import com.dayzwiki.portal.security.JwtTokenProvider;
import com.dayzwiki.portal.service.user.AuthorizationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSignInSuccessfully() {
        LoginDto loginDto = new LoginDto("user", "password", true);
        Authentication authentication = mock(Authentication.class);
        String token = "generatedToken";

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn(token);

        JWTAuthResponse authResponse = authorizationService.signIn(loginDto, response);

        assertNotNull(authResponse);
        assertEquals(token, authResponse.getAccessToken());
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void shouldReturnNullWhenBadCredentials() {
        LoginDto loginDto = new LoginDto("user", "wrongPassword", true);

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        JWTAuthResponse authResponse = authorizationService.signIn(loginDto, response);

        assertNull(authResponse);
    }

    @Test
    void shouldLogoutSuccessfully() {
        HttpSession session = mock(HttpSession.class);

        authorizationService.logOut(session, response);

        verify(session).invalidate();
        verify(response).addCookie(argThat(cookie ->
                "dayzwiki_user_token".equals(cookie.getName()) && cookie.getMaxAge() == -1));
    }
}
