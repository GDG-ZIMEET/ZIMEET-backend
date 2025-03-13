package com.gdg.z_meet.domain.user.service;

import com.gdg.z_meet.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Cacheable(value = "userDetails", key = "#studentNumber")
    @Override
    public UserDetails loadUserByUsername(String studentNumber) throws UsernameNotFoundException {
        return userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with student number: " + studentNumber));
    }
}