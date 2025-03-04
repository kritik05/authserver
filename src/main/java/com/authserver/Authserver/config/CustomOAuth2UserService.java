package com.authserver.Authserver.config;

import com.authserver.Authserver.model.User;
import com.authserver.Authserver.model.Role;
import com.authserver.Authserver.model.UserTenant;
import com.authserver.Authserver.repository.UserRepository;
import com.authserver.Authserver.repository.UserTenantRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;

    public CustomOAuth2UserService(UserRepository userRepository,UserTenantRepository userTenantRepository) {
        this.userRepository = userRepository;
        this.userTenantRepository=userTenantRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegates to the default service to fetch user info from Google
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Extract email (the user identifier)
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");
        String googleId = (String) oAuth2User.getAttributes().get("sub");
        String imageUrl = (String) oAuth2User.getAttributes().get("picture");

        // Find user in DB or create if doesn't exist
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setGoogleId(googleId);
                    newUser.setImageUrl(imageUrl);
                    newUser.setDefaultTenant(1); // or some logic
                    userRepository.save(newUser);

                    UserTenant ut = new UserTenant();
                    ut.setUserId(newUser.getUserId());
                    ut.setTenantId(newUser.getDefaultTenant()); // if 1 is the tenant ID
                    ut.setRole(Role.USER);
                    userTenantRepository.save(ut);
                    return newUser;
                });

        user.setGoogleId(googleId);
        user.setImageUrl(imageUrl);
        user.setName(name);
        userRepository.save(user);
        // Build a List/Set of authorities
        Role defaultRole = Role.USER; // fallback
        Optional<UserTenant> maybeUT = userTenantRepository.findByUserIdAndTenantId(
                user.getUserId(),
                user.getDefaultTenant()
        );
        if (maybeUT.isPresent()) {
            defaultRole = maybeUT.get().getRole();
        }
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + defaultRole)
        );

        // Return a new CustomUserPrincipal, or you can also return a DefaultOAuth2User with those authorities
        // We'll store the entire Google attributes as well
        return new CustomUserPrincipal(user, oAuth2User.getAttributes(), authorities);
    }

}