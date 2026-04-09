package com.example.taskManagement.Configurations;

import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findActiveUserWithRolesByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with mail: "+ email));
        return new CustomUserDetails(user);
    }
}
