package com.spring.DeliveryApp.auth.Dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class LoginRequest {
    private String name;
    private String password;
}