package com.google.ytd.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation applied to Command instances to indicate that they can be executed by non-admins.
 * If the annotation is not present, the web browser needs to be logged in as an admin user in
 * order to execute a command.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NonAdmin {

}