package com.authserver.Authserver.security;

import com.authserver.Authserver.config.CustomUserPrincipal;
import com.authserver.Authserver.model.Role;
import com.authserver.Authserver.model.UserTenant;
import com.authserver.Authserver.repository.UserTenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.security.access.AccessDeniedException;

@Aspect
@Component
public class RoleCheckAspect {
private final UserTenantRepository userTenantRepository;

    public RoleCheckAspect(UserTenantRepository userTenantRepository) {
        this.userTenantRepository = userTenantRepository;
    }

    @Around("@annotation(requiresRoles)")
    public Object checkRoles(ProceedingJoinPoint pjp, RequiresRoles requiresRoles) throws Throwable {
        // 1) Extract required roles from annotation
        Role[] requiredRoles = requiresRoles.value();

        // 2) Get current principal from SecurityContext
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserPrincipal customUserPrincipal)) {
            throw new AccessDeniedException("No valid user principal found.");
        }


        // 3) Fetch the tenant ID from request parameter (e.g., ?tenantId=123)
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String tenantIdParam = request.getParameter("tenantId");
        if (tenantIdParam == null || tenantIdParam.isBlank()) {
            throw new AccessDeniedException("Access Denied. No tenantId provided in request.");
        }

        int tenantId;
        try {
            tenantId = Integer.valueOf(tenantIdParam);
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid tenantId parameter: " + tenantIdParam);
        }

        // 5) Look up user's role in user_tenant for that tenant
        int userId = customUserPrincipal.getUser().getUserId();
        UserTenant userTenant = userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new AccessDeniedException(
                        "No user_tenant record found for userId=" + userId + " and tenantId=" + tenantId
                ));

        Role userRole = userTenant.getRole();

        // 6) Check if userRole is in the requiredRoles
        for (Role required : requiredRoles) {
            if (userRole == required) {
                // user has a matching role, proceed
                return pjp.proceed();
            }
        }

        // 7) If we reach here, the user's role was not in the required roles
        throw new AccessDeniedException("Access Denied. User does not have the required role(s).");
    }
}
