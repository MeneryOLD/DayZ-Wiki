package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.ChangePasswordDto;
import com.dayzwiki.portal.dto.TokenDto;
import com.dayzwiki.portal.service.ChangePasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/change")
public class ChangePasswordController {

    private final ChangePasswordService changePasswordService;

    @GetMapping("/password")
    public ResponseEntity<String> changePasswordByEmail(@RequestParam("email") String email) {
        try {
            changePasswordService.changePasswordByEmail(email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/password")
    public ResponseEntity<String> changePasswordByToken(@RequestBody TokenDto tokenDto) {
        try {
            changePasswordService.changePasswordByToken(tokenDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/password/by-username")
    public ResponseEntity<String> changePasswordByUsername(@RequestBody ChangePasswordDto changePasswordDto) {
        try {
            changePasswordService.changePasswordByUsername(changePasswordDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
