package org.liujing.tools.compiler;

import java.lang.annotation.*;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.PARAMETER})
public @interface Variable{
	String name();
	//Object value() default null;
}
