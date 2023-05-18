/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.junit.Test;

import static org.junit.Assert.*;

public class OctaneVersionUTCase {

    @Test
    public void testParsing() {
        String invalidVersionString = "12.55.3.peanutbutter";
        try {
            new OctaneVersion(invalidVersionString);
            fail("The version number "+invalidVersionString+" should be invalid");
        } catch (Exception ignored) {}

        invalidVersionString = "jelly.55.3.32";
        try {
            new OctaneVersion(invalidVersionString);
            fail("The version number "+invalidVersionString+" should be invalid");
        } catch (Exception ignored) {}

        invalidVersionString = "12.55.3.32.42";
        try {
            new OctaneVersion(invalidVersionString);
            fail("The version number "+invalidVersionString+" should be invalid");
        } catch (Exception ignored) {}

    }

    @Test
    public void testComparison() {
        OctaneVersion v1;
        OctaneVersion v2;

        v1 = new OctaneVersion("12.53.13.23");
        v2 = new OctaneVersion("12.53.13.23");

        assertEquals(v1, v2);
        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.EQ, v2));
        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.LOWER_EQ, v2));
        assertTrue(v1.isLessOrEqThan(v2));
        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.HIGHER_EQ, v2));
        assertTrue(v1.isMoreOrEqThan(v2));

        v1 = new OctaneVersion("12.53.13.23");
        v2 = new OctaneVersion("12.55.3.21321");

        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.LOWER, v2));
        assertTrue(v1.isLessThan(v2));
    }

}