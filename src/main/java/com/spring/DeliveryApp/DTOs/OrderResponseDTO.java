package com.spring.DeliveryApp.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String customerName;

    // إحداثيات الطلب في الاستجابة
    private double pickupLatitude;
    private double pickupLongitude;
    private double dropoffLatitude;
    private double dropoffLongitude;

    private double deliveryFee;
    private String status;
    private String assignedDriverName;
    private List<OrderItemDTO> items;
}