package com.hyguettelabs.medibridge.security;

import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {}

    public static UserAccount require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails details)) {
            throw ApiException.unauthorized("Authentication required");
        }
        return details.getUser();
    }
}
