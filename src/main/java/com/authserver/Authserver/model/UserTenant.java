package com.authserver.Authserver.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_tenant")
public class UserTenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Enumerated(EnumType.STRING)
    private Role role;

    public UserTenant(){}

    public UserTenant(Integer id, Integer userId, Integer tenantId, Role role) {
        this.id = id;
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}