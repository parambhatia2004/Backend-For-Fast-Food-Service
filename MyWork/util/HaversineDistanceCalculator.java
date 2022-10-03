package uk.ac.warwick.cs126.util;

public class HaversineDistanceCalculator {

    private final static float R = 6372.8f;
    private final static float kilometresInAMile = 1.609344f;

    public static float inKilometres(float lat1, float lon1, float lat2, float lon2) {
        // TODO
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double a = (Math.pow((Math.sin((lat2Rad-lat1Rad)/2)), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow((Math.sin((lon2Rad-lon1Rad)/2)), 2));
        double c = 2 * (Math.asin(Math.pow(a, 0.5)));
        double d = (R * c);
        float finalResult = (float) (Math.round(d * 10.0)/10.0);
        return finalResult;
    }

    public static float inMiles(float lat1, float lon1, float lat2, float lon2) {
        // TODO
        float radius = 6372.8f;
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double a = (Math.pow((Math.sin((lat2Rad-lat1Rad)/2)), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow((Math.sin((lon2Rad-lon1Rad)/2)), 2));
        double c = 2 * (Math.asin(Math.pow(a, 0.5)));
        double d = R * c;
        double mileValue = d/kilometresInAMile;
        float finalResult = (float) (Math.round(mileValue * 10.0)/10.0);
        return finalResult;
    }

}
