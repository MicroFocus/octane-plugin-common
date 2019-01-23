package com.hpe.adm.octane.ideplugins.integrationtests.util;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used on the classes that extend the IntegrationTestBase class
 * the parameter clean specifies if the environment needs a clean workspace or not
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WorkSpace {
        boolean clean();
}
