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
    private String pickupLocation;
    private String dropoffLocation;
    private double deliveryFee;
    private String status;
    private String assignedDriverName;
    private List<OrderItemDTO> items;
}