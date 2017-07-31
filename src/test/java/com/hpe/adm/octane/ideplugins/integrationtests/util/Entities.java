package com.hpe.adm.octane.ideplugins.integrationtests.util;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entities {

    Entity[] requiredEntities();
}
