# GitHub Proxy 

## Tech Stack
- Java 25
- Spring Boot 4.0.1
- WireMock 

## How to Run
1. Ensure JDK 25 is installed.
2. Build the project using Maven:
   mvn clean install
3. Run the application:
   mvn spring-boot:run

The server will start on port 8080.

## API Usage
Endpoint:
GET /api/repos/{username}

## Testing
To run the tests:
mvn test