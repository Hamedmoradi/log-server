package logserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogApi {

    boolean param() default true;
    boolean result() default true;
    boolean costTime() default false;
    boolean exception() default true;
    long slowThresholdMills() default -1;
}