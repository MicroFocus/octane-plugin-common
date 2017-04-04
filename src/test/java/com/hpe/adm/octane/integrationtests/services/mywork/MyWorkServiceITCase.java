package com.hpe.adm.octane.integrationtests.services.mywork;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.services.di.ServiceModule;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.mywork.MyWorkService;
import com.hpe.adm.octane.services.mywork.MyWorkUtil;
import com.hpe.adm.octane.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.services.util.EntityUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * TODO: The test framework need lots improvement
 */
@Ignore
public class MyWorkServiceITCase {

    private static ServiceModule chelsea = new ServiceModule(new BasicConnectionSettingProvider(HardcodedServerUtil.chelsea));
    private static ServiceModule dynamo = new ServiceModule(new BasicConnectionSettingProvider(HardcodedServerUtil.dynamo));
    private static ServiceModule everton21 = new ServiceModule(new BasicConnectionSettingProvider(HardcodedServerUtil.everton21));
    private static ServiceModule everton22 = new ServiceModule(new BasicConnectionSettingProvider(HardcodedServerUtil.everton22));

    private static ServiceModule[] serviceModules = new ServiceModule[]{chelsea, dynamo, everton21, everton22};

    private boolean isServerUp(ServiceModule module) {
        try {
            module.getInstance(OctaneVersionService.class).getOctaneVersion();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Test
    public void testGetMyWork() {
        Arrays.stream(serviceModules).filter(this::isServerUp).forEach(this::getMyWork);
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

        myWork = MyWorkUtil.getEntityModelsFromUserItems(myWork);

        if (!myWorkService.isAddingToMyWorkSupported()) {
            return;
        }

        Assert.assertTrue(EntityUtil.containsEntityModel(myWork, newEntityModel));

        myWorkService.removeFromMyWork(newEntityModel);

        myWork = myWorkService.getMyWork();
        printEntities(myWork);
        Assert.assertFalse(EntityUtil.containsEntityModel(myWork, newEntityModel));
    }

    @Test
    public void testIsAddToMyWorkSupported() {
        if (isServerUp(chelsea)){
            Assert.assertFalse(chelsea.getInstance(MyWorkService.class).isAddingToMyWorkSupported());
        }
        if (isServerUp(dynamo)){
            Assert.assertFalse(dynamo.getInstance(MyWorkService.class).isAddingToMyWorkSupported());
        }
        if (isServerUp(everton21)){
            Assert.assertFalse(everton21.getInstance(MyWorkService.class).isAddingToMyWorkSupported());
        }
        if (isServerUp(everton22)){
            Assert.assertFalse(everton22.getInstance(MyWorkService.class).isAddingToMyWorkSupported());
        }
    }

    //DEBUG
    private void printEntities(Collection<EntityModel> entities) {
        System.out.println("My Work Entities size: " + entities.size());
        if (entities.size() != 0) {
            String entitiesString = entities
                    .stream()
                    .map(MyWorkUtil::getEntityModelFromUserItem)
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
