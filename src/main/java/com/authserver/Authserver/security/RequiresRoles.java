package com.authserver.Authserver.security;

import com.authserver.Authserver.model.Role;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {
    Role[] value() default {};
}
