package com.spring.DeliveryApp.Entity;

import com.spring.DeliveryApp.Enum.OrderStatus;
import com.spring.DeliveryApp.auth.Entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    // إحداثيات مكان الاستلام
    private double pickupLatitude;
    private double pickupLongitude;

    // إحداثيات مكان التسليم
    private double dropoffLatitude;
    private double dropoffLongitude;

    private double deliveryFee;
    private Double acceptedBid;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}