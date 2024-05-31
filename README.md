# http-server-agent

This repository contains a Spring Boot application (`http-server`) and a Java agent (`agent-loader`) for mocking HTTP and database calls. This setup is useful for:

- **Testing:** Simulating external dependencies in unit and integration tests.
- **Development:**  Creating realistic environments for development without relying on actual external systems.
- **Performance Testing:**  Analyzing application performance with controlled data and responses.

# Record Mode
java -javaagent:/path/to/agent-loader.jar -jar /path/to/http-server.jar

# Replay Mode 
REPLAY_MODE=true java -javaagent:/path/to/agent-loader.jar -jar /path/to/http-server.jar


## Dependencies

* **Spring Boot:**  [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
* **Byte Buddy:**  [https://bytebuddy.net/](https://bytebuddy.net/)
* **Your Database Driver:**  (e.g., PostgreSQL, MySQL)

## Contributing

Contributions are welcome! Feel free to open issues or pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
