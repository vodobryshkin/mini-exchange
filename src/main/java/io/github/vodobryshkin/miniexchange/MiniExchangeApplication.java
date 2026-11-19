package io.github.vodobryshkin.miniexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Приложение учебной торговой системы.
 *
 * @author vodobryshkin
 * @since 19.11.2026
 */
@SpringBootApplication
public class MiniExchangeApplication {

    /**
     * Метод для запуска приложения.
     *
     * @param args аргументы запуска.
     */
    public static void main(String[] args) {
        SpringApplication.run(MiniExchangeApplication.class, args);
    }

}
