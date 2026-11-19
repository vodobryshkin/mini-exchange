package io.github.vodobryshkin.miniexchange.service.impl;

import io.github.vodobryshkin.miniexchange.service.spec.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Класс для фонового исполнения заявок и завершения действия DAY-заявок.
 *
 * @author vodobryshkin
 * @since 19.11.2026 09:01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {
    private final IOrderService orderService;

    /**
     * Метод для выполнения ограниченного количества шагов исполнения заявок.
     */
    @Scheduled(fixedDelayString = "${exchange.matching.delay-ms:200}")
    public void matchOrders() {
        try {
            for (int i = 0; i < 50 && orderService.matchNext(); i++) {
                // Один шаг выполняется отдельной транзакцией через Spring-прокси.
            }
        } catch (RuntimeException ex) {
            log.error(
                    "Order matching failed; processing will retry on the next run",
                    ex
            );
        }
    }

    /**
     * Метод для завершения действия ограниченного количества просроченных заявок.
     */
    @Scheduled(
            fixedDelayString = "${exchange.trading-day.expiration-delay-ms:1000}"
    )
    public void expireOrders() {
        try {
            for (int i = 0; i < 100 && orderService.expireNext(); i++) {
            }
        } catch (RuntimeException ex) {
            log.error(
                    "DAY expiration failed; processing will retry on the next run",
                    ex
            );
        }
    }
}