package ru.sinvic.server.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RequestTimingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_REQUEST_ATTRIBUTE_NAME = "startTime";
    private final RequestTimeInMemoryStorage requestTimeInMemoryStorage;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        Instant startTime = Instant.now();
        request.setAttribute(START_TIME_REQUEST_ATTRIBUTE_NAME, startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        Instant startTime = (Instant) request.getAttribute(START_TIME_REQUEST_ATTRIBUTE_NAME);

        if (startTime != null) {
            Instant endTime = Instant.now();
            requestTimeInMemoryStorage.addDuration(request.getRequestURI(), Duration.between(startTime, endTime));
        }
    }
}

