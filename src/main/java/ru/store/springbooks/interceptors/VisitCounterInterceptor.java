package ru.store.springbooks.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import ru.store.springbooks.service.VisitCounterService;

@Component
@RequiredArgsConstructor
public class VisitCounterInterceptor implements HandlerInterceptor {
    private final VisitCounterService visitCounterService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           ModelAndView modelAndView) {
        String url = request.getRequestURI();
        visitCounterService.incrementVisitCount(url);
    }
}