package com.dayzwiki.portal.service.user;

import com.dayzwiki.portal.dto.LoginDto;
import com.dayzwiki.portal.security.JWTAuthResponse;
import com.dayzwiki.portal.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public JWTAuthResponse signIn(LoginDto loginDto, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getNameOrEmail(), loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = tokenProvider.generateToken(authentication);
            Cookie cookie = new Cookie("dayzwiki_user_token", token);
            cookie.setMaxAge(loginDto.getRemember() ? 365 * 24 * 60 * 60 : 24 * 60 * 60);
            cookie.setPath("/");
            response.addCookie(cookie);

            return new JWTAuthResponse(token);
        } catch (BadCredentialsException e) {
            return null;
        }
    }

    public void logOut(HttpSession session, HttpServletResponse response) {
        Cookie cookie = new Cookie("dayzwiki_user_token", "");
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
        session.invalidate();
    }
}
