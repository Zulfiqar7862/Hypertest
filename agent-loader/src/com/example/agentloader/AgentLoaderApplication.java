package com.example.agentloader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class AgentLoaderApplication {

    private static final String AGENT_JAR_NAME = "agent-loader.jar";
    private static final String SPRING_BOOT_MAIN_CLASS = "main.java.org.example.httpserver.HttpserverApplication";

    public static void main(String[] args) throws Exception {
        // 1. Find Agent JAR (Search Multiple Locations)
        String agentJarPath = findAgentJar();
        if (agentJarPath == null) {
            System.err.println("Error: Could not find agent JAR (" + AGENT_JAR_NAME + ")");
            System.exit(1);
        }

        // 2. Load Agent JAR into Classpath
        ClassLoader newClassLoader = loadAgentJar(agentJarPath);

        // 3. Start Spring Boot Application using the new classloader
        startSpringBootApplication(args, newClassLoader);
    }

    private static String findAgentJar() {
        String[] potentialPaths = {
                System.getProperty("user.dir"), // Current directory
                new File(System.getProperty("user.dir")).getParent(), // Parent directory
                new File(System.getProperty("user.dir")).getParent() + "/http-server/target", // Parent's http-server/target
                new File(System.getProperty("user.dir")).getParent() + "/agent-loader/target/agent-loader-0.0.1-SNAPSHOT.jar"  // Exact path to the JAR
        };

        for (String path : potentialPaths) {
            System.out.println("Searching for agent JAR in: " + path);
            File agentJar = new File(path);
            if (agentJar.exists()) {
                System.out.println("Found agent JAR at: " + agentJar.getAbsolutePath());
                return agentJar.getAbsolutePath();
            }
        }

        System.out.println("Agent JAR not found in any of the specified locations.");
        return null;
    }

    private static ClassLoader loadAgentJar(String agentJarPath) throws Exception {
        URL agentJarUrl = new File(agentJarPath).toURI().toURL();

        // 1. Get the system classloader
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        // 2. Use reflection to find the 'addURL' method (no need to make it accessible)
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);

        // 3. Find the appropriate classloader (either the system classloader or a parent)
        ClassLoader classLoader = systemClassLoader;
        while (classLoader != null && !(classLoader instanceof URLClassLoader)) {
            classLoader = classLoader.getParent();
        }

        // 4. If a URLClassLoader was found, create a new URLClassLoader with the added URL
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            URL[] existingUrls = urlClassLoader.getURLs();
            URL[] newUrls = Arrays.copyOf(existingUrls, existingUrls.length + 1);
            newUrls[newUrls.length - 1] = agentJarUrl;

            // 5. Use reflection to invoke the 'addURL' method on the new URLClassLoader
            URLClassLoader newClassLoader = new URLClassLoader(newUrls, urlClassLoader.getParent());
            addURL.invoke(newClassLoader, agentJarUrl);

            return newClassLoader;

        } else {
            System.err.println("Error: Could not find a suitable URLClassLoader to add the agent JAR to the classpath.");
            return null; // Or throw an exception
        }
    }

    private static void startSpringBootApplication(String[] args, ClassLoader classLoader) throws Exception {
        // Load the main class using the newClassLoader
        Class<?> mainClass = Class.forName(SPRING_BOOT_MAIN_CLASS, true, classLoader);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, new Object[]{args});
    }
}