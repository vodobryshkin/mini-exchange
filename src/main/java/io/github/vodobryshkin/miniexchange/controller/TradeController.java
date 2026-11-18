package io.github.vodobryshkin.miniexchange.controller;

import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.trade.response.TradeResponse;
import io.github.vodobryshkin.miniexchange.security.ExchangeUserDetails;
import io.github.vodobryshkin.miniexchange.service.spec.ITradeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для получения реестра заключенных сделок.
 *
 * @author vodobryshkin
 * @since 19.11.2026 02:00
 */
@RestController
@RequestMapping(
        value = {"/trades", "/api/trades"},
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class TradeController {
    private final ITradeService tradeService;
    private final Validator validator;

    /**
     * Метод для получения реестра сделок по доступным пользователю счетам.
     *
     * @param user авторизованный пользователь.
     * @param tradingAccountId идентификатор торгового счета.
     * @param secCode код торгового инструмента.
     * @param from начало периода включительно.
     * @param to конец периода исключительно.
     * @param page номер страницы.
     * @param size размер страницы.
     * @return HTTP-ответ со страницей реестра сделок.
     */
    @GetMapping
    public ResponseEntity<PageResponse<TradeResponse>> getTrades(
            @AuthenticationPrincipal ExchangeUserDetails user,
            @RequestParam(required = false) Long tradingAccountId,
            @RequestParam(required = false) String secCode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        RegistryRequest request = RegistryRequest.builder()
                .tradingAccountId(tradingAccountId)
                .secCode(secCode)
                .from(from)
                .to(to)
                .page(page)
                .size(size)
                .build();

        Set<ConstraintViolation<RegistryRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tradeService.getTrades(user.getId(), request));
    }
}
