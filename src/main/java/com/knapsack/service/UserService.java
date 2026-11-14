package com.knapsack.service;

import com.knapsack.model.User;
import com.knapsack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(String fullName, String email, String phone, String password, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email address is already registered.");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role : "CUSTOMER");

        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword()) || rawPassword.equals(user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean changePassword(User user, String currentRawPassword, String newRawPassword) {
        if (passwordEncoder.matches(currentRawPassword, user.getPassword()) || currentRawPassword.equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newRawPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public User updateUserProfile(User user, String fullName, String phone) {
        user.setFullName(fullName);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void blockUser(Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsBlocked(true);
            userRepository.save(user);
        });
    }

    public void unblockUser(Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsBlocked(false);
            userRepository.save(user);
        });
    }

    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }
}
