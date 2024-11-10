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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private HttpSession httpSession;
    @Mock
    private Authentication authentication;

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authorizationService = new AuthorizationService(authenticationManager, tokenProvider);
    }

    @Test
    void testSignInSuccess() {
        LoginDto loginDto = new LoginDto("testUser", "password", true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("mockToken");

        ResponseEntity<JWTAuthResponse> responseEntity = authorizationService.signIn(loginDto, httpServletResponse);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals("mockToken", responseEntity.getBody().getAccessToken());
        verify(httpServletResponse, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void testSignInFailureIncorrectData() {
        LoginDto loginDto = new LoginDto("testUser", "wrongPassword", true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Incorrect data"));

        ResponseEntity<JWTAuthResponse> responseEntity = authorizationService.signIn(loginDto, httpServletResponse);

        assertEquals(401, responseEntity.getStatusCodeValue());
    }

    @Test
    void testLogOut() {
        when(httpSession.getId()).thenReturn("sessionId");

        ResponseEntity<?> responseEntity = authorizationService.logOut(httpSession, httpServletResponse);

        assertEquals(200, responseEntity.getStatusCodeValue());
        verify(httpServletResponse, times(1)).addCookie(any(Cookie.class));
        verify(httpSession, times(1)).invalidate();
    }
}
