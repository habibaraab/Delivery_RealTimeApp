package com.spring.DeliveryApp.Controller;

import com.spring.DeliveryApp.DTOs.BidRequestDTO;
import com.spring.DeliveryApp.Service.BiddingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class BiddingController {

    private final BiddingService biddingService; // الخدمة التي ستحفظ وتُشعر العميل

    /**
     * يستقبل عروض المزايدة من السائقين عبر WebSocket
     * العميل يرسل إلى: /app/bid/submit
     */
    @MessageMapping("/bid/submit")
    public void submitBid(@Valid @Payload BidRequestDTO bidRequest) {
        // يتم تمرير العرض إلى خدمة المزايدة
        biddingService.processBid(bidRequest);
    }
}