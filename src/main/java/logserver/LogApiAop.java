package logserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@EnableAspectJAutoProxy
@Log4j2
public class LogApiAop {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Around("@annotation(logApi)")
    public Object around(ProceedingJoinPoint point, LogApi logApi) throws Throwable {
        Object result;
        Method method = getCurrentMethod(point);
        String methodName = method.getName();

        Map<String, Object> msg = new HashMap();

        try {

            final long startMills = System.currentTimeMillis();

            msg.put("method", methodName);
            msg.put("complete_name", point.toShortString());
            CodeSignature codeSignature = (CodeSignature) point.getSignature();
            msg.put("args_name", codeSignature.getParameterNames());
            msg.put("args_value", point.getArgs());

            log.info("{} param is {}.", methodName, Arrays.toString(point.getArgs()));

            result = point.proceed();

            msg.put("result", result);

            if (logApi.result()) {
                log.info("{} result is {}.", methodName, result);
            }
            final long slowThreshold = logApi.slowThresholdMills();
            if (logApi.costTime() || slowThreshold >= 0) {
                final long endMills = System.currentTimeMillis();
                long costTime = endMills - startMills;
                if (logApi.costTime()) {
                    log.debug("{} cost time is {}ms.", methodName, costTime);
                }
                if (slowThreshold >= 0 && costTime >= slowThreshold) {
                    log.debug("{} is slow log, {}ms >= {}ms.", methodName, costTime,  slowThreshold);
                }
            }
        } catch (Throwable e) {
            if (logApi.exception()) {
                log.debug("{} meet ex.", methodName);
            }
            throw e;
        }
        kafkaTemplate.send("registered", new ObjectMapper().writeValueAsString(msg));
        return result;
    }

    private Method getCurrentMethod(ProceedingJoinPoint point) throws Exception {
        try {
            Signature sig = point.getSignature();
            MethodSignature msig = (MethodSignature) sig;
            Object target = point.getTarget();
            return target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new Exception(e);
        }
    }

}
