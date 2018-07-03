package springroll.framework.annotation;

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
