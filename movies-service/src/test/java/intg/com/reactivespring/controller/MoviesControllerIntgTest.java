package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domian.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebClient
@AutoConfigureWireMock(port = 8084) // automaticaly spins up a httpserver in port 8084
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
        "restClient.reviewsUrl=http://localhost:8084/v1/reviews",
})
class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;


    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    void retrieveMovieById() {
        var movieId = "abc";
        // Mocking Movie Info Endpoint:
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                // Configuring the response for the "/v1/movieinfos/{movieId}" endpoint
                .willReturn(aResponse()
                        // Setting the response header to indicate JSON content
                        .withHeader("Content-Type", "application/json")
                        // Using the contents of "movieinfo.json" file as the response body
                        .withBodyFile("movieinfo.json")));

        // Mocking Reviews Endpoint:
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                // Expecting a GET request to "/v1/reviews?movieInfoId=abc" with a specific query parameter
                .withQueryParam("movieInfoId", equalTo(movieId))
                // Configuring the response for the "/v1/reviews" endpoint
                .willReturn(aResponse()
                        // Setting the response header to indicate JSON content
                        .withHeader("Content-Type", "application/json")
                        // Using the contents of "reviews.json" file as the response body
                        .withBodyFile("reviews.json")));

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }

    @Test
    void retrieveMovieById_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(aResponse()
                        .withStatus(404)));
        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo Available for the passed in id: abc");
        //then
        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));
        ;
    }

    @Test
    void retrieveMovieById_Reviews_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(aResponse()
                        .withStatus(404)));
        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is2xxSuccessful();
        //then
        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));
        ;
    }

    @Test
    void retrieveMovieById_5XX() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")));

        /*stubFor(get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(aResponse()
                        .withStatus(500)));
*/

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(message -> {
                    assertEquals("Server Exception in MoviesInfoServiceMovieInfo Service Unavailable", message);
                });
        //then
        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));
    }

    @Test
    void retrieveMovieById_reviews_5XX() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));


        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review Service Unavailable")));

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(message -> {
                    assertEquals("Server Exception in ReviewServiceReview Service Unavailable", message);
                });
        //then
        WireMock.verify(1, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }

}