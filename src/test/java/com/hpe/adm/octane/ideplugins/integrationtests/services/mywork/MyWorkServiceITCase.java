/*
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpe.adm.octane.ideplugins.integrationtests.services.mywork;


import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityGenerator;
import com.hpe.adm.octane.ideplugins.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.EntityUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static com.hpe.adm.octane.ideplugins.TestUtil.printEntities;

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



}
