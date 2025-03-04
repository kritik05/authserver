package com.authserver.Authserver.repository;


import com.authserver.Authserver.model.Runbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunbookRepository extends JpaRepository<Runbook, Long> {

    List<Runbook> findByTenantId(Long tenantId);
    List<Runbook> findByTenantIdAndIsEnabledTrue(Long tenantId);

}