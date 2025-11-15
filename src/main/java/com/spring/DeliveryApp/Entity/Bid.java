package com.spring.DeliveryApp.Entity;

import com.spring.DeliveryApp.auth.Entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    private double bidAmount;
    private LocalDateTime bidTime = LocalDateTime.now();
}