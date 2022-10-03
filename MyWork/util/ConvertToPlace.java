package uk.ac.warwick.cs126.util;

import uk.ac.warwick.cs126.interfaces.IConvertToPlace;
import uk.ac.warwick.cs126.models.Place;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.HashMap;

public class ConvertToPlace implements IConvertToPlace {
    /**
     * Use hashmapto store location of array as a value
     * This allows for efficient lookup time.
     * However the getPlaces array takes o(n)
     */
    private HashMap<String, Place> PlacesMap;
    private Place[] allPlaces;
    public ConvertToPlace() {
        // Initialise things here
        PlacesMap = new HashMap<>();
        allPlaces = this.getPlacesArray();
        for(int i=0; i<allPlaces.length; i++){
            float placeLat = allPlaces[i].getLatitude();
            float placeLong = allPlaces[i].getLongitude();
            String coordinates = String.valueOf(placeLat) + String.valueOf(placeLong);
            PlacesMap.add(coordinates, allPlaces[i]);//convert all the elements in the array to values
        }
    }

    public Place convert(float latitude, float longitude) {
        
        String targetCoordinates = String.valueOf(latitude) + String.valueOf(longitude);
        Place foundPlace = PlacesMap.get(targetCoordinates);//O(1) lookup time
        if(foundPlace == null){
            return new Place("", "", 0.0f, 0.0f);
        }

        return foundPlace;

    }

    public Place[] getPlacesArray() {
        Place[] placeArray = new Place[0];

        try {
            InputStream resource = ConvertToPlace.class.getResourceAsStream("/data/placeData.tsv");
            if (resource == null) {
                String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
                String resourcePath = Paths.get(currentPath, "data", "placeData.tsv").toString();
                File resourceFile = new File(resourcePath);
                resource = new FileInputStream(resourceFile);
            }

            byte[] inputStreamBytes = IOUtils.toByteArray(resource);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int lineCount = 0;
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (!("".equals(line))) {
                    lineCount++;
                }
            }
            lineReader.close();

            Place[] loadedPlaces = new Place[lineCount - 1];

            BufferedReader tsvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int placeCount = 0;
            String row;

            tsvReader.readLine();
            while ((row = tsvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split("\t");
                    Place place = new Place(
                            data[0],
                            data[1],
                            Float.parseFloat(data[2]),
                            Float.parseFloat(data[3]));
                    loadedPlaces[placeCount++] = place;
                }
            }
            tsvReader.close();

            placeArray = loadedPlaces;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return placeArray;
    }
}

