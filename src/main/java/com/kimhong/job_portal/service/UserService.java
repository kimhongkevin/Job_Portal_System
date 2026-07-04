package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.UserRequest;
import com.kimhong.job_portal.dto.UserResponse;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UserResponse mapToUserResponse(User user){
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse).toList();
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found."));
    }

    // For authenticated use-their own info
    public UserResponse getMyInfo(String email){
        return mapToUserResponse(getUserByEmail(email));
    }

    // For admin, look up any user by id
    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return mapToUserResponse(user);
    }

    public UserResponse updateMyInfo(String email, UserRequest request){
        User user = getUserByEmail(email);

        if(request.getFullName() != null && !request.getFullName().isBlank()){
            user.setFullName(request.getFullName());
        }

        if(request.getNewPassword() !=null && !request.getNewPassword().isBlank()){
            if(request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()){
                throw new ResourceNotFoundException("Current password is required");
            }

            if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword() )){
                throw new BadRequestException("Current password is incorrect.");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return mapToUserResponse(userRepository.save(user));
    }

    public void deleteUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        userRepository.delete(user);
    }
}
