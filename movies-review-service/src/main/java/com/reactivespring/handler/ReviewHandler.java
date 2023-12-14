package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                // Like this
                /*.flatMap(review -> {
                 return    reviewReactiveRepository.save(review);
                });*/

                // OR replace it with expression lambda
                .flatMap(reviewReactiveRepository::save)

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

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
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

    private static Mono<ServerResponse> buildReviewResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }
}
