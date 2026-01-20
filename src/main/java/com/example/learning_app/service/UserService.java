package com.example.learning_app.service;

import java.util.Optional; // Optionalをimport

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.learning_app.dto.UserRegistrationDto;
import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public Users registerNewUser(UserRegistrationDto dto) {
        if (usersRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("既にこのユーザー名は使用されています");
        }

        Users user = Users.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role("ROLE_USER")
                .build();

        return usersRepository.save(user);
    }
    
    public Optional<Users> findByUsername(String username) {
        // UserRepositoryにfindByUsername(String)メソッドが必要です
    	// ★修正: Optional<Users> を返すように変更
    	return usersRepository.findByUsername(username);
    }

    /**
     * ★修正: findById の実装を UsersRepository に委譲し、戻り値を Optional<Users> に変更
     */
	public Optional<Users> findById(Long id) {
		return usersRepository.findById(id);
	}
}