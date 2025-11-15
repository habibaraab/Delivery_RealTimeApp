package com.spring.DeliveryApp.Service;

import com.spring.DeliveryApp.DTOs.BidRequestDTO;
import com.spring.DeliveryApp.Entity.Bid;
import com.spring.DeliveryApp.Entity.Order;
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
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void processBid(BidRequestDTO request) {

        // 1. التحقق من وجود الطلب والسائق
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود."));

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("السائق غير موجود."));

        // 2. حفظ العرض في قاعدة البيانات
        Bid newBid = new Bid();
        newBid.setOrderId(order.getId());
        newBid.setDriver(driver);
        newBid.setBidAmount(request.getBidAmount());
        bidRepository.save(newBid);

        // 3. إرسال إشعار فوري للعميل صاحب الطلب (رسالة خاصة)
        // ليعلم أن هناك عرض مزايدة جديداً.

        // المسار: /user/{customerId}/queue/bids
        String destination = "/queue/bids";

        // الحمولة: يمكن أن تكون BidResponseDTO أو مجرد إشعار
        String notificationMessage = String.format("عرض جديد على طلبك رقم %d من السائق %s بمبلغ %.2f",
                order.getId(), driver.getName(), request.getBidAmount());

        // الإرسال الخاص للمستخدم (العميل)
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getCustomer().getId()),
                destination,
                notificationMessage
        );

        System.out.println("✅ تم إشعار العميل " + order.getCustomer().getName() + " بعرض جديد.");
    }
}