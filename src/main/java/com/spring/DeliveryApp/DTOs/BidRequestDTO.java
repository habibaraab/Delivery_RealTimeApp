package com.spring.DeliveryApp.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BidRequestDTO {
    @NotNull(message = "معرف الطلب مطلوب")
    private Long orderId;

    @NotBlank(message = "معرف السائق مطلوب")
    private Integer driverId;

    @Positive(message = "يجب أن يكون مبلغ العرض قيمة موجبة")
    private double bidAmount;
}