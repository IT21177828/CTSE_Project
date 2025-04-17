package com.ctse.microservice.orderService.controller;

import com.ctse.microservice.orderService.dto.OrderRequest;
import com.ctse.microservice.orderService.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private String placeOrder(@RequestBody OrderRequest orderRequest){
        log.info("Order request received: {}", orderRequest);
        orderService.placeOrder(orderRequest);
        return "Order Placed Successfully";
    }

}
