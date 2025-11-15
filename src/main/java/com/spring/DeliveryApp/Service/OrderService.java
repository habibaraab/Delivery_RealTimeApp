package com.spring.DeliveryApp.Service;

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

        Order newOrder = new Order();
        newOrder.setCustomer(customer);
        newOrder.setPickupLocation(request.getPickupLocation());
        newOrder.setDropoffLocation(request.getDropoffLocation());
        newOrder.setDeliveryFee(request.getDeliveryFee());
        newOrder.setStatus(OrderStatus.BIDDING);

        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> {
                    OrderItem item = new OrderItem();
                    item.setProductName(itemDto.getProductName());
                    item.setWeightKg(itemDto.getWeightKg());
                    item.setLengthCm(itemDto.getLengthCm());
                    item.setWidthCm(itemDto.getWidthCm());
                    item.setOrder(newOrder); // ربط المنتج بالطلب
                    return item;
                }).collect(Collectors.toList());

        newOrder.setItems(items);
        Order savedOrder = orderRepository.save(newOrder);

        // **المرحلة الثانية:** إشعار جميع السائقين المتاحين
        notifyAvailableDrivers(savedOrder);

        return convertToDto(savedOrder);
    }

    // دالة مساعدة لتحويل Order إلى DTO
    private OrderResponseDTO convertToDto(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomer().getName());
        dto.setPickupLocation(order.getPickupLocation());
        dto.setDropoffLocation(order.getDropoffLocation());
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
}