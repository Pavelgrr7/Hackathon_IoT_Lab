package com.tst.iotlab;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserManager userManager;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        String isAdmin = credentials.get("isAdmin");
        logger.info("Login attempt, isAdmin: {}", isAdmin);

        logger.debug("Attempting login with username: {}", username);

        boolean isAuthenticated = userManager.authenticate(username, password);
        if (isAuthenticated) {
            logger.info("Login successful for user: {}", username);
            Map<String, Object> response = new HashMap<>();
            if (String.valueOf(userManager.isAdmin(username)).equals(isAdmin)) {
                response.put("success", true);
            } else {
                if (Objects.equals(String.valueOf(isAdmin), "true")){
                    response.put("success", false);
                } else {
                    response.put("success", true);
                }

            }
            session.setAttribute("username", username);
            session.setAttribute("isAdmin", userManager.isAdmin(username));
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Failed login attempt for user: {}", username);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
