package io.github.vodobryshkin.miniexchange.controller;

import io.github.vodobryshkin.miniexchange.dto.orderbook.request.OrderBookRequest;
import io.github.vodobryshkin.miniexchange.dto.orderbook.response.OrderBookResponse;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для обработки запросов, связанных с получением биржевого стакана.
 *
 * @author vodobryshkin
 * @since 19.11.2026 07:27
 */
@RestController
@RequestMapping(
        value = "/instruments",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class OrderBookController {
    private final IOrderBookService orderBookService;

    /**
     * Метод для настройки разрешённых полей при заполнении параметров запроса стакана.
     *
     * @param binder объект для настройки привязки параметров запроса.
     */
    @InitBinder("orderBookRequest")
    public void initOrderBookBinder(WebDataBinder binder) {
        binder.setAllowedFields("depth");
    }

    /**
     * Метод для получения актуального биржевого стакана по торговому инструменту.
     *
     * @param secCode код торгового инструмента.
     * @param request параметры запроса биржевого стакана.
     * @return HTTP-ответ с биржевым стаканом и его показателями.
     */
    @GetMapping("/{secCode}/orderbook")
    public ResponseEntity<OrderBookResponse> getOrderBook(
            @PathVariable("secCode") String secCode,
            @Valid @ModelAttribute("orderBookRequest") OrderBookRequest request) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(orderBookService.getOrderBook(
                        secCode,
                        request.getDepth()
                ));
    }
}