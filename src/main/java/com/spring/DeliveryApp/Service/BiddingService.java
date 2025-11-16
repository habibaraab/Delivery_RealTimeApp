package com.spring.DeliveryApp.Service;

import com.spring.DeliveryApp.DTOs.BidAcceptanceDTO;
import com.spring.DeliveryApp.DTOs.BidRequestDTO;
import com.spring.DeliveryApp.DTOs.OrderResponseDTO;
import com.spring.DeliveryApp.Entity.Bid;
import com.spring.DeliveryApp.Entity.Order;
import com.spring.DeliveryApp.Enum.OrderStatus;
import com.spring.DeliveryApp.Repository.BidRepository;
import com.spring.DeliveryApp.Repository.OrderRepository;
import com.spring.DeliveryApp.auth.Entity.User;
import com.spring.DeliveryApp.auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BiddingService {

    private final BidRepository bidRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void processBid(BidRequestDTO request) {

        // 1. Check for the existence of the order and driver
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found."));

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found."));

        // 2. Save the bid to the database
        Bid newBid = new Bid();
        newBid.setOrderId(order.getId());
        newBid.setDriver(driver);
        newBid.setBidAmount(request.getBidAmount());
        bidRepository.save(newBid);

        // 3. Send an instant notification to the order owner (private message)
        // to inform them about a new bidding offer.

        // Path: /user/{customerId}/queue/bids
        String destination = "/queue/bids";

        // Payload: Can be BidResponseDTO or just a notification
        String notificationMessage = String.format("New bid on your order No. %d from driver %s for amount %.2f",
                order.getId(), driver.getName(), request.getBidAmount());

        // Private send to the user (customer)
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getCustomer().getId()),
                destination,
                notificationMessage
        );

        System.out.println(" Customer " + order.getCustomer().getName() + " notified of a new bid.");
    }

    @Transactional
    public OrderResponseDTO acceptBid(BidAcceptanceDTO acceptance) {

        Order order = orderRepository.findById(acceptance.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found."));

        Bid acceptedBid = bidRepository.findById(acceptance.getAcceptedBidId())
                .orElseThrow(() -> new RuntimeException("Bid not found."));

        // 2. Update the order and its status
        order.setStatus(OrderStatus.ACCEPTED);
        order.setDriver(acceptedBid.getDriver());
        order.setAcceptedBid(acceptedBid.getBidAmount());
        Order updatedOrder = orderRepository.save(order);

        // 3. Notify the winning driver via WebSocket
        String driverNotification = "Congratulations! Your bid has been accepted for order No. " + order.getId() + ". Please proceed to the pickup location.";
        // Driver private path: /user/{driverId}/queue/notifications
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getDriver().getId()),
                "/queue/notifications",
                driverNotification
        );

        // 4. Notify the customer about the driver assignment
        String customerNotification = "Driver " + order.getDriver().getName() + " has been assigned to your order. You can now track their location instantly.";
        // Customer private path: /user/{customerId}/queue/notifications
        messagingTemplate.convertAndSendToUser(
                String.valueOf( order.getCustomer().getId()),
                "/queue/notifications",
                customerNotification
        );

        System.out.println(" Bid No. " + acceptedBid.getId() + " accepted for order " + order.getId());

        // We use the conversion function to ensure the response contains the updated coordinates
        return orderService.convertToDto(updatedOrder);
    }
}