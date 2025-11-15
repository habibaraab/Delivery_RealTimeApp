package com.spring.DeliveryApp.Service;

import com.spring.DeliveryApp.DTOs.LocationUpdateDTO;
import com.spring.DeliveryApp.Entity.Order;
import com.spring.DeliveryApp.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * تعالج تحديث موقع السائق وتبث الإحداثيات للعميل الخاص بالطلب.
     * @param locationDto الإحداثيات المرسلة من السائق
     */
    public void processDriverLocation(LocationUpdateDTO locationDto) {

        Order order = orderRepository.findById(locationDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود."));

        // التحقق الأمني: يجب التأكد أن السائق الذي يرسل التحديث هو السائق المعين للطلب
        if (order.getDriver() == null || !locationDto.getDriverId().equals(order.getDriver().getId())) {
            throw new SecurityException("السائق غير مصرح له بتحديث موقع هذا الطلب.");
        }

        // إرسال الإحداثيات كرسالة خاصة للمستخدم (العميل) صاحب الطلب
        // المسار: /user/{customerId}/queue/track/{orderId}
        // العميل يجب أن يشترك في هذا المسار لتلقي تحديثات الخريطة
        String destination = "/queue/track/" + order.getId();

        // نرسل الـ DTO (الذي يحتوي على الإحداثيات) مباشرة
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getCustomer().getId()),
                destination,
                locationDto
        );

        System.out.println(String.format("✅ بث موقع (Lat: %.4f, Lon: %.4f) للعميل على الطلب %d",
                locationDto.getLatitude(), locationDto.getLongitude(), order.getId()));
    }
}