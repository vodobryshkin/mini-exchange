package io.github.vodobryshkin.miniexchange.config;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Конфигурация времени и фоновой обработки заявок биржи.
 *
 * @author vodobryshkin
 * @since 19.11.2026 8:53
 */
@Configuration
@EnableScheduling
public class TradingConfig {
    /**
     * Метод для создания источника текущего времени.
     *
     * @return источник текущего времени.
     */
    @Bean
    public Clock exchangeClock() {
        return Clock.systemUTC();
    }

    /**
     * Метод для получения времени окончания торгового дня.
     *
     * @param value время из настроек приложения.
     * @return время окончания торгового дня.
     */
    @Bean
    public LocalTime tradingDayEndTime(
            @Value("${exchange.trading-day.end-time}") String value) {

        return LocalTime.parse(value);
    }

    /**
     * Метод для получения часового пояса торгового календаря.
     *
     * @param value часовой пояс из настроек приложения.
     * @return часовой пояс торгового календаря.
     */
    @Bean
    public ZoneId tradingZone(
            @Value("${exchange.trading-day.zone}") String value) {

        return ZoneId.of(value);
    }
}