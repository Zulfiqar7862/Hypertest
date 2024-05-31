package main.java.org.example.httpserver.config;

import main.java.org.example.httpserver.agent.DBInterceptor;
import main.java.org.example.httpserver.agent.HttpInterceptor;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
public class AppConfig {
    @PostConstruct
    public void init() {
        // Get the Byte Buddy agent instance
        ByteBuddyAgent.install();

        // HTTP Interceptor
        new AgentBuilder.Default()
                .type(ElementMatchers.named("org.springframework.web.client.RestTemplate"))
                .transform((builder, type, classLoader, module, protectionDomain) -> builder // Explicit parameter name
                        .method(ElementMatchers.any())
                        .intercept(MethodDelegation.to(HttpInterceptor.class)))
                .installOnByteBuddyAgent();

        // DB Interceptor (executeUpdate)
        new AgentBuilder.Default()
                .type(ElementMatchers.isSubTypeOf(Statement.class))
                .transform((builder, type, classLoader, module, protectionDomain) -> builder // Explicit parameter name
                        .method(ElementMatchers.isOverriddenFrom(Connection.class)
                                .and(ElementMatchers.returns(int.class))
                                .and(ElementMatchers.takesArgument(0, String.class))
                                .and(ElementMatchers.named("executeUpdate")))
                        .intercept(MethodDelegation.to(DBInterceptor.class)))
                .installOnByteBuddyAgent();

        // DB Interceptor (executeQuery)
        new AgentBuilder.Default()
                .type(ElementMatchers.isSubTypeOf(Statement.class))
                .transform((builder, type, classLoader, module, protectionDomain) -> builder // Explicit parameter name
                        .method(ElementMatchers.isOverriddenFrom(Statement.class)
                                .and(ElementMatchers.returns(ResultSet.class))
                                .and(ElementMatchers.takesArgument(0, String.class))
                                .and(ElementMatchers.named("executeQuery")))
                        .intercept(MethodDelegation.to(DBInterceptor.class)))
                .installOnByteBuddyAgent();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}