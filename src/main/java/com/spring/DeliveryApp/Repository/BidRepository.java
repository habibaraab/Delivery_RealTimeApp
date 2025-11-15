package com.spring.DeliveryApp.Repository;

import com.spring.DeliveryApp.Entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findAllByOrderId(Long orderId);
}
