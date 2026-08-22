package com.multitalent.email.consumer;

import com.multitalent.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Listens to "email-events" (published by auth-service) and sends
 * transactional emails. Best-effort — failures are logged, not retried
 * indefinitely, so a bad SMTP config doesn't block the consumer group.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;

    @KafkaListener(topics = "email-events", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(Object event) {
        if (!(event instanceof UserRegisteredEvent registered)) {
            log.debug("Ignoring non-user-registered event on email-events topic: {}", event);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(registered.getEmail());
            message.setSubject("Welcome to Multi-Talent SaaS Platform");
            message.setText("Hi " + registered.getFullName() + ",\n\n" +
                    "Your account has been created successfully. Welcome aboard!\n\n" +
                    "— The Multi-Talent SaaS Platform Team");
            mailSender.send(message);
            log.info("Welcome email sent to {}", registered.getEmail());
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}", registered.getEmail(), ex);
        }
    }
}
