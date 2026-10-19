package com.andesstay.msbff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

// Recibe el regreso del navegador desde Transbank tras el pago
// (token_ws si se completó; sin token si el usuario anuló o expiró la transacción)
// y redirige al SPA, que con el JWT del usuario ejecuta el commit del pago.
// Acepta GET y POST: Transbank devuelve normalmente por POST (formulario), pero
// la navegación también puede repetirse como GET con el token en la URL.
@RestController
@RequestMapping("/payments")
public class PaymentReturnBffController {

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @RequestMapping(value = "/return", method = {RequestMethod.GET, RequestMethod.POST})
    public Mono<ResponseEntity<Void>> webpayReturn(ServerWebExchange exchange) {
        String queryToken = exchange.getRequest().getQueryParams().getFirst("token_ws");
        if (queryToken != null && !queryToken.isBlank()) {
            return Mono.just(buildRedirect(queryToken));
        }

        return exchange.getFormData()
                .map(form -> form.getFirst("token_ws"))
                .defaultIfEmpty("")
                .onErrorReturn("")
                .map(this::buildRedirect);
    }

    private ResponseEntity<Void> buildRedirect(String tokenWs) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/payment-result");

        if (tokenWs != null && !tokenWs.isBlank()) {
            builder.queryParam("token_ws", tokenWs);
        } else {
            // sin token_ws => el usuario anuló el pago o la transacción expiró
            builder.queryParam("status", "cancelled");
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(builder.build().encode().toUri())
                .build();
    }
}