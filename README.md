# Reactive Microservices using Spring WebFlux and Spring Boot

Welcome to the Reactive Microservices tutorial, where we explore the realm of reactive programming using the Reactor framework in the context of building microservices with Spring WebFlux and Spring Boot.

## 1. Reactive Programming with Reactor
**Project name : reactive-programming-using-reactor**

### 1 Creating Flux and Mono
- Implement the `FluxAndMonoGeneratorService` class to create your first Flux and Mono.

### 2 Testing with JUnit5
- Develop the `FluxAndMonoGeneratorServiceTest` class for testing Flux using JUnit5. 
* Using ***Ctrl+Alt+T*** to create test method

### 3 Transforming Data Using Operators in Project Reactor for Flux
Explore various operators for transforming data in Flux:
- `map()`: Transform each item emitted by the Flux.
- `filter()`: Filter items based on a given predicate.
- `flatMap()`: Flatten the items emitted by the Flux.
- `concatMap()`: Similar to flatMap but maintains the order of emitted items.

### 4 Transforming Data Using Operators in Project Reactor for Mono
Learn about operators for transforming data in Mono:
- `flatMap()`: Transform the emitted item.
- `flatMapMany()`: Works similarly to flatMap.

### 5 Additional Operators for Flux or Mono
Discover more operators for transforming data:
- `transform()`: Perform a general transformation on Flux or Mono.
- `defaultIfEmpty()` and `switchIfEmpty()`: Handle empty scenarios.

### 6 Combining Flux and Mono in Reactive Streams
Learn how to combine multiple reactive streams:
- `concat()` (Static method in Flux) and `concatWith()` (Instance method in Flux and Mono): Combine two reactive streams into one, maintaining order.
- `merge()` (Static method in Flux) and `mergeWith()` (Instance method in Flux and Mono): Merge two publishers into one.
- `mergeSequential()`: Merge two Flux publishers into one sequentially.
- `zip()` (Static method in Flux) and `zipWith()` (Instance method in Flux and Mono): Merge up to 2 to 8 Publishers (Flux or Mono) into one.

## 2. Spring WebFlux
Spring WebFlux, a component of the Spring ecosystem, is a reactive programming framework designed for constructing asynchronous, non-blocking, and event-driven applications. Key features include the use of `Flux` and `Mono` for handling data streams, non-blocking I/O to enhance resource management, annotation-based programming for developer convenience, and integration with Reactive Streams.

### 1. Simple Non Blocking RESTFUL API using Annotated Controller Approach:
* Here's the provided dependency information added to your movies-info-service project
```groovy
dependencies {
	// MongoDB Reactive support
	implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'

	// Validator for input validation
	implementation 'org.springframework.boot:spring-boot-starter-validation'

	// Spring WebFlux for reactive web support
	implementation 'org.springframework.boot:spring-boot-starter-webflux'

	// Lombok for reducing boilerplate code
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'

	testImplementation 'org.springframework.boot:spring-boot-starter-test'

	// Embedded MongoDB for testing purposes
	implementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.3.3'

	// Reactor Test for testing reactive components
	testImplementation 'io.projectreactor:reactor-test'
}
```
####  1. Build a Simple Non Blocking API - Flux, Mono, Infinite Streams API Server-Sent Events (SSE) in `FluxAndMonoController` class
####  2. Automated Tests using JUnit5 and @WebFluxTest:
1. Configure Unit and Integration Test Folders in Gradle
    ```groovy
    sourceSets {
        test{
            // Configure Unit and Integration Test Folders in Gradle
            java.srcDirs = ['src/test/java/unit', 'src/test/java/intg']
        }
    }
    ```
2. Unit test Spring Webflux Endpoint using `@WebFluxTest` Annotation
3. Different Approaches to Unit Testing Flux, Mono and Streaming Server-Sent Events (SSE) Endpoint
### 2. Reactive Programming in MongoDB for the MovieInfo Service
1. Create The `MovieInfo class` and `MovieInfoRepository interface`
2. Config `application.yml` file
3. Set up the Integration Test using `@DataMongoTest` on `MovieInfoRepositoryIntegTest` class
   - Write Integration Test for findAll(), findById(), Saving, Updating and Deleting MovieInfo Document
### Build MovieInfo Service using Rest Controller Approach
**Using curl-commands.txt file to check http requests**
1. Build a POST endpoint to create a new MovieInfo then handling the Integration Test using JUnit5
2. Build a GET Endpoint to get all the MoviesInfo  then handling the Integration Test
3. Build a GET By ID Endpoint to retrieve a MoviesInfo  then handling the Integration Test
4. Build a PUT Endpoint to update a MovieInfo by ID  then handling the Integration Test
5. Build the DELETE endpoint to delete a MovieInfo by ID  then handling the Integration Test
