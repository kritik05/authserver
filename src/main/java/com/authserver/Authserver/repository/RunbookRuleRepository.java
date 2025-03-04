package com.authserver.Authserver.repository;


import com.authserver.Authserver.model.RunbookRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunbookRuleRepository extends JpaRepository<RunbookRule, Long> {

    List<RunbookRule> findByRunbookRunbookId(Long runbookId);
}
