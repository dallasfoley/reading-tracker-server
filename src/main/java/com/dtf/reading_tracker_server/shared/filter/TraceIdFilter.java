package com.dtf.reading_tracker_server.shared.filter;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
class TraceIdFilter extends OncePerRequestFilter {

    private final ObjectProvider<Tracer> tracer;

    TraceIdFilter(ObjectProvider<Tracer> tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceId = getTraceId();
        if (traceId != null) {
            response.setHeader("X-Trace-Id", traceId);
        }
        filterChain.doFilter(request, response);
    }

    private @Nullable String getTraceId() {
        return Optional.ofNullable(this.tracer.getIfAvailable())
                .map(Tracer::currentTraceContext)
                .map(currentTraceContext -> currentTraceContext.context())
                .map(TraceContext::traceId)
                .orElse(null);
    }

}


