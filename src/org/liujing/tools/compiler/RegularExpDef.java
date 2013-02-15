package org.liujing.tools.compiler;

import java.lang.annotation.*;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD})

public @interface RegularExpDef{
	String name();
}
