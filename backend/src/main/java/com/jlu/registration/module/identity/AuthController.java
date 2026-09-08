package com.jlu.registration.module.identity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CurrentUserService currentUser;

    public AuthController(CurrentUserService currentUser) {
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public CurrentUserView login() {
        return view();
    }

    @GetMapping("/me")
    public CurrentUserView me() {
        return view();
    }

    private CurrentUserView view() {
        UserAccount account = currentUser.account();
        return new CurrentUserView(
                account.getUsername(),
                account.getDisplayName(),
                account.getRole(),
                account.getLinkedPersonId()
        );
    }

    public record CurrentUserView(
            String username,
            String displayName,
            UserRole role,
            Long linkedPersonId
    ) {
    }
}
