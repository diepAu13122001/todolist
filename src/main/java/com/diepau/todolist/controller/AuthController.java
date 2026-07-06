package com.diepau.todolist.controller;

import com.diepau.todolist.security.JwtCookieFilter;
import com.diepau.todolist.security.JwtService;
import com.diepau.todolist.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Handles register, login and logout. On successful login it issues a JWT stored in the "jwt" cookie.
@Controller
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        if (username == null || username.trim().length() < 3) {
            model.addAttribute("error", "Username must be at least 3 characters");
            return "register";
        }
        if (password == null || password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters");
            return "register";
        }
        try {
            userService.register(username.trim(), password);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        Model model) {
        if (!userService.checkLogin(username == null ? "" : username.trim(), password)) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
        String token = jwtService.generateToken(username.trim());
        Cookie cookie = new Cookie(JwtCookieFilter.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
        return "redirect:/tasks";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Clear the cookie by setting its max age to 0.
        Cookie cookie = new Cookie(JwtCookieFilter.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login?logout";
    }
}
