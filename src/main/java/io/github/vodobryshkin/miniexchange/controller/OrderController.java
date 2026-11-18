package io.github.vodobryshkin.miniexchange.controller;

import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.security.ExchangeUserDetails;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для обработки запросов, связанных с заявками на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 20:13
 */
@RestController
@RequestMapping(
        value = "/orders",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<OrderOperationResponse> createOrder(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(user.getId(), request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderOperationResponse> cancelOrder(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @RequestParam long orderId) {
        orderService.cancelOrder(user.getId(), orderId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
