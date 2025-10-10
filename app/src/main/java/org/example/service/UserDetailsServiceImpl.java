package org.example.service;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entities.UserInfo;
import org.example.model.UserInfoDto;
import org.example.repository.UserInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Component
@AllArgsConstructor
@Data
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    @Autowired
    private final UserInfoRepository userInfoRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("Entering in loadUserByUsername Method...");
        UserInfo user = userInfoRepository.findByUsername(username);
        if (user == null) {
            log.error("Username not found: " + username);
            throw new UsernameNotFoundException("could not found user..!!");
        }
        log.info("User Authenticated Successfully..!!!");
        return new CustomUserDetails(user);
    }

    public UserInfo checkIfUserAlreadyExist(UserInfoDto userInfoDto) {

        return userInfoRepository.findByUsername(userInfoDto.getUsername());
    }

    public Boolean signupUser(UserInfoDto userInfoDto) {
        userInfoDto.setPassword(passwordEncoder.encode(userInfoDto.getPassword()));

        if (Objects.nonNull(checkIfUserAlreadyExist(userInfoDto))) {
            return false;
        }

        String userId = UUID.randomUUID().toString();
        userInfoRepository.save(new UserInfo(userId, userInfoDto.getUsername(), userInfoDto.getPassword(), new HashSet<>()));

        return true;

    }

    public Boolean validateUserCredentials(String username, String rawPassword) {
        // Fetch the user from the database by username (or email if your system uses that)
        UserInfo user = userInfoRepository.findByUsername(username);

        // Check if user exists
        if (user == null) {
            log.warn("User not found: {}", username);
            return false;
        }

        // Verify the raw password against the encoded one stored in DB
        boolean isPasswordValid = passwordEncoder.matches(rawPassword, user.getPassword());

        if (!isPasswordValid) {
            log.warn("Invalid password for user: {}", username);
            return false;
        }

        log.info("User credentials are valid for: {}", username);
        return true;
    }


}
