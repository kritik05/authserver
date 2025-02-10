package com.authserver.Authserver.controller;

import com.authserver.Authserver.config.CustomUserPrincipal;
import com.authserver.Authserver.model.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    @GetMapping("/currentuser")
    public ResponseEntity<Map<String, Object>> getCurrentUser(OAuth2AuthenticationToken authToken) {
        if (authToken == null) {
            return ResponseEntity.ok(Collections.singletonMap("authenticated", false));
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) authToken.getPrincipal();
        AppUser appUser = principal.getAppUser();
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("email", appUser.getEmail());
        response.put("name", appUser.getName());
        response.put("role", appUser.getRole().name());
        return ResponseEntity.ok(response);
    }
}
