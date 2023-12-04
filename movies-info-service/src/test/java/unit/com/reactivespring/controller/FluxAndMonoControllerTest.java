package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void flux_approach1() {
        // Perform a GET request to "/flux"
        webTestClient.get()
                .uri("/flux")
                .exchange()
                // Verify that the HTTP status is in the 2xx range (successful)
                .expectStatus().is2xxSuccessful()
                // Verify that the response body is a list of integers
                .expectBodyList(Integer.class)
                // Verify that the list has a size of 3
                .hasSize(3);
    }

    @Test
    void flux_approach2() {
        // Perform a GET request to "/flux"
        var flux = webTestClient.get()
                .uri("/flux")
                .exchange()
                // Verify that the HTTP status is in the 2xx range (successful)
                .expectStatus().is2xxSuccessful()
                .returnResult(Integer.class)
                .getResponseBody();
        StepVerifier.create(flux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void flux_approach3() {
        // Perform a GET request to "/flux"
        webTestClient.get()
                .uri("/flux")
                .exchange()
                // Verify that the HTTP status is in the 2xx range (successful)
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    assert (Objects.requireNonNull(responseBody).size() == 3);
                });
    }

    @Test
    void mono() {
        // Perform a GET request to "/flux"
        webTestClient.get()
                .uri("/mono")
                .exchange()
                // Verify that the HTTP status is in the 2xx range (successful)
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Helle world", responseBody);
                });
    }

    @Test
    void stream() {
        // Perform a GET request to "/flux"
        var flux = webTestClient.get()
                .uri("/stream")
                .exchange()
                // Verify that the HTTP status is in the 2xx range (successful)
                .expectStatus().is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();
        StepVerifier.create(flux)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();
    }
}