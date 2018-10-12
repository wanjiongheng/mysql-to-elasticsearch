package com.netopstec.annotation.es;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * es properties
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESField {

    String value() default "";
    /**
     * field is type in es
     * @return field type
     */
    String type() default "";

    /**
     * es property name
     * @return
     */
    String property() default "";

}
