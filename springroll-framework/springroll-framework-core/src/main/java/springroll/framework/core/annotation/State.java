package springroll.framework.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface State {

    String BEGINNING = "";
    String ALL = "*";

    String[] value() default "";

}
