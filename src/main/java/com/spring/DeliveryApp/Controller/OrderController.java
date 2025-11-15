package com.spring.DeliveryApp.Controller;

import com.spring.DeliveryApp.DTOs.BidAcceptanceDTO;
import com.spring.DeliveryApp.DTOs.DeliveryCompletionDTO;
import com.spring.DeliveryApp.DTOs.OrderRequestDTO;
import com.spring.DeliveryApp.DTOs.OrderResponseDTO;
import com.spring.DeliveryApp.Service.BiddingService;
import com.spring.DeliveryApp.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    private final BiddingService biddingService ;
    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * يستقبل طلب العميل لقبول عرض مزايدة محدد.
     * @param acceptance معلومات الطلب والعرض المقبول
     * @return OrderResponseDTO
     */
    @PostMapping("/accept-bid")
    public ResponseEntity<OrderResponseDTO> acceptBid(@Valid @RequestBody BidAcceptanceDTO acceptance) {
        OrderResponseDTO response = biddingService.acceptBid(acceptance);
        return ResponseEntity.ok(response);
    }

    /**
     * يستقبل طلب إنهاء الخدمة من تطبيق السائق (بعد التوصيل).
     */
    @PostMapping("/complete-delivery")
    public ResponseEntity<String> completeDelivery(@Valid @RequestBody DeliveryCompletionDTO completionDto) {
        orderService.completeDelivery(completionDto);
        return ResponseEntity.ok("تم إنهاء الطلب بنجاح. تم إرسال إشعار للعميل.");
    }

}