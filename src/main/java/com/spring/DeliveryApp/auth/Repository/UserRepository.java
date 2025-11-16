package com.spring.DeliveryApp.auth.Repository;

import com.spring.DeliveryApp.auth.Entity.User;
import com.spring.DeliveryApp.auth.Enum.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findUserByName(String name);

    
    List<User> findAllByRole(Role role);
}
