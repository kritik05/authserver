package com.authserver.Authserver.controller;

import com.authserver.Authserver.config.CustomUserPrincipal;
import com.authserver.Authserver.model.Tenant;
import com.authserver.Authserver.model.User;
import com.authserver.Authserver.model.UserTenant;
import com.authserver.Authserver.repository.TenantRepository;
import com.authserver.Authserver.repository.UserTenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserTenantRepository userTenantRepository;
    private final TenantRepository tenantRepository;
    public UserController(UserTenantRepository userTenantRepository,TenantRepository tenantRepository) {
        this.userTenantRepository = userTenantRepository;
        this.tenantRepository=tenantRepository;
    }

    @GetMapping("/currentuser")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            OAuth2AuthenticationToken authToken,
            @RequestParam(value = "tenantId", required = false) Integer tenantId
    ) {
        if (authToken == null) {
            return ResponseEntity.ok(Collections.singletonMap("authenticated", false));
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) authToken.getPrincipal();
        User user = principal.getUser();

        if (tenantId == null) {
            tenantId = user.getDefaultTenant();
        }

        // 4. Find the user_tenant record for that user & tenant
        UserTenant userTenant = userTenantRepository
                .findByUserIdAndTenantId(user.getUserId(), tenantId)
                .orElse(null);

        List<UserTenant> userTenantList = userTenantRepository.findByUserId(user.getUserId());
        List<Map<String, Object>> userTenantListWithNames = new ArrayList<>();


        for (UserTenant ut : userTenantList) {
            Map<String, Object> item = new HashMap<>();
            item.put("tenantId", ut.getTenantId());
            item.put("role", ut.getRole().name());

            Tenant t = tenantRepository.findById(ut.getTenantId()).orElse(null);
            if (t != null) {
                item.put("tenantName", t.getTenant_name());
            } else {
                System.out.println(("Unknown Tenant"));
            }

            userTenantListWithNames.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("defaultTenant", user.getDefaultTenant());
        response.put("requestedTenant", tenantId);
        if (userTenant != null) {
            response.put("roleForTenant", userTenant.getRole().name());
        } else {
            response.put("roleForTenant", "NO_ROLE_FOUND");
        }
        response.put("userTenantList", userTenantListWithNames);

        return ResponseEntity.ok(response);
    }
}
