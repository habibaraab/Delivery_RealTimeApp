package com.spring.DeliveryApp.Service;


import com.spring.DeliveryApp.auth.Entity.User;
import com.spring.DeliveryApp.auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateUserAvailability(String userId, boolean isAvailable) {
        Optional<User> userOpt = userRepository.findUserByName(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setAvailable(isAvailable);
            userRepository.save(user);
            System.out.println(String.format("🔄 User status updated for %s to: %s",
                    userId, isAvailable ? "ONLINE" : "OFFLINE"));

        }}}