package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/@{username}")
    public String handleUserRequest(Model model, @PathVariable String username) {
        Optional<User> user = userRepository.findByName(username);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "user-profile";
        }
        return "error";
    }

}
