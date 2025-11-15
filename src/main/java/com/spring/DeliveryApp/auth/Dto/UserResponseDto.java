package com.spring.DeliveryApp.auth.Dto;


import com.spring.DeliveryApp.auth.Enum.Role;
import lombok.Data;

@Data
public class UserResponseDto {
    private int id;
    private String name;
    private String email;
    private Role role;
}