package com.jlu.registration.module.identity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserAccountRepository accounts;

    public CurrentUserService(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    public UserAccount account() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("当前用户未登录");
        }
        return accounts.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("登录账号不存在"));
    }

    public Long requireLinkedPersonId() {
        Long linkedId = account().getLinkedPersonId();
        if (linkedId == null) {
            throw new IllegalStateException("当前账号没有关联人员档案");
        }
        return linkedId;
    }
}

