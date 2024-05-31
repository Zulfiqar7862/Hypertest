package main.java.org.example.httpserver.agent;

import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HttpInterceptor {

    private static boolean isReplayMode = System.getenv("REPLAY_MODE") != null;
    private static final Map<String, String> recordedHttpResponses = new HashMap<>();

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @AllArguments Object[] args,
                                   @This Object httpClient) throws Throwable {
        if (isReplayMode) {
            return replayHttpRequest(method, args);
        } else {
            return recordHttpRequest(method, args, httpClient);
        }
    }

    private static Object recordHttpRequest(Method method, Object[] args, Object httpClient) throws Throwable {
        System.out.println("HTTP Request: " + extractRequestDetails(args));
        Object response = method.invoke(httpClient, args);

        if (response instanceof ResponseEntity) {
            ResponseEntity<?> re = (ResponseEntity<?>) response;
            recordedHttpResponses.put(extractRequestDetails(args), re.getBody().toString());
        }

        return response;
    }

    private static Object replayHttpRequest(Method method, Object[] args) {
        String requestKey = extractRequestDetails(args);
        String hardcodedResponseBody = recordedHttpResponses.get(requestKey);

        if (hardcodedResponseBody != null) {
            return new ResponseEntity<>(hardcodedResponseBody, HttpStatus.OK);
        } else {
            System.err.println("No recorded response found for: " + requestKey);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // (Helper function to extract relevant request details for the key)
    private static String extractRequestDetails(Object[] args) {
        // You'll likely need to adjust this based on your HTTP client
        // and how you make requests.
        if (args.length > 0 && args[0] instanceof String) {
            return (String) args[0]; // Assuming URL is the first argument
        }
        return "default"; // Or handle cases where you can't get details
    }
}