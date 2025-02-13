package com.authserver.Authserver.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "default_tenant", nullable = false)
    private int defaultTenant;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean enabled = true;



    public User() {
    }

    public User(Integer userId, String name, String email, int defaultTenant, String googleId, String imageUrl, boolean enabled) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.defaultTenant = defaultTenant;
        this.googleId = googleId;
        this.imageUrl = imageUrl;
        this.enabled = enabled;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(int defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}