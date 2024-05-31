package main.java.org.example.httpserver.agent;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBInterceptor {

    private static boolean isReplayMode = System.getenv("REPLAY_MODE") != null;
    private static final Map<String, List<Map<String, Object>>> recordedDbData = new HashMap<>();

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                     @AllArguments Object[] args,
                                     @This Object statement) throws Throwable {

        if (isReplayMode) {
            return replayDatabaseCall(method, args);
        } else {
            return recordDatabaseCall(method, args, statement);
        }
    }

    private static Object recordDatabaseCall(Method method, Object[] args, Object statement) throws Throwable {
        String sqlQuery = (String) args[0];
        System.out.println("SQL Query: " + sqlQuery);

        Object result = method.invoke(statement, args);
        if (result instanceof ResultSet) {
            recordedDbData.put(sqlQuery, mapResultSet((ResultSet) result));
        }

        return result;
    }

    private static Object replayDatabaseCall(Method method, Object[] args) throws Throwable {
        String sqlQuery = (String) args[0];

        if (method.getName().equals("executeQuery")) {
            List<Map<String, Object>> mockData = recordedDbData.get(sqlQuery);
            if (mockData != null) {
                // Create and return a mock ResultSet from mockData
                return new MockResultSet(mockData);
            } else {
                // Handle case where data is not found
                return new MockResultSet(new ArrayList<>()); // Empty ResultSet
            }
        } else if (method.getName().equals("executeUpdate")) {
            return 1; // Simulate successful update
        }

        return method.invoke(args);
    }

    private static List<Map<String, Object>> mapResultSet(ResultSet resultSet) throws Exception {
        List<Map<String, Object>> resultSetData = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> rowData = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            resultSetData.add(rowData);
        }

        return resultSetData;
    }
}