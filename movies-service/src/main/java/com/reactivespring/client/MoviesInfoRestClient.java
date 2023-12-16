package com.reactivespring.client;

import com.reactivespring.domian.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

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

        /**
         * Creates a retry spec with specific behavior:
         * - Maximum retries: 3 (with 1-second delay between each)
         * - Only retries if the exception is a MoviesInfoServerException 5XX error.
         * - Throws the original exception if all retries are exhausted.
         * This allows for targeted retry logic for specific server-side issues.
         */
       /* var retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1))
                // Retry only for specific 5XX exception
                .filter(ex -> ex instanceof MoviesInfoServerException)
                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure())));*/
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
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error( new MoviesInfoServerException(
                                    "Server Exception in MoviesInfoService" + responseMessage )));
                })

                .bodyToMono(MovieInfo.class)
                // Retry the failed call 3 number of times before giving up.
                //.retry(3)
                //.retryWhen(retrySpec)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }

}
