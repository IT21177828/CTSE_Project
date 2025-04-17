package com.ctse.microservice.orderService.service;

import brave.Tracer;
import com.ctse.microservice.orderService.client.InventoryClient;
import com.ctse.microservice.orderService.dto.OrderRequest;
import com.ctse.microservice.order.event.OrderPlacedEvent;
import com.ctse.microservice.orderService.model.Order;
import com.ctse.microservice.orderService.repository.OrderRepository;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final Tracer tracer; // from brave.Tracer

    public void placeOrder(OrderRequest orderRequest){
        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        if (!isProductInStock) {
            throw new RuntimeException("Product with sku code " + orderRequest.skuCode() + " is not in stock");
        } else {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);

            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName(orderRequest.userDetails().firstName());
            orderPlacedEvent.setLastName(orderRequest.userDetails().lastName());

            //  Create Kafka record with B3 tracing header
            ProducerRecord<String, OrderPlacedEvent> record =
                    new ProducerRecord<>("order-placed", orderPlacedEvent);

            var span = tracer.currentSpan();
            if (span != null) {
                String traceId = span.context().traceIdString(); // Brave-specific
                String spanId = span.context().spanIdString();
                String b3Header = traceId + "-" + spanId;

                record.headers().add(new RecordHeader("b3", b3Header.getBytes(StandardCharsets.UTF_8)));
                log.info("Injected B3 header: {}", b3Header);
            }

            log.info("Start - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
            kafkaTemplate.send(record);
            log.info("End - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
        }
    }
}
