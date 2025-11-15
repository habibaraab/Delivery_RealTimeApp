package com.spring.DeliveryApp.Controller;

import com.spring.DeliveryApp.DTOs.LocationUpdateDTO;
import com.spring.DeliveryApp.Service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    /**
     * يستقبل تحديثات الموقع من تطبيق السائق عبر WebSocket
     * السائق يرسل إلى: /app/track/update-location
     */
    @MessageMapping("/track/update-location")
    public void handleDriverLocationUpdate(@Valid @Payload LocationUpdateDTO locationUpdate) {
        trackingService.processDriverLocation(locationUpdate);
    }
}