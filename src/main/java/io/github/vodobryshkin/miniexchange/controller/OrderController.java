package io.github.vodobryshkin.miniexchange.controller;

import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.UpdateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderResponse;
import io.github.vodobryshkin.miniexchange.security.ExchangeUserDetails;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderService;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

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
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @RequestParam long orderId) {
        orderService.cancelOrder(user.getId(), orderId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @RequestParam long orderId,
            @RequestBody @Valid UpdateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.updateOrder(
                        user.getId(),
                        orderId,
                        request
                ));
    }

    /**
     * Метод для получения реестра заявок по доступным пользователю счетам.
     *
     * @param user авторизованный пользователь.
     * @param registry общие параметры фильтрации и пагинации.
     * @param status статус заявки.
     * @param direction направление заявки.
     * @return HTTP-ответ со страницей реестра заявок.
     */
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @ModelAttribute @Valid RegistryRequest registry,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "direction", required = false) OrderDirection direction) {

        OrderRegistryRequest request = new OrderRegistryRequest(
                registry,
                status,
                direction
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.getOrders(user.getId(), request));
    }
}
