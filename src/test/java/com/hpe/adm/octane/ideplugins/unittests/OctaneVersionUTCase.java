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
package com.hpe.adm.octane.ideplugins.unittests;

import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

        assertTrue(v1.equals(v2));
        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.EQ, v2));
        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.LOWER_EQ, v2));
        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.HIGHER_EQ, v2));

        v1 = new OctaneVersion("12.53.13.23");
        v2 = new OctaneVersion("12.55.3.21321");

        assertTrue(OctaneVersion.compare(v1, OctaneVersion.Operation.LOWER, v2));
    }

}