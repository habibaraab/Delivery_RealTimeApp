package com.spring.DeliveryApp.DTOs;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @NotBlank(message = "معرف العميل مطلوب")
    private Integer customerId;

    @NotBlank(message = "مكان الاستلام مطلوب")
    private String pickupLocation;

    @NotBlank(message = "مكان التسليم مطلوب")
    private String dropoffLocation;

    @NotNull(message = "رسوم التوصيل التقديرية مطلوبة")
    private Double deliveryFee;

    @Valid
    @NotNull
    @Size(min = 1, message = "يجب تحديد محتويات الطلب")
    private List<OrderItemDTO> items;
}