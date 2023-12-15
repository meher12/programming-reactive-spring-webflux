package com.reactivespring.client;

import com.reactivespring.domian.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewRestClient {
    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public ReviewRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {

        var url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand().toString();
        return webClient.get()
                .uri(url)
                .retrieve()

                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    // Log the status code for 4xx client errors
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    // Check if the client error is a 404 Not Found
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        // If it's a 404, return an empty Mono to indicate no further action is needed
                        return Mono.empty();
                    }
                    // If it's a different 4xx error, extract the response body and create a ReviewsClientException
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(
                                    responseMessage)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->{
                    // Log the status code for 5xx server errors
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    // Extract the response body and create a ReviewsServerException for server errors
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error( new ReviewsServerException(
                                    "Server Exception in ReviewService" + responseMessage )));
                })
                .bodyToFlux(Review.class)
                .log();
    }
}
