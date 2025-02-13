package com.authserver.Authserver.repository;

import com.authserver.Authserver.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    Tenant findByOwnerAndRepo(String owner, String repo);
}