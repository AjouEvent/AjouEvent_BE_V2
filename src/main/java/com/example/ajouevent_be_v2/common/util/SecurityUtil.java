package com.example.ajouevent_be_v2.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {

    public static String getCurrentMemberUsernameOrAnonymous() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            return "Anonymous";
        }

        return authentication.getName();
    }
}
