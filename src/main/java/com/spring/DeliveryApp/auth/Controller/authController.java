package com.spring.DeliveryApp.auth.Controller;

import com.spring.DeliveryApp.auth.Dto.AuthenticationResponse;
import com.spring.DeliveryApp.auth.Dto.LoginRequest;
import com.spring.DeliveryApp.auth.Dto.UserRequestDto;
import com.spring.DeliveryApp.auth.Service.AuthService;
import com.spring.DeliveryApp.auth.Service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class authController {


    private final AuthService authService;
    private final LogoutService logoutService;



    @PostMapping("/Login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest , @RequestParam Map<String,Object> claims) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/Register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UserRequestDto registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/Refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }




    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) {
        logoutService.logout(request, response, authentication);
        return ResponseEntity.ok("Logged out successfully");
    }

}