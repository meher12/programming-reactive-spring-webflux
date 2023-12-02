# Reactive Microservices using Spring WebFlux and Spring Boot

Welcome to the Reactive Microservices tutorial, where we explore the realm of reactive programming using the Reactor framework in the context of building microservices with Spring WebFlux and Spring Boot.

## 1. Reactive Programming with Reactor
**Project name : reactive-programming-using-reactor**

### 1.1 Creating Flux and Mono
- Implement the `FluxAndMonoGeneratorService` class to create your first Flux and Mono.

### 1.2 Testing with JUnit5
- Develop the `FluxAndMonoGeneratorServiceTest` class for testing Flux using JUnit5.

### 1.3 Transforming Data Using Operators in Project Reactor for Flux
Explore various operators for transforming data in Flux:
- `map()`: Transform each item emitted by the Flux.
- `filter()`: Filter items based on a given predicate.
- `flatMap()`: Flatten the items emitted by the Flux.
- `concatMap()`: Similar to flatMap but maintains the order of emitted items.

### 1.4 Transforming Data Using Operators in Project Reactor for Mono
Learn about operators for transforming data in Mono:
- `flatMap()`: Transform the emitted item.
- `flatMapMany()`: Works similarly to flatMap.

### 1.5 Additional Operators for Flux or Mono
Discover more operators for transforming data:
- `transform()`: Perform a general transformation on Flux or Mono.
- `defaultIfEmpty()` and `switchIfEmpty()`: Handle empty scenarios.

### 1.6 Combining Flux and Mono in Reactive Streams
Learn how to combine multiple reactive streams:
- `concat()` (Static method in Flux) and `concatWith()` (Instance method in Flux and Mono): Combine two reactive streams into one, maintaining order.
- `merge()` (Static method in Flux) and `mergeWith()` (Instance method in Flux and Mono): Merge two publishers into one.
- `mergeSequential()`: Merge two Flux publishers into one sequentially.
- `zip()` (Static method in Flux) and `zipWith()` (Instance method in Flux and Mono): Merge up to 2 to 8 Publishers (Flux or Mono) into one.

Explore the power of reactive programming with Spring WebFlux and Spring Boot! Happy coding!
