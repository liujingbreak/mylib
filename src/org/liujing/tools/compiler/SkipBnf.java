package org.liujing.tools.compiler;

import java.lang.annotation.*;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface SkipBnf{
	String[] value();
}

