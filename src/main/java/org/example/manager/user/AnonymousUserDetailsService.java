package org.example.manager.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.example.model.User;
import org.example.model.UserRole;

public class AnonymousUserDetailsService implements UserDetailsService{

    private static CustomUserDetails defaultUserDetails;

    {
        // Setup a mock user.
        final User anonymousUser = new User();
        anonymousUser.setId(0);
        anonymousUser.setDisplayName("Anonymous User");
        anonymousUser.setEmail("Anonymous User");
        anonymousUser.setRole(UserRole.ROLE_ADMIN);
        anonymousUser.setActive(true);

        defaultUserDetails = new CustomUserDetails(anonymousUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    public static CustomUserDetails getDefaultAnonymousUser() {
        return defaultUserDetails;
    }
}
