package com.ctse.microservice.notification.service;

import brave.Tracer;
import com.ctse.microservice.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;
    private final Tracer tracer;

    @KafkaListener(topics = "order-placed")
    public void listen(ConsumerRecord<String, OrderPlacedEvent> record) {
        OrderPlacedEvent orderPlacedEvent = record.value();

        // Extract trace ID from Kafka headers
        String traceId = null;
        Header traceHeader = record.headers().lastHeader("X-B3-TraceId");
        if (traceHeader != null) {
            traceId = new String(traceHeader.value(), StandardCharsets.UTF_8);
        }

        log.info("Kafka Consumer Trace ID: {}", traceId);
        log.info("Got Message from order-placed topic {}", orderPlacedEvent);

        // Prepare email
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(orderPlacedEvent.getEmail().toString());
            messageHelper.setSubject(String.format("Your Order with OrderNumber %s is placed successfully", orderPlacedEvent.getOrderNumber()));
            messageHelper.setText(String.format("""
                            Hi %s %s,

                            Your order with order number %s is now placed successfully.
                            
                            Best Regards,
                            Spring Shop
                            """,
                    orderPlacedEvent.getFirstName(),
                    orderPlacedEvent.getLastName(),
                    orderPlacedEvent.getOrderNumber()));
        };

        // Send email
        try {
            javaMailSender.send(messagePreparator);
            log.info("Order Notification email sent!!");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail to {}", e);
        }
    }
}
