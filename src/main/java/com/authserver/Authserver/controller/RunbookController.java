package com.authserver.Authserver.controller;

import com.authserver.Authserver.model.Runbook;
import com.authserver.Authserver.model.RunbookRule;
import com.authserver.Authserver.repository.RunbookRepository;
import com.authserver.Authserver.repository.RunbookRuleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/runbook")
public class RunbookController {

    private final RunbookRepository runbookRepository;
    private final RunbookRuleRepository runbookRuleRepository;

    public RunbookController(RunbookRepository runbookRepository,
                             RunbookRuleRepository runbookRuleRepository) {
        this.runbookRepository = runbookRepository;
        this.runbookRuleRepository = runbookRuleRepository;
    }

    @GetMapping
    public List<Runbook> getRunbooks(@RequestParam Long tenantId) {
        return runbookRepository.findByTenantId(tenantId);
    }

    @PostMapping
    public Runbook createRunbook(@RequestBody Map<String,Object> body) {
        Runbook rb = new Runbook();

        Long tenantId = body.get("tenantId") != null
                ? Long.valueOf(body.get("tenantId").toString())
                : null;
        String runbookName = (String) body.getOrDefault("runbookName", "Unnamed");
        String runbookDesc = (String) body.getOrDefault("runbookDescription", "");

        rb.setTenantId(tenantId);
        rb.setRunbookName(runbookName);
        rb.setRunbookDescription(runbookDesc);
        rb.setEnabled(false);
        long randomLong = Math.abs(new java.util.Random().nextInt());
        rb.setRunbookId(randomLong);

        return runbookRepository.save(rb);
    }


    @PutMapping("/{runbookId}/enable")
    public Runbook toggleRunbook(@PathVariable Long runbookId, @RequestParam boolean value) {
        Runbook rb = runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));
        rb.setEnabled(value);
        return runbookRepository.save(rb);
    }

    @PutMapping("/{runbookId}")
    public Runbook updateRunbook(@PathVariable Long runbookId, @RequestBody Map<String,Object> body) {
        Runbook rb = runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));
        if(body.containsKey("runbookName")) {
            rb.setRunbookName(body.get("runbookName").toString());
        }
        if(body.containsKey("runbookDescription")) {
            rb.setRunbookDescription(body.get("runbookDescription").toString());
        }
        return runbookRepository.save(rb);
    }

    @GetMapping("/{runbookId}/rules")
    public List<RunbookRule> getRulesForRunbook(@PathVariable Long runbookId) {
        return runbookRuleRepository.findByRunbookRunbookId(runbookId);
    }

    @PostMapping("/{runbookId}/rules")
    public RunbookRule createRule(
            @PathVariable Long runbookId,
            @RequestBody Map<String,Object> body
    ) {
        Runbook rb = runbookRepository.findById(runbookId).orElseThrow(
                () -> new RuntimeException("Runbook not found")
        );
        RunbookRule rule = new RunbookRule();
        rule.setRunbook(rb);

        if(body.containsKey("triggerType")) {
            rule.setTriggerType(body.get("triggerType").toString());
        }
        if(body.containsKey("filterType")) {
            rule.setFilterType(body.get("filterType").toString());
        }
        if(body.containsKey("filterParams")) {
            rule.setFilterParams(body.get("filterParams").toString());
        }
        if(body.containsKey("actionType")) {
            rule.setActionType(body.get("actionType").toString());
        }
        if(body.containsKey("actionParams")) {
            rule.setActionParams(body.get("actionParams").toString());
        }
        rule.setEnabled(true);

        return runbookRuleRepository.save(rule);
    }

    @PutMapping("/rules/{ruleId}")
    public RunbookRule updateRule(
            @PathVariable Long ruleId,
            @RequestBody Map<String,Object> body
    ) {
        RunbookRule rule = runbookRuleRepository.findById(ruleId).orElseThrow(
                () -> new RuntimeException("Rule not found")
        );
        if(body.containsKey("triggerType")) {
            rule.setTriggerType(body.get("triggerType").toString());
        }
        if(body.containsKey("filterType")) {
            rule.setFilterType(body.get("filterType").toString());
        }
        if(body.containsKey("filterParams")) {
            rule.setFilterParams(body.get("filterParams").toString());
        }
        if(body.containsKey("actionType")) {
            rule.setActionType(body.get("actionType").toString());
        }
        if(body.containsKey("actionParams")) {
            rule.setActionParams(body.get("actionParams").toString());
        }
        if(body.containsKey("isEnabled")) {
            rule.setEnabled(Boolean.parseBoolean(body.get("isEnabled").toString()));
        }
        return runbookRuleRepository.save(rule);
    }

    @PutMapping("/rules/{ruleId}/trigger")
    public RunbookRule updateRuleTrigger(
            @PathVariable Long ruleId,
            @RequestBody Map<String,Object> body
    ) {
        RunbookRule rule = runbookRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        if(body.containsKey("triggerType")) {
            rule.setTriggerType(body.get("triggerType").toString());
        }

        return runbookRuleRepository.save(rule);
    }

    @PutMapping("/rules/{ruleId}/filter")
    public RunbookRule updateRuleFilter(
            @PathVariable Long ruleId,
            @RequestBody Map<String,Object> body
    ) {
        RunbookRule rule = runbookRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        if(body.containsKey("filterType")) {
            rule.setFilterType(body.get("filterType").toString());
        }
        if(body.containsKey("filterParams")) {
            rule.setFilterParams(body.get("filterParams").toString());
        }
        return runbookRuleRepository.save(rule);
    }

    @PutMapping("/rules/{ruleId}/action")
    public RunbookRule updateRuleAction(
            @PathVariable Long ruleId,
            @RequestBody Map<String,Object> body
    ) {
        RunbookRule rule = runbookRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        if(body.containsKey("actionType")) {
            rule.setActionType(body.get("actionType").toString());
        }
        if(body.containsKey("actionParams")) {
            rule.setActionParams(body.get("actionParams").toString());
        }
        return runbookRuleRepository.save(rule);
    }

    @GetMapping("/{runbookId}")
    public Runbook getRunbook(@PathVariable Long runbookId) {
        return runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));
    }
}
