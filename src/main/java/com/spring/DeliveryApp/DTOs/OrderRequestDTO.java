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

    // إحداثيات مكان الاستلام (يتم اختيارها من الخريطة)
    @NotNull(message = "خط عرض الاستلام مطلوب")
    private Double pickupLatitude;
    @NotNull(message = "خط طول الاستلام مطلوب")
    private Double pickupLongitude;

    // إحداثيات مكان التسليم
    @NotNull(message = "خط عرض التسليم مطلوب")
    private Double dropoffLatitude;
    @NotNull(message = "خط طول التسليم مطلوب")
    private Double dropoffLongitude;

    @NotNull(message = "رسوم التوصيل التقديرية مطلوبة")
    private Double deliveryFee;

    @Valid
    @NotNull
    @Size(min = 1, message = "يجب تحديد محتويات الطلب")
    private List<OrderItemDTO> items;
}