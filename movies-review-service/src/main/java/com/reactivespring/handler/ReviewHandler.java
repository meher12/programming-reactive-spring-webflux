package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.stream.Collectors;


@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;
    @Autowired
    private Validator validator;

    Sinks.Many<Review> reviewsSinks = Sinks.many().replay().latest();

    private static Mono<ServerResponse> buildReviewResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                // Like this
                /*.flatMap(review -> {
                 return    reviewReactiveRepository.save(review);
                });*/

                // OR replace it with expression lambda
                .flatMap(reviewReactiveRepository::save)

                .doOnNext(review -> {
                    reviewsSinks.tryEmitNext(review);
                })

                // To transform Provided Mono <Object> to Required type Mono<ServerResponse>
                /*.flatMap(savedReview -> {
                 return    ServerResponse.status(HttpStatus.CREATED)
                            .bodyValue(savedReview);
                });*/

                // OR replace it with lambda expression
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

   /* public Mono<ServerResponse> getReviews(ServerRequest request) {
        var reviewsFlux = reviewReactiveRepository.findAll();
        return buildReviewResponse(reviewsFlux);
    }*/

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("constraintViolations : {} ", constraintViolations);
        if (constraintViolations.size() > 0) {
            var errorMessage = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", "));
            log.info("errorMessage : {} ", errorMessage);
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
               // .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id: " + reviewId )));
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
        return existingReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }

    // Build the GET endpoint to retrieve reviews for a given MovieInfoId
    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var movieInfoId = serverRequest.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            var reviews = reviewReactiveRepository.findReviewByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewResponse(reviews);
        } else {
            var reviews = reviewReactiveRepository.findAll();
            return buildReviewResponse(reviews);
        }
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSinks.asFlux(), Review.class)
                .log();
    }
}
