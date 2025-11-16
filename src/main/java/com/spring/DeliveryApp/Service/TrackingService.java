package com.spring.DeliveryApp.Service;

import com.spring.DeliveryApp.DTOs.LocationUpdateDTO;
import com.spring.DeliveryApp.Entity.Order;
import com.spring.DeliveryApp.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
//To Track live location
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Processes the driver's location update and broadcasts the coordinates to the order's customer.
     *  locationDto The coordinates sent by the driver
     */
    public void processDriverLocation(LocationUpdateDTO locationDto) {

        Order order = orderRepository.findById(locationDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found."));

        // Security Check: Must ensure the driver sending the update is the one assigned to the order
        if (order.getDriver() == null || !locationDto.getDriverId().equals(order.getDriver().getId())) {
            throw new SecurityException("Driver is not authorized to update the location for this order.");
        }

        // Send the coordinates as a private message to the order owner (customer)
        // Path: /user/{customerId}/queue/track/{orderId}
        // The customer must subscribe to this path to receive map updates
        String destination = "/queue/track/" + order.getId();

        // We send the DTO (which contains the coordinates) directly
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getCustomer().getId()),
                destination,
                locationDto
        );

        System.out.println(String.format("Broadcasting location (Lat: %.4f, Lon: %.4f) to customer for order %d",
                locationDto.getLatitude(), locationDto.getLongitude(), order.getId()));
    }
}