package com.hypherionmc.sdlink.core.jsondb.annotations;

import java.lang.annotation.*;

/**
 * @author HypherionSA
 * Marker annotation to mark database tables
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {
    String collection();
}