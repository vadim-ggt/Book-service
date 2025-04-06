package ru.store.springbooks.aspects;


import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {


    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);


    @Pointcut("execution(* ru.store.springbooks.service.*.*(..))")
    public void allServiceMethods() {}


    @Pointcut("@annotation(ru.store.springbooks.aspects.AspectAnnotation)")
    public void annotatedMethods() {}

    @Before("allServiceMethods() || annotatedMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        if (shouldLog(methodName)) {
            LOGGER.info(">> {}() - args: {}", methodName, Arrays.toString(args));
        }
    }


    @AfterReturning(pointcut = "allServiceMethods() || annotatedMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        LOGGER.info("<< {}() - result: {}", methodName, result);
    }


    @AfterThrowing(pointcut = "allServiceMethods() || annotatedMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        LOGGER.error("!! {}() - exception: {}", methodName, exception.getMessage());
    }


    private boolean shouldLog(String methodName) {
        return methodName.startsWith("update")
                || methodName.startsWith("create")
                || methodName.startsWith("delete")
                || methodName.startsWith("save");
    }


}
