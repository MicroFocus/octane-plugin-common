package com.hpe.adm.octane.integrationtests.services.noentity;


import com.google.inject.Inject;
import com.hpe.adm.octane.TestUtil;
import com.hpe.adm.octane.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.nonentity.EntitySearchService;
import org.junit.Test;

public class EntitySearchServiceITCase extends IntegrationTestBase {

    @Inject
    private EntitySearchService searchService;

    @Test
    public void testGlobalSearch() {
        //Just make sure this doesn't blow up for now
        TestUtil.printEntities(searchService.searchGlobal("user", 25, Entity.DEFECT, Entity.USER_STORY, Entity.AUTOMATED_TEST));
    }
}
