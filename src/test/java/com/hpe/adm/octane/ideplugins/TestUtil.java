package com.hpe.adm.octane.ideplugins;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;

import java.util.Collection;

public class TestUtil {
    public static void printEntities(Collection<EntityModel> entities) {
        System.out.println("Entities size: " + entities.size());
        if (entities.size() != 0) {
            entities
                    .stream()
                    .map(entityModel -> {
                        if(Entity.USER_ITEM == Entity.getEntityType(entityModel)){
                            return MyWorkUtil.getEntityModelFromUserItem(entityModel);
                        }
                        return entityModel;
                    })
                    .map(em -> {
                        if (em.getValue("name") != null) {
                            return em.getValue("name").getValue().toString();
                        } else if (Entity.COMMENT == Entity.getEntityType(em)) {
                            return "(Comment)";
                        }
                        return "{???}";
                    })
                    .forEach(System.out::println);
        }
    }
}