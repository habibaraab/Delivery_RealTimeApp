package com.spring.DeliveryApp.DTOs;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BidAcceptanceDTO {
    @NotNull(message = "معرف الطلب مطلوب")
    private Long orderId;

    @NotNull(message = "معرف العرض المقبول مطلوب")
    @Positive(message = "معرف العرض يجب أن يكون موجباً")
    private Long acceptedBidId;
}