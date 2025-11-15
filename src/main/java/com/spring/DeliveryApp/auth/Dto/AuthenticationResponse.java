package com.spring.DeliveryApp.auth.Dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
}