package com.reactivespring.client;

import com.reactivespring.domian.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {

        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        var url = moviesInfoUrl.concat("/{id}");
        return webClient
                .get()
                .uri(url, movieId)
                .retrieve()
                //  This code handles 4xx client errors from the MoviesInfoClient.
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        // If the status code is NOT_FOUND (404), throw an exception
                        // indicating no movie info available for the requested ID.
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo Available for the passed in id: " + movieId,
                                clientResponse.statusCode().value()));
                    }
                    // For other 4xx errors, read the response message and throw an exception
                    // if it contains any error indication.
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                    responseMessage, clientResponse.statusCode().value()
                            )));
                })
                // This code handles 5xx server errors (internal server errors)
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->{
                    log.info("Received 5xx server error from MoviesInfoService: {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error( new MoviesInfoServerException(
                                    "Server Exception in MoviesInfoService" + responseMessage )));
                })

                .bodyToMono(MovieInfo.class)
                .log();
    }

}
