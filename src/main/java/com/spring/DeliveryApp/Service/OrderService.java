package com.spring.DeliveryApp.Service;

import com.spring.DeliveryApp.DTOs.DeliveryCompletionDTO;
import com.spring.DeliveryApp.DTOs.OrderItemDTO;
import com.spring.DeliveryApp.DTOs.OrderRequestDTO;
import com.spring.DeliveryApp.DTOs.OrderResponseDTO;
import com.spring.DeliveryApp.Entity.Order;
import com.spring.DeliveryApp.Entity.OrderItem;
import com.spring.DeliveryApp.Enum.OrderStatus;
import com.spring.DeliveryApp.Repository.OrderRepository;
import com.spring.DeliveryApp.auth.Entity.User;
import com.spring.DeliveryApp.auth.Enum.Role;
import com.spring.DeliveryApp.auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("العميل غير موجود: " + request.getCustomerId()));

        // 1. تحويل DTO إلى Model وحفظه بالإحداثيات
        Order newOrder = new Order();
        newOrder.setCustomer(customer);
        newOrder.setPickupLatitude(request.getPickupLatitude());
        newOrder.setPickupLongitude(request.getPickupLongitude());
        newOrder.setDropoffLatitude(request.getDropoffLatitude());
        newOrder.setDropoffLongitude(request.getDropoffLongitude());
        newOrder.setDeliveryFee(request.getDeliveryFee());
        newOrder.setStatus(OrderStatus.BIDDING);

        // ربط محتويات الطلب (Items)
        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> {
                    OrderItem item = new OrderItem();
                    item.setProductName(itemDto.getProductName());
                    item.setWeightKg(itemDto.getWeightKg());
                    item.setLengthCm(itemDto.getLengthCm());
                    item.setWidthCm(itemDto.getWidthCm());
                    item.setOrder(newOrder);
                    return item;
                }).collect(Collectors.toList());

        newOrder.setItems(items);
        Order savedOrder = orderRepository.save(newOrder);

        // **المرحلة الثانية:** إشعار جميع السائقين المتاحين
        notifyAvailableDrivers(savedOrder);

        return convertToDto(savedOrder);
    }

    // دالة مساعدة لتحويل Order إلى DTO
    public OrderResponseDTO convertToDto(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomer().getName());

        // نقل الإحداثيات إلى DTO
        dto.setPickupLatitude(order.getPickupLatitude());
        dto.setPickupLongitude(order.getPickupLongitude());
        dto.setDropoffLatitude(order.getDropoffLatitude());
        dto.setDropoffLongitude(order.getDropoffLongitude());

        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setStatus(order.getStatus().name());
        if (order.getDriver() != null) {
            dto.setAssignedDriverName(order.getDriver().getName());
        }

        // تحويل OrderItems إلى DTOs
        List<OrderItemDTO> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getProductName(),
                        item.getWeightKg(),
                        item.getLengthCm(),
                        item.getWidthCm()
                )).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }

    // دالة الإشعار الفوري
    public void notifyAvailableDrivers(Order order) {
        // 1. جلب جميع السائقين المتاحين
        List<User> availableDrivers = userRepository.findAllByRole(Role.DRIVER).stream()
                .filter(User::isAvailable)
                .collect(Collectors.toList());

        // 2. تحويل الطلب إلى DTO للبث
        OrderResponseDTO orderDto = convertToDto(order);

        // 3. بث الطلب الجديد على مسار السائقين العام
        // المسار: /topic/drivers/new-orders
        messagingTemplate.convertAndSend("/topic/drivers/new-orders", orderDto);

        System.out.println("✅ تم بث الطلب رقم " + order.getId() + " لـ " + availableDrivers.size() + " سائقين متاحين.");
    }

    @Transactional
    public void completeDelivery(DeliveryCompletionDTO completionDto) {

        Order order = orderRepository.findById(completionDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود."));

        // 1. التحقق الأمني: يجب التأكد أن السائق الذي ينهي الطلب هو السائق المعين.
        if (order.getDriver() == null || !completionDto.getDriverId().equals(order.getDriver().getId())) {
            throw new SecurityException("السائق غير مصرح له بإنهاء هذا الطلب.");
        }

        // 2. تحديث الحالة
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        // 3. إرسال إشعارات WebSocket نهائية:

        // إشعار العميل: تم التوصيل (المسار: /user/{customerId}/queue/notifications)
        String customerMsg = "تم توصيل طلبك رقم " + order.getId() + " بنجاح. شكراً لاستخدامك خدمتنا.";
        messagingTemplate.convertAndSendToUser(
              String.valueOf(  order.getCustomer().getId()),
                "/queue/notifications",
                customerMsg
        );

        // إشعار السائق: تم إغلاق الطلب (المسار: /user/{driverId}/queue/notifications)
        String driverMsg = "تم إنهاء الطلب رقم " + order.getId() + ". المبلغ المستحق: " + order.getAcceptedBid();
        messagingTemplate.convertAndSendToUser(
                String.valueOf(  order.getDriver().getId()),
                "/queue/notifications",
                driverMsg
        );

        // 4. خطوة إضافية: إشعار العميل بأن التتبع قد انتهى
        // (هذا يوقف تطبيق العميل عن محاولة تحديث الخريطة)
        String trackingEndMsg = "TRACKING_END";
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getCustomer().getId()),
                "/queue/track/" + order.getId(),
                trackingEndMsg
        );

        System.out.println("✅ تم إنهاء الطلب رقم " + order.getId() + " بنجاح.");
    }
}