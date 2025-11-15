package com.spring.DeliveryApp.DTOs;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    @NotBlank(message = "يجب تحديد اسم المنتج")
    private String productName;

    @Positive(message = "يجب أن يكون الوزن قيمة موجبة")
    private double weightKg;

    @Positive(message = "يجب أن يكون الطول قيمة موجبة")
    private double lengthCm;

    @Positive(message = "يجب أن يكون العرض قيمة موجبة")
    private double widthCm;
}