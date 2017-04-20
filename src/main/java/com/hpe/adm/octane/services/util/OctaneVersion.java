package com.hpe.adm.octane.services.util;

public class OctaneVersion implements Comparable<OctaneVersion> {

    public static final OctaneVersion CHELSEA = new OctaneVersion("12.53.16");
    public static final OctaneVersion DYNAMO = new OctaneVersion("12.53.20");
    public static final OctaneVersion EVERTON_P1 = new OctaneVersion("12.53.21");
    public static final OctaneVersion EVERTON_P2 = new OctaneVersion("12.53.22");

    private String almVersion;
    private Integer octaneVersion;
    private Integer buildNumber;

    public OctaneVersion(String versionString){
        String[] parts = versionString.split("\\.");

        if(!(parts.length == 3 || parts.length == 4)){
            throw new RuntimeException("Unable to parse octane version from string: " + versionString);
        }

        //The first two parts are from ALM version
        almVersion = parts[0] + "." + parts[1];

        try {
            //Even though we don't use it, still good to test
            Integer.parseInt(parts[0]);
            Integer.parseInt(parts[1]);

            octaneVersion = Integer.parseInt(parts[2]);

            if(parts.length == 4){
                buildNumber = Integer.parseInt(parts[3]);
            }
        } catch (NumberFormatException ex){
            throw new RuntimeException("Unable to parse octane version from string: " + versionString, ex);
        }
    }

    public String getAlmVersion() {
        return almVersion;
    }

    public Integer getOctaneVersion() {
        return octaneVersion;
    }

    public Integer getBuildNumber(){
        return buildNumber;
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
        if(compareAlmVersion != 0){
            return compareAlmVersion;
        }

        int compareOctaneVersion = this.octaneVersion.compareTo(octaneVersion.getOctaneVersion());
        if(compareOctaneVersion != 0){
            return compareOctaneVersion;
        }

        return nullSafeComparator(buildNumber, octaneVersion.buildNumber);
    }

    public enum Operation{
        LOWER, LOWER_EQ, EQ, HIGHER_EQ, HIGHER
    }

    public static boolean compare(OctaneVersion v1, Operation op, OctaneVersion v2){
        int comparison = v1.compareTo(v2);
        if(comparison <  0 && Operation.LOWER == op) return true;
        if(comparison <= 0 && Operation.LOWER_EQ == op) return true;
        if(comparison == 0 && Operation.EQ == op) return true;
        if(comparison >= 0 && Operation.HIGHER_EQ == op) return true;
        if(comparison >  0 && Operation.HIGHER == op) return true;
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OctaneVersion{");
        sb.append(almVersion);
        sb.append(".");
        sb.append(octaneVersion);
        if(buildNumber != null) {
            sb.append(".");
            sb.append(buildNumber);
        }
        sb.append("}");
        return sb.toString();
    }

    public String getVersionString(){
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

    public void discardBuildNumber(){
        buildNumber = null;
    }
}
