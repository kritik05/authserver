package com.authserver.Authserver.repository;

import com.authserver.Authserver.model.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTenantRepository extends JpaRepository<UserTenant, Integer> {
    // Finds all the (tenant_id, role) pairs for a given user:
    List<UserTenant> findByUserId(Integer userId);

    // Optionally, if you need to find a single user-tenant pair:
    Optional<UserTenant> findByUserIdAndTenantId(Integer userId, Integer tenantId);
}