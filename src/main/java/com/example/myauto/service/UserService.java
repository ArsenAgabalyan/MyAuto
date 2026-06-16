package com.example.myauto.service;

import com.example.myauto.entity.Role;
import com.example.myauto.entity.User;
import com.example.myauto.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        // Сохраняем пароль как текст (без шифрования)
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }
}