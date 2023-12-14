package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    /*@Bean
    public RouterFunction<ServerResponse> reviewRoute() {
        return route()
                .GET("/v1/helloworld", (request -> ServerResponse
                        .ok()
                        .bodyValue("helloworld")))
                .build();
    }*/

    @Bean
    public RouterFunction<ServerResponse> reviewRoute(ReviewHandler reviewHandler) {
        return route()
                // Build the POST endpoint for creating a new Review
                //.POST("/v1/reviews", request-> reviewHandler.addReview(request))
                // Build the GET endpoint for retrieving all the Reviews
                //.GET("/v1/reviews", request-> reviewHandler.getReviews(request))
                // Nesting Endpoints using nest() Function
                .nest(path("/v1/reviews"), builder -> {
                    builder.POST("", request -> reviewHandler.addReview(request))
                            .GET("", request -> reviewHandler.getReviews(request))
                            .PUT("/{id}", request -> reviewHandler.updateReview(request))
                            .DELETE("/{id}", request -> reviewHandler.deleteReview(request));

                })
                .build();
    }
}
