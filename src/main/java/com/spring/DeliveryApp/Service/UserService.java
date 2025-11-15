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
    public void updateUserAvailability(int userId, boolean isAvailable) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // تحديث حالة التوفر
            user.setAvailable(isAvailable);
            userRepository.save(user);

            System.out.println(String.format("🔄 تم تحديث حالة المستخدم %s إلى: %s",
                    userId, isAvailable ? "ONLINE" : "OFFLINE"));
        }
    }
}