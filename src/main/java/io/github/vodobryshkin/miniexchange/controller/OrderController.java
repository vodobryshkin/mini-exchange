package io.github.vodobryshkin.miniexchange.controller;

import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.UpdateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderResponse;
import io.github.vodobryshkin.miniexchange.security.ExchangeUserDetails;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    /**
     * Метод для создания новой заявки на бирже.
     *
     * @param user авторизованный пользователь.
     * @param request тело запроса на создание заявки.
     * @return HTTP-ответ с результатом создания заявки.
     */
    @PostMapping
    public ResponseEntity<OrderOperationResponse> createOrder(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @RequestBody @Valid CreateOrderRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(user.getId(), request));
    }

    /**
     * Метод для отмены неисполненного остатка заявки.
     *
     * @param user авторизованный пользователь.
     * @param orderId идентификатор заявки.
     * @return HTTP-ответ со статусом NO CONTENT.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @PathVariable("id") long orderId) {

        orderService.cancelOrder(user.getId(), orderId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Метод для изменения цены и общего количества заявки.
     *
     * @param user авторизованный пользователь.
     * @param orderId идентификатор заявки.
     * @param request тело запроса на изменение заявки.
     * @return HTTP-ответ с обновлённой заявкой.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @PathVariable("id") long orderId,
            @RequestBody @Valid UpdateOrderRequest request) {

        return ResponseEntity.ok(orderService.updateOrder(
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

        return ResponseEntity.ok(
                orderService.getOrders(user.getId(), request)
        );
    }
}