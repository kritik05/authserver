package com.authserver.Authserver.repository;

import com.authserver.Authserver.model.TenantTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantTicketRepository extends JpaRepository<TenantTicket, Integer> {
    Optional<TenantTicket> findByTicketId(String ticketId);

    // For fetching by finding ID + tenant
    Optional<TenantTicket> findByFindingIdAndTenantId(String findingId, Integer tenantId);

    // If you want to filter by tenant
    List<TenantTicket> findByTenantId(Integer tenantId);
    Page<TenantTicket> findByTenantId(int tenantId, Pageable pageable);
}