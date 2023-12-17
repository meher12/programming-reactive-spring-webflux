package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {

    MoviesInfoService moviesInfoService;

   // Sinks.Many<MovieInfo> movieInfoSinks = Sinks.many().replay().all();
   Sinks.Many<MovieInfo> movieInfoSinks = Sinks.many().replay().latest();

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMoviesInfo() {
        return moviesInfoService.getAllMovieInfos().log();
    }

    @GetMapping("/movieinfos/year")
    public Flux<MovieInfo> getAllMoviesInfoByYear(@RequestParam(value= "year", required = false) Integer year) {
       log.info("Year is : {}" + year);
        if (year != null){
            return moviesInfoService.getMovieInfoByYear(year).log();
        }
        return moviesInfoService.getAllMovieInfos().log();
    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getByIdMoviesInfo(@PathVariable("id") String id) {
        return moviesInfoService.getByIdMovieInfos(id)
                .map(movieInfo -> ResponseEntity.ok()
                        .body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> streamMovieInfos() {

        return movieInfoSinks.asFlux().log();
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService.addMovieInfo(movieInfo)
                .doOnNext(savedInfo-> movieInfoSinks.tryEmitNext(savedInfo));
    }

    @PutMapping("/movieinfos/{id}")
     public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo, @PathVariable String id) {
         return moviesInfoService.updateMovieInfo(updatedMovieInfo, id)
                 .map(movieInfo -> ResponseEntity.ok().body(movieInfo)).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                 .log();
     }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {
       return moviesInfoService.deleteMovieInfo(id).log();
    }
}
