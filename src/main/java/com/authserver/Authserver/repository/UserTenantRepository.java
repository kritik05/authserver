package com.authserver.Authserver.repository;

import com.authserver.Authserver.model.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTenantRepository extends JpaRepository<UserTenant, Integer> {
    List<UserTenant> findByUserId(Integer userId);
    Optional<UserTenant> findByUserIdAndTenantId(Integer userId, Integer tenantId);
}