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
package com.hpe.adm.octane.ideplugins.services.util;

public class OctaneVersion implements Comparable<OctaneVersion> {

    public static final OctaneVersion CHELSEA = new OctaneVersion("12.53.16");
    public static final OctaneVersion DYNAMO = new OctaneVersion("12.53.20");
    public static final OctaneVersion EVERTON_P1 = new OctaneVersion("12.53.21");
    public static final OctaneVersion EVERTON_P2 = new OctaneVersion("12.53.22");
    public static final OctaneVersion EVERTON_P3 = new OctaneVersion("12.55.3");
    public static final OctaneVersion FENER_P1 = new OctaneVersion("12.55.5");
    public static final OctaneVersion FENER_P2 = new OctaneVersion("12.55.6");
    public static final OctaneVersion FENER_P3 = new OctaneVersion("12.55.7");
    public static final OctaneVersion GENT_P3 = new OctaneVersion("12.55.12");
    public static final OctaneVersion INTER_P2 = new OctaneVersion("12.60.16");
    public static final OctaneVersion JUVENTUS_P3 = new OctaneVersion("12.60.35");
    public static final OctaneVersion LIVERPOOL_P0 = new OctaneVersion("12.60.36");
    public static final OctaneVersion COLDPLAY_P1 = new OctaneVersion("15.1.4");
    public static final OctaneVersion IRONMAIDEN_P1 = new OctaneVersion("16.0.208");

    private String almVersion;
    private Integer octaneVersion;
    private Integer buildNumber;

    public OctaneVersion(String versionString) {
        String[] parts = versionString.split("\\.");

        if (!(parts.length == 3 || parts.length == 4)) {
            throw new RuntimeException("Unable to parse Core Software Delivery Platform version from string: " + versionString);
        }

        //The first two parts are from ALM version
        almVersion = parts[0] + "." + parts[1];

        try {
            //Even though we don't use it, still good to test
            Integer.parseInt(parts[0]);
            Integer.parseInt(parts[1]);

            octaneVersion = Integer.parseInt(parts[2]);

            if (parts.length == 4) {
                buildNumber = Integer.parseInt(parts[3]);
            }
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Unable to parse Core Software Delivery Platform version from string: " + versionString, ex);
        }
    }

    public String getAlmVersion() {
        return almVersion;
    }

    public Integer getOctaneVersion() {
        return octaneVersion;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    public boolean isLessThan(OctaneVersion anotherVersions) {
        return compareTo(anotherVersions) < 0;
    }

    public boolean isMoreThan(OctaneVersion anotherVersions) {
        return compareTo(anotherVersions) > 0;
    }

    public boolean isLessOrEqThan(OctaneVersion anotherVersions) {
        return compareTo(anotherVersions) <= 0;
    }

    public boolean isMoreOrEqThan(OctaneVersion anotherVersions) {
        return compareTo(anotherVersions) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OctaneVersion that = (OctaneVersion) o;

        if (almVersion != null ? !almVersion.equals(that.almVersion) : that.almVersion != null) return false;
        if (octaneVersion != null ? !octaneVersion.equals(that.octaneVersion) : that.octaneVersion != null)
            return false;
        return buildNumber != null ? buildNumber.equals(that.buildNumber) : that.buildNumber == null;
    }

    @Override
    public int hashCode() {
        int result = almVersion != null ? almVersion.hashCode() : 0;
        result = 31 * result + (octaneVersion != null ? octaneVersion.hashCode() : 0);
        result = 31 * result + (buildNumber != null ? buildNumber.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(OctaneVersion octaneVersion) {
        if (this == octaneVersion) return 0;

        int compareAlmVersion = this.almVersion.compareTo(octaneVersion.getAlmVersion());
        if (compareAlmVersion != 0) {
            return compareAlmVersion;
        }

        int compareOctaneVersion = this.octaneVersion.compareTo(octaneVersion.getOctaneVersion());
        if (compareOctaneVersion != 0) {
            return compareOctaneVersion;
        }

        return nullSafeComparator(buildNumber, octaneVersion.buildNumber);
    }


    public enum Operation {
        LOWER, LOWER_EQ, EQ, HIGHER_EQ, HIGHER;

    }

    public static boolean isBetween(OctaneVersion version,OctaneVersion lowerLimit,OctaneVersion upperLimit) {
        return isBetween(version, lowerLimit, upperLimit, true);
    }

    public static boolean isBetween(OctaneVersion version,OctaneVersion lowerLimit,OctaneVersion upperLimit, boolean inclusive) {
        int comparisonWithLower = version.compareTo(lowerLimit);
        int comparisonWithUpper = version.compareTo(upperLimit);

        //check if the version is either of the limits if the comparison is inclusive
        if((comparisonWithLower == 0 || comparisonWithUpper == 0) && inclusive)
            return true;

        if(comparisonWithLower >= 0 && comparisonWithUpper <= 0) return true;

        return false;
    }

    public static boolean compare(OctaneVersion v1, Operation op, OctaneVersion v2) {
        int comparison = v1.compareTo(v2);
        if (comparison < 0 && Operation.LOWER == op) return true;
        if (comparison <= 0 && Operation.LOWER_EQ == op) return true;
        if (comparison == 0 && Operation.EQ == op) return true;
        if (comparison >= 0 && Operation.HIGHER_EQ == op) return true;
        if (comparison > 0 && Operation.HIGHER == op) return true;
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OctaneVersion{");
        sb.append(almVersion);
        sb.append(".");
        sb.append(octaneVersion);
        if (buildNumber != null) {
            sb.append(".");
            sb.append(buildNumber);
        }
        sb.append("}");
        return sb.toString();
    }

    public String getVersionString() {
        return almVersion + "." + octaneVersion;
    }

    private int nullSafeComparator(final Comparable one, final Comparable two) {
        if (one == null && two == null) {
            return 0;
        }
        if (one == null || two == null) {
            return (one == null) ? -1 : 1;
        }
        return one.compareTo(two);
    }

    public void discardBuildNumber() {
        buildNumber = null;
    }
}
