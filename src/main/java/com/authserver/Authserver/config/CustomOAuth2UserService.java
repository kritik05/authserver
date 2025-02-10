package com.authserver.Authserver.config;

import com.authserver.Authserver.model.AppUser;
import com.authserver.Authserver.model.Role;
import com.authserver.Authserver.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AppUserRepository appUserRepository;

    public CustomOAuth2UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegates to the default service to fetch user info from Google
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Extract email (the user identifier)
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");

        // Find user in DB or create if doesn't exist
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseGet(() -> {
                    // If user doesn't exist, create a new entry with default role = USER
                    AppUser newUser = new AppUser(email, name, Role.USER);
                    return appUserRepository.save(newUser);
                });

        // Build a List/Set of authorities
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name())
        );

        // Return a new CustomUserPrincipal, or you can also return a DefaultOAuth2User with those authorities
        // We'll store the entire Google attributes as well
        return new CustomUserPrincipal(appUser, oAuth2User.getAttributes(), authorities);
    }

}