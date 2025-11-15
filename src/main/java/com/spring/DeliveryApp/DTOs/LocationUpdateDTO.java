package com.spring.DeliveryApp.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateDTO {
    @NotNull private Long orderId;
    @NotNull private String driverId;
    @NotNull private Double latitude; // خط العرض: لعرض موقع السائق على الخريطة
    @NotNull private Double longitude; // خط الطول: لعرض موقع السائق على الخريطة
}