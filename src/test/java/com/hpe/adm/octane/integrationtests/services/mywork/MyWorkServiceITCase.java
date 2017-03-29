package com.hpe.adm.octane.integrationtests.services.mywork;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.services.di.ServiceModule;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.mywork.MyWorkService;
import com.hpe.adm.octane.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.services.util.EntityUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class MyWorkServiceITCase {

    private static ServiceModule chelsea = new ServiceModule(new BasicConnectionSettingProvider(HardcodedServerUtil.chelsea));
    private static ServiceModule dynamo = new ServiceModule(new BasicConnectionSettingProvider(HardcodedServerUtil.dynamo));
    private static ServiceModule everton = new ServiceModule(new BasicConnectionSettingProvider(HardcodedServerUtil.everton));

    private static MyWorkService chelseaMyWork = chelsea.getInstance(MyWorkService.class);
    private static MyWorkService dynamoMyWork = dynamo.getInstance(MyWorkService.class);
    private static MyWorkService evertonMyWork = everton.getInstance(MyWorkService.class);

    @Test
    public void test() {
        System.out.println("12.53.13.23".compareTo("12.53.13"));
    }


    @Test
    public void areServersUp() {
        System.out.println(chelsea.getInstance(OctaneVersionService.class).getOctaneVersion());
        System.out.println(dynamo.getInstance(OctaneVersionService.class).getOctaneVersion());
        System.out.println(everton.getInstance(OctaneVersionService.class).getOctaneVersion());
    }

//    @Test
//    public void testIsAddMyWorkSupported() {
//        chelsea.getOctane().getOctane().entityList("defects").get().execute();
//        everton.getOctane().getOctane().entityList("defects").get().execute();
//        dynamo.getOctane().getOctane().entityList("defects").get().execute();
//    }

    @Test
    public void testIsAddToMyWorkSupported() {
        assertEquals(false, chelseaMyWork.isAddingToMyWorkSupported());
        assertEquals(true, dynamoMyWork.isAddingToMyWorkSupported());
        assertEquals(true, evertonMyWork.isAddingToMyWorkSupported());
    }

    @Test
    public void testGetMyWork() {
//        getMyWork(chelsea);
        //getMyWork(dynamo);
        getMyWork(everton);
//        getMyWork(center);
    }

    private void getMyWork(ServiceModule module) {

        MyWorkService myWorkService = module.getInstance(MyWorkService.class);

        EntityGenerator entityGenerator = new EntityGenerator(module.getOctane());

        EntityModel newEntityModel = entityGenerator.createEntityModel(Entity.USER_STORY);

        if (myWorkService.isAddingToMyWorkSupported()) {
            myWorkService.addToMyWork(newEntityModel);
        }

        Collection<EntityModel> myWork = myWorkService.getMyWork();
        printEntities(myWork);

        if (myWorkService.isAddingToMyWorkSupported()) {
            Assert.assertTrue(EntityUtil.containsEntityModel(myWork, newEntityModel));
        }
    }

    @Test
    public void dev() {
        Collection<EntityModel> myWork = dynamo.getInstance(MyWorkService.class).getMyWork();
        printEntities(myWork);

        Collection<EntityModel> myWork2 = everton.getInstance(MyWorkService.class).getMyWork();
        printEntities(myWork2);
    }

    //DEBUG
    private void printEntities(Collection<EntityModel> entities) {
        System.out.println("My Work Entities size: " + entities.size());
        if (entities.size() != 0) {
            String entitiesString = entities
                    .stream()
                    .map(em -> {
                        if (em.getValue("name") != null) {
                            return em.getValue("name").getValue().toString();
                        } else if (Entity.COMMENT == Entity.getEntityType(em)) {
                            return "(Comment)";
                        }
                        return "{???}";
                    })
                    .collect(Collectors.joining(", "));
            System.out.println("My Work Entities: " + entitiesString);
        }
    }

}
