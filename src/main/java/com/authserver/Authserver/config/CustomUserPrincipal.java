package com.authserver.Authserver.config;

import com.authserver.Authserver.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomUserPrincipal implements OAuth2User {

    private final AppUser appUser;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(AppUser appUser,
                               Map<String, Object> attributes,
                               Collection<? extends GrantedAuthority> authorities) {
        this.appUser = appUser;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getName() {
        return this.appUser.getEmail();
    }

    public AppUser getAppUser() {
        return this.appUser;
    }
}