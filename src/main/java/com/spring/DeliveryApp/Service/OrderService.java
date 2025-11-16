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
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        Order newOrder = new Order();
        newOrder.setCustomer(customer);
        newOrder.setPickupLatitude(request.getPickupLatitude());
        newOrder.setPickupLongitude(request.getPickupLongitude());
        newOrder.setDropoffLatitude(request.getDropoffLatitude());
        newOrder.setDropoffLongitude(request.getDropoffLongitude());
        newOrder.setDeliveryFee(request.getDeliveryFee());
        newOrder.setStatus(OrderStatus.BIDDING);

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

        // Notify all available drivers
        notifyAvailableDrivers(savedOrder);

        return convertToDto(savedOrder);
    }

    public OrderResponseDTO convertToDto(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomer().getName());

        dto.setPickupLatitude(order.getPickupLatitude());
        dto.setPickupLongitude(order.getPickupLongitude());
        dto.setDropoffLatitude(order.getDropoffLatitude());
        dto.setDropoffLongitude(order.getDropoffLongitude());

        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setStatus(order.getStatus().name());
        if (order.getDriver() != null) {
            dto.setAssignedDriverName(order.getDriver().getName());
        }

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

    // Notification function
    public void notifyAvailableDrivers(Order order) {
        List<User> availableDrivers = userRepository.findAllByRole(Role.DRIVER).stream()
                .filter(User::isAvailable)
                .collect(Collectors.toList());

        OrderResponseDTO orderDto = convertToDto(order);

        //  /topic/drivers/new-orders
        messagingTemplate.convertAndSend("/topic/drivers/new-orders", orderDto);

        System.out.println(" Order No. " + order.getId() + " to " + availableDrivers.size() + " available drivers.");
    }

    @Transactional
    public void completeDelivery(DeliveryCompletionDTO completionDto) {

        Order order = orderRepository.findById(completionDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found."));

        // 1. Security Check: Must ensure the driver completing the order is the assigned driver.
        if (order.getDriver() == null || !completionDto.getDriverId().equals(order.getDriver().getId())) {
            throw new SecurityException("Driver is not authorized to complete this order.");
        }

        // 2. Update status
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        // 3. Send final WebSocket notifications:

        // Customer Notification: Delivery successful (Path: /user/{customerId}/queue/notifications)
        String customerMsg = "Your order  " + order.getId() + " has been delivered successfully. Thank you for using our service.";
        messagingTemplate.convertAndSendToUser(
                String.valueOf(  order.getCustomer().getId()),
                "/queue/notifications",
                customerMsg
        );

        // Driver Notification: Order closed (Path: /user/{driverId}/queue/notifications)
        String driverMsg = "Order No. " + order.getId() + " has been completed. Amount due: " + order.getAcceptedBid();
        messagingTemplate.convertAndSendToUser(
                String.valueOf(  order.getDriver().getId()),
                "/queue/notifications",
                driverMsg
        );

        // Additional step: Notify the customer that tracking has ended
        // (This stops the client application from attempting to update the map)
        String trackingEndMsg = "TRACKING_END";
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getCustomer().getId()),
                "/queue/track/" + order.getId(),
                trackingEndMsg
        );

        System.out.println("Order  " + order.getId() + " completed successfully.");
    }
}