package com.hpe.adm.octane.ideplugins.integrationtests.services.noentity;


import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.TestUtil;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.EntitySearchService;
import org.junit.Test;

public class EntitySearchServiceITCase extends IntegrationTestBase {

    @Inject
    private EntitySearchService searchService;

    @Test
    public void testGlobalSearch() {
        //Just make sure this doesn't blow up for now
        TestUtil.printEntities(searchService.searchGlobal("req", 25, Entity.REQUIREMENT));
    }
}
