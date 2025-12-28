package ru.sinvic.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RequestTimingInterceptor implements HandlerInterceptor {

    private final RequestTimeInMemoryStorage requestTimeInMemoryStorage;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Instant startTime = Instant.now();
        request.setAttribute("startTime", startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Instant endTime = Instant.now();
        Instant startTime = (Instant) request.getAttribute("startTime");

        if (startTime != null) {
            requestTimeInMemoryStorage.addDuration(request.getRequestURI(), Duration.between(startTime, endTime));
        }
    }
}

