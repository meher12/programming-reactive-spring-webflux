package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @Test
    void getAllMoviesInfo() {

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(moviesInfoServiceMock.getAllMovieInfos())
                .thenReturn(Flux.fromIterable(movieinfos));

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void addMovieInfo() {
        //Given
        var movieinfos = new MovieInfo("mockId", "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
       when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class)))
               .thenReturn(Mono.just(movieinfos));
        //When
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());

                    /*assert savedMovieInfo !=null;
                    assert savedMovieInfo.getMovieInfoId()!=null;*/

                });
    }

    @Test
    void updateMovieInfo() {
        //Given
        var movieInfoId = "abc";
        var movieinfos = new MovieInfo(movieInfoId, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(movieinfos));

        //When
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL+ "/{id}", movieInfoId)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedMovieInfo);
                    assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertEquals("Batman Begins",updatedMovieInfo.getName());

                    /*assert savedMovieInfo !=null;
                    assert savedMovieInfo.getMovieInfoId()!=null;*/
                });
    }

    @Test
    void addMovieInfo_validation() {
        //Given
        var movieinfos = new MovieInfo(null, "",
                -2005, List.of(""), LocalDate.parse("2005-06-15"));

        //When
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                   var responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("ResponseBody: " + responseBody);
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a Positive Value";
                    assertNotNull(responseBody);
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }

    @Test
    void updateMovieInfo_notFound() {
        var id = "def";
        var updatedMovieInfo = new MovieInfo(null, "Dark Knight Rises 1",
                2013, List.of("Christian Bale1", "Tom Hardy1"), LocalDate.parse("2012-07-20"));
        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.empty());

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}