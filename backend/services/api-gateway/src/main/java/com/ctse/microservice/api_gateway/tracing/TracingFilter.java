package com.ctse.microservice.api_gateway.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class TracingFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private final ObservationRegistry registry;

    @Override
    @NonNull
    public ServerResponse filter(@NonNull ServerRequest request,
                                 @NonNull HandlerFunction<ServerResponse> next) throws Exception {

        String path = request.path();
        String method = request.method().name();

        if (path.startsWith("/actuator/prometheus")) {
            return next.handle(request); // Skip metrics
        }

        log.info("[TracingFilter] Tracing: {} {}", method, path);

        return Objects.requireNonNull(
                Observation.createNotStarted("http.request", registry)
                        .contextualName(method + " " + path)
                        .lowCardinalityKeyValue("http.method", method)
                        .lowCardinalityKeyValue("http.path", path)
                        .observe(() -> {
                            try {
                                return next.handle(request);
                            } catch (Exception e) {
                                log.error("Error while handling request: {}", path, e);
                                throw new RuntimeException(e);
                            }
                        })
        );
    }
}
