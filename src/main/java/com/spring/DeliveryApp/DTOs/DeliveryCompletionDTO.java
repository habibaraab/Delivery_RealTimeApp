package com.spring.DeliveryApp.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryCompletionDTO {
    @NotNull(message = "معرف الطلب مطلوب")
    private Long orderId;

    @NotBlank(message = "معرف السائق مطلوب")
    private String driverId;

}