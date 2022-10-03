package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IRestaurantStore;
import uk.ac.warwick.cs126.models.Cuisine;
import uk.ac.warwick.cs126.models.EstablishmentType;
import uk.ac.warwick.cs126.models.Place;
import uk.ac.warwick.cs126.models.PriceRange;
import uk.ac.warwick.cs126.models.Restaurant;
import uk.ac.warwick.cs126.models.RestaurantDistance;
import java.util.Date;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyArrayList;
import uk.ac.warwick.cs126.structures.HashMap;

import uk.ac.warwick.cs126.util.ConvertToPlace;
import uk.ac.warwick.cs126.util.HaversineDistanceCalculator;
import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.StringFormatter;

public class RestaurantStore implements IRestaurantStore {

    /**
     * Here I declare and initialise all classes used in the file.
     * Design Decision to initialise all immediately rather than in individual methods
     * This will help with efficiency, as only the initial time will be longer, but faster usage.
     */
    private MyArrayList<Restaurant> restaurantArray;
    private DataChecker dataChecker;
    private HaversineDistanceCalculator distanceCalculator;
    private HashMap<String, String> BlacklistedRestaurant;
    private MyArrayList<Restaurant> matchingWarwickStars;
    private StringFormatter stringFormatter;
    private ConvertToPlace convertToPlace;
    private MyArrayList<Restaurant> restaurantMatchingQuery;

    public RestaurantStore() {
        // Initialise variables here
        distanceCalculator = new HaversineDistanceCalculator();
        BlacklistedRestaurant = new HashMap<>();
        restaurantArray = new MyArrayList<>();
        dataChecker = new DataChecker();
        matchingWarwickStars = new MyArrayList<>();
        stringFormatter = new StringFormatter();
        restaurantMatchingQuery = new MyArrayList<>();
        convertToPlace = new ConvertToPlace();
    }

    public Restaurant[] loadRestaurantDataToArray(InputStream resource) {
        Restaurant[] restaurantArray = new Restaurant[0];

        try {
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

            Restaurant[] loadedRestaurants = new Restaurant[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            String row;
            int restaurantCount = 0;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");

                    Restaurant restaurant = new Restaurant(
                            data[0],
                            data[1],
                            data[2],
                            data[3],
                            Cuisine.valueOf(data[4]),
                            EstablishmentType.valueOf(data[5]),
                            PriceRange.valueOf(data[6]),
                            formatter.parse(data[7]),
                            Float.parseFloat(data[8]),
                            Float.parseFloat(data[9]),
                            Boolean.parseBoolean(data[10]),
                            Boolean.parseBoolean(data[11]),
                            Boolean.parseBoolean(data[12]),
                            Boolean.parseBoolean(data[13]),
                            Boolean.parseBoolean(data[14]),
                            Boolean.parseBoolean(data[15]),
                            formatter.parse(data[16]),
                            Integer.parseInt(data[17]),
                            Integer.parseInt(data[18]));

                    loadedRestaurants[restaurantCount++] = restaurant;
                }
            }
            csvReader.close();

            restaurantArray = loadedRestaurants;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return restaurantArray;
    }

    /**
     * Check if restaurant is valid restaurant
     * Check if restaurant has been blacklisted
     * Check if a restaurant with same ID is in the store, if so then do not add, blacklist
     * If all checks are passed, add the restaurant to store, and set id of the restaurant by extractID
     * @param restaurant        to be added
     * @return                  result
     */
    public boolean addRestaurant(Restaurant restaurant) {
        // TODO
        if(restaurant.getRepeatedID() == null){
            return false;
        }
        if(dataChecker.isValid(restaurant) == false){
            return false;
        }
        Long restaurantID = dataChecker.extractTrueID(restaurant.getRepeatedID());
        String currentStringID = String.valueOf(restaurantID);
        if(BlacklistedRestaurant.contains(currentStringID) == true){
            return false;
        }

        for(int j = 0; j<restaurantArray.size(); j++){
            if(restaurantID.equals(restaurantArray.get(j).getID())){
                BlacklistedRestaurant.add(currentStringID, currentStringID);
                restaurantArray.remove(restaurantArray.get(j));
                return false;
            }
        }

        restaurantArray.add(restaurant);
        restaurant.setID(restaurantID);//need to set id otherwise it is default
        return true;
    }


    /**
     * Check if the input array is valid
     * try to add each element of array to the store
     * If even one restaurant is not added, return false
     * @param restaurants       array of restaurant objects
     * @return                  status
     */
    public boolean addRestaurant(Restaurant[] restaurants) {
        if(restaurants == null || restaurants.length == 0){
            return false;
        }
        boolean allAdded = true;
        for(int i = 0; i<restaurants.length; i++){
            if(this.addRestaurant(restaurants[i]) == false){
                allAdded = false;
            }
        }
        return allAdded;
    }


    /**
     * Get corrsepondig restaurant from array
     * If it doesn't exist then return null
     * @param id        Id to serach for
     * @return          Status of search
     */
    public Restaurant getRestaurant(Long id) {
        for(int i = 0; i<restaurantArray.size(); i++){
            if(id.equals(restaurantArray.get(i).getID())){
                return restaurantArray.get(i);
            }
        }
        return null;
    }


    /**
     * Converts restaurantStore arraylist into an array, so that it can be passed to next method
     * Obtains an array of all the restaurants in store sorted by ID
     * @return
     */
    public Restaurant[] getRestaurants() {
        Restaurant restaurants[] = new Restaurant[restaurantArray.size()];
        for(int i = 0; i<restaurantArray.size(); i++){
            restaurants[i] = restaurantArray.get(i);
        }
        return getRestaurants(restaurants);
    }

    /**
     * Sorts input array by calling the quickSort method
     * Calls quicksort method which takes O(nlogn) time
     *
     * @param restaurants   array to be sorted by ascending ID
     * @return              sorted array
     */
    public Restaurant[] getRestaurants(Restaurant[] restaurants) {
        for(int i = 0; i<restaurants.length; i++){
            //validation takes O(n)
            if(dataChecker.isValid(restaurants[i]) == false){
                return new Restaurant[0];
            }
        }
        
        quickSort(restaurants, 0, restaurants.length-1);
        return restaurants;
    }

    /**
     * This sort method is quickSort, which breaks down the input array into smaller arrays recursively
     * The smaller arrays are then sorted, and join together to form the overall sorted array
     * Only continue if elements are not sorted.
     * This method can be considered to have O(1) time complexity since it takes constant time to call function.
     * @param inputArray
     * @param indexStart
     * @param end
     */
    public void quickSort(Restaurant inputArray[], int indexStart, int end) {
        if (indexStart < end) {
            int partitionIndex = partition(inputArray, indexStart, end);

            quickSort(inputArray, indexStart, partitionIndex-1);
            quickSort(inputArray, partitionIndex+1, end);
        }
    }

    /**
     * Using the last elements ID, compare all restaurants(in input array) ID to the last element's ID.
     * If it is lower than the last element, then swap the elements
     * @param favouriteArray     Input Array to sort
     * @param first             Index to start sorting by
     * @param end               Inde x to finish sorting by
     * @return                  Index to further split the array
     */
    private int partition(Restaurant restaurantArr[], int first, int end) {
        long pivot = restaurantArr[end].getID();
        int i = first-1;

        for (int j = first; j < end; j++) {
            if (restaurantArr[j].getID() <= pivot) {
                i++;
                Restaurant tempRestaurant = restaurantArr[i];
                restaurantArr[i] = restaurantArr[j];
                restaurantArr[j] = tempRestaurant;
            }
        }

        Restaurant tempRestaurantTwo = restaurantArr[i+1];
        restaurantArr[i+1] = restaurantArr[end];//swap last element
        restaurantArr[end] = tempRestaurantTwo;

        return i+1;
    }


    /**
     * Similar to quickSort, but used to sort by string
     * @param inputArray
     * @param indexStart
     * @param end
     */
    public void quickSortString(Restaurant inputArray[], int indexStart, int end){
        if (indexStart < end) {
            int partitionIndex = partitionString(inputArray, indexStart, end);

            quickSortString(inputArray, indexStart, partitionIndex-1);
            quickSortString(inputArray, partitionIndex+1, end);
        }
    }


    /**
     * similar to quicksort, however it sorts by date first, then name, then ID
     * @param inputArray
     * @param indexStart
     * @param end
     */
    public void quickSortDate(Restaurant inputArray[], int indexStart, int end){
        if (indexStart < end) {
            int partitionIndex = partitionDate(inputArray, indexStart, end);

            quickSortDate(inputArray, indexStart, partitionIndex-1);
            quickSortDate(inputArray, partitionIndex+1, end);
        }
    }

    /**
     * This method takes in an array of restaurants, and sorts them by comparing the date established
     * If this is the same for any two restaurants, then compare the names alphabetically.
     * If this is also same, then compare by ID uniquely.
     * @param restArray
     * @param first
     * @param end
     * @return
     */
    private int partitionDate(Restaurant restArray[], int first, int end) {
        Date pivot = restArray[end].getDateEstablished();
        int i = (first-1);

        for (int j = first; j < end; j++) {
            if (restArray[j].getDateEstablished().compareTo(pivot) < 0) {
                i++;

                Restaurant restTemp = restArray[i];
                restArray[i] = restArray[j];
                restArray[j] = restTemp;
            }
            if(restArray[j].getDateEstablished().compareTo(pivot) == 0){
                if (restArray[j].getName().compareToIgnoreCase(restArray[end].getName()) < 0) {
                    i++;

                    Restaurant restTemp = restArray[i];
                    restArray[i] = restArray[j];
                    restArray[j] = restTemp;
                }
                if(restArray[j].getName().compareToIgnoreCase(restArray[end].getName()) == 0){
                    long pivotID = restArray[end].getID();
                    if (restArray[j].getID() <= pivotID) {
                        i++;
                        Restaurant tempRestaurantTwo = restArray[i];
                        restArray[i] = restArray[j];
                        restArray[j] = tempRestaurantTwo;
                    }
                }
            }
        }
        Restaurant tempRestaurantThree = restArray[i+1];
        restArray[i+1] = restArray[end];
        restArray[end] = tempRestaurantThree;

        return i+1;
    }

    /**
     * This method sorts the input array of restaurants by the name, alphabetically
     * It ignores the casing
     * If the name is alphabetically identical, then sort by ID
     * @param restArray
     * @param first
     * @param end
     * @return
     */
    private int partitionString(Restaurant restArray[], int first, int end) {
        String pivot = restArray[end].getName();
        int i = (first-1);

        for (int j = first; j < end; j++) {
            if (restArray[j].getName().compareToIgnoreCase(pivot) < 0) {
                i++;

                Restaurant restTemp = restArray[i];
                restArray[i] = restArray[j];
                restArray[j] = restTemp;
            }
            if(restArray[j].getName().compareToIgnoreCase(pivot) == 0){
                long pivotID = restArray[end].getID();
                if (restArray[j].getID() <= pivotID) {
                    i++;
                    Restaurant tempRestaurantTwo = restArray[i];
                    restArray[i] = restArray[j];
                    restArray[j] = tempRestaurantTwo;
                }
            }
        }
        Restaurant tempRestaurantThree = restArray[i+1];
        restArray[i+1] = restArray[end];
        restArray[end] = tempRestaurantThree;

        return i+1;
    }

    /**
     * This method sorts all the restaurants alphabetically
     * Converts the restaurant store to array.
     * @return
     */
    public Restaurant[] getRestaurantsByName() {
        // TODO
        Restaurant restaurants[] = new Restaurant[restaurantArray.size()];
        for(int i = 0; i<restaurantArray.size(); i++){
            restaurants[i] = restaurantArray.get(i);
        }
        quickSortString(restaurants, 0,restaurants.length -1);
        return restaurants;
    }

    /**
     * Returns the restaurants in the store by Date
     * @return
     */
    public Restaurant[] getRestaurantsByDateEstablished() {
        // TODO
        Restaurant restaurants[] = new Restaurant[restaurantArray.size()];
        for(int i = 0; i<restaurantArray.size(); i++){
            restaurants[i] = restaurantArray.get(i);
        }
        return getRestaurantsByDateEstablished(restaurants);
    }

    public Restaurant[] getRestaurantsByDateEstablished(Restaurant[] restaurants) {
        // TODO
        for(int i = 0; i<restaurants.length; i++){
            //validation takes O(n)
            if(dataChecker.isValid(restaurants[i]) == false){
                return new Restaurant[0];
            }
        }
        quickSortDate(restaurants, 0,restaurants.length -1);
        return restaurants;
    }


    /**
     * This method uses an arraylist to store all the restaurants with at least one warwick star
     * This is because the size will be indeterminate.
     * Calls quick sort method to sort by the number of stars decreasing.
     * @return      array of restaurants with at leat one WarwickStar, in decreasing order of stars.
     */
    public Restaurant[] getRestaurantsByWarwickStars() {
        // TODO
        for(int i = 0; i<restaurantArray.size(); i++){

            if(restaurantArray.get(i).getWarwickStars() > 0){
                matchingWarwickStars.add(restaurantArray.get(i));//cannot use array as we do not know size
            }
        }
        Restaurant[] restaurantsWithWarwickStars = new Restaurant[matchingWarwickStars.size()];
        for(int i = 0; i<matchingWarwickStars.size(); i++){
            restaurantsWithWarwickStars[i] = matchingWarwickStars.get(i);
        }
        quickSortStars(restaurantsWithWarwickStars, 0, restaurantsWithWarwickStars.length-1);
        return restaurantsWithWarwickStars;
    }

    /**
     * sorts the input array by number of warwickStars recursively
     * @param inputArray
     * @param indexStart
     * @param end
     */
    public void quickSortStars(Restaurant inputArray[], int indexStart, int end){
        if (indexStart < end) {
            int partitionIndex = partitionStars(inputArray, indexStart, end);

            quickSortStars(inputArray, indexStart, partitionIndex-1);
            quickSortStars(inputArray, partitionIndex+1, end);
        }
    }

    /**
     * This method sorts by the number of stars for each restaurant in input array
     * If stars is the same, compare by name alphabetically
     * If this is the same again, compare IDs
     * @param restArray     input array to be sorted
     * @param first         first index to sort
     * @param end           index to stop sort
     * @return              index to further split the arrays recursively
     */
    private int partitionStars(Restaurant restArray[], int first, int end) {
        int pivot = restArray[end].getWarwickStars();
        int i = (first-1);

        for (int j = first; j < end; j++) {
            if (restArray[j].getWarwickStars() > restArray[end].getWarwickStars()) {
                i++;

                Restaurant restTemp = restArray[i];
                restArray[i] = restArray[j];
                restArray[j] = restTemp;
            }
            if(restArray[j].getWarwickStars() == restArray[end].getWarwickStars()){
                if (restArray[j].getName().compareToIgnoreCase(restArray[end].getName()) < 0) {
                    i++;

                    Restaurant restTemp = restArray[i];
                    restArray[i] = restArray[j];
                    restArray[j] = restTemp;
                }
                if(restArray[j].getName().compareToIgnoreCase(restArray[end].getName()) == 0){
                    long pivotID = restArray[end].getID();
                    if (restArray[j].getID() <= pivotID) {
                        i++;
                        Restaurant tempRestaurantTwo = restArray[i];
                        restArray[i] = restArray[j];
                        restArray[j] = tempRestaurantTwo;
                    }
                }
            }
        }
        Restaurant tempRestaurantThree = restArray[i+1];
        restArray[i+1] = restArray[end];
        restArray[end] = tempRestaurantThree;

        return i+1;
    }




    /**
     * Similar to Warick Stars but for Rating
     * Does not use arraylist as the size is defined
     * @param restaurants
     * @return
     */
    public Restaurant[] getRestaurantsByRating(Restaurant[] restaurants) {
        // TODO
        for(int i = 0; i<restaurants.length; i++){
            //validation takes O(n)
            if(dataChecker.isValid(restaurants[i]) == false){
                return new Restaurant[0];
            }
        }
        quickSortRating(restaurants, 0,restaurants.length-1);
        return restaurants;
    }
    public void quickSortRating(Restaurant inputArray[], int indexStart, int end){
        if (indexStart < end) {
            int partitionIndex = partitionRating(inputArray, indexStart, end);

            quickSortRating(inputArray, indexStart, partitionIndex-1);
            quickSortRating(inputArray, partitionIndex+1, end);
        }
    }
    /**
     * Compare by Customer Rating
     * If this is the same then compare by name and then ID if needed.
     * @param restArray     input array to be sorted
     * @param first         first index to sort
     * @param end           index to stop sort
     * @return              index to further split the arrays recursively
     */
    private int partitionRating(Restaurant restArray[], int first, int end) {
        float pivot = restArray[end].getCustomerRating();
        int i = (first-1);

        for (int j = first; j < end; j++) {
            if (restArray[j].getCustomerRating() > restArray[end].getCustomerRating()) {
                i++;

                Restaurant restTemp = restArray[i];
                restArray[i] = restArray[j];
                restArray[j] = restTemp;
            }
            if(restArray[j].getCustomerRating() == restArray[end].getCustomerRating()){
                if (restArray[j].getName().compareToIgnoreCase(restArray[end].getName()) < 0) {
                    i++;

                    Restaurant restTemp = restArray[i];
                    restArray[i] = restArray[j];
                    restArray[j] = restTemp;
                }
                if(restArray[j].getName().compareToIgnoreCase(restArray[end].getName()) == 0){
                    long pivotID = restArray[end].getID();
                    if (restArray[j].getID() <= pivotID) {
                        i++;
                        Restaurant tempRestaurantTwo = restArray[i];
                        restArray[i] = restArray[j];
                        restArray[j] = tempRestaurantTwo;
                    }
                }
            }
        }
        Restaurant tempRestaurantThree = restArray[i+1];
        restArray[i+1] = restArray[end];
        restArray[end] = tempRestaurantThree;

        return i+1;
    }


    /**
     * Converts restaurant store into arrays
     * Calls getRestaurantByDistance which takes an input array
     * returns the array of restaurantDistance objects sorted by distance
     * @param latitude
     * @param longitude
     * @return
     */
    public RestaurantDistance[] getRestaurantsByDistanceFrom(float latitude, float longitude) {
        // TODO
        Restaurant[] restArray = new Restaurant[restaurantArray.size()];
        for(int i = 0; i<restaurantArray.size(); i++){
            restArray[i] = restaurantArray.get(i);
        }
        return getRestaurantsByDistanceFrom(restArray, latitude, longitude);
    }


    /**
     * Checks if any of the input restaurant objects in array are invalid
     * Sorts the array by distance from paramters
     * Creates restaurantDistance objects for each restaurant object
     * @param restaurants   Array of restaurants to be sorted by distance
     * @param latitude
     * @param longitude
     * @return              Array of restaurantDistance objects.
     */
    public RestaurantDistance[] getRestaurantsByDistanceFrom(Restaurant[] restaurants, float latitude, float longitude) {
        // TODO
        if(restaurants == null || restaurants.length == 0){
            return new RestaurantDistance[0];
        }

        for(int i =0; i<restaurants.length; i++){
            if(restaurants[i].getRepeatedID() == null || dataChecker.isValid(restaurants[i]) == false){
                return new RestaurantDistance[0];
            }
        }

        quickSortDistance(restaurants, 0, restaurants.length -1, latitude, longitude);

        RestaurantDistance[] restDistance = new RestaurantDistance[restaurants.length];
        for(int i = 0; i<restaurants.length; i++){
            float distance = distanceCalculator.inKilometres(latitude, longitude, restaurants[i].getLatitude(), restaurants[i].getLongitude());
            RestaurantDistance r = new RestaurantDistance(restaurants[i], distance);
            restDistance[i] = r;
        }
        //Places the restaurantDistance objects in the array to be returned
        return restDistance;
    }

    /**
     * Reursively splits the input array into smaller partitions to sort
     * Passes the parameter coordinates as a arguments to the partitionIndex method
     *
     * @param inputArray
     * @param indexStart
     * @param end
     * @param lat       The target latitude
     * @param lon       The target longitude
     */
    public void quickSortDistance(Restaurant inputArray[], int indexStart, int end, float lat, float lon){
        if (indexStart < end) {
            int partitionIndex = partitionDistance(inputArray, indexStart, end, lat, lon);

            quickSortDistance(inputArray, indexStart, partitionIndex-1, lat, lon);
            quickSortDistance(inputArray, partitionIndex+1, end, lat, lon);
        }
    }

    /**
     * Calls the haversine distance method (inKilometers) to calculate distance of each restaurant
     * Sorts by the distance for each restaurant increasing distance wise.
     * If distance is the same, sort by ID
     * @param restArray
     * @param first
     * @param end
     * @param targetLat
     * @param targetLon
     * @return
     */
    private int partitionDistance(Restaurant restArray[], int first, int end, float targetLat, float targetLon) {
        float pivotLat = restArray[end].getLatitude();
        float pivotLong = restArray[end].getLongitude();
        float pivotDistance = distanceCalculator.inKilometres(targetLat, targetLon, pivotLat, pivotLong);
        int i = (first-1);

        for (int j = first; j < end; j++) {
            //compare both distances to sort increasingly by distance to target
            float currentDistance = distanceCalculator.inKilometres(targetLat, targetLon, restArray[j].getLatitude(), restArray[j].getLongitude());
            if (currentDistance < pivotDistance) {
                i++;

                Restaurant restTemp = restArray[i];
                restArray[i] = restArray[j];
                restArray[j] = restTemp;
            }
            if(currentDistance == pivotDistance){
                long pivotID = restArray[end].getID();
                if (restArray[j].getID() <= pivotID) {
                    i++;
                    Restaurant tempRestaurantTwo = restArray[i];
                    restArray[i] = restArray[j];
                    restArray[j] = tempRestaurantTwo;
                }
            }
        }
        Restaurant tempRestaurantThree = restArray[i+1];
        restArray[i+1] = restArray[end];
        restArray[end] = tempRestaurantThree;

        return i+1;
    }

    /**
     * This method calls the string formatter class to strip accents from a query
     * Then all the restaurants are converted to lowercase and searched if a substring contains the searchterm query, and add to arraylist
     * Then all cuisines are converted to lowercase and restaurants with matching cuisines are added
     * Then all restaurants with all places with substring searchterm are added to arraylist.
     * Sort by name
     * @param searchTerm
     * @return
     */
    public Restaurant[] getRestaurantsContaining(String searchTerm) {
        // TODO
        if(searchTerm == ""){
            return new Restaurant[0];
        }
        restaurantMatchingQuery.clear();
        String searchTermConvertedFaster = stringFormatter.convertAccentsFaster(searchTerm);
        searchTermConvertedFaster = searchTermConvertedFaster.toLowerCase().trim();//remove spaces trailing + leading
        for(int i = 0;i<restaurantArray.size(); i++){
            if(restaurantArray.get(i).getName().toLowerCase().contains(searchTermConvertedFaster)){
                restaurantMatchingQuery.add(restaurantArray.get(i));
                continue;
            }
            if(restaurantArray.get(i).getCuisine().toString().toLowerCase().contains(searchTermConvertedFaster)){
                restaurantMatchingQuery.add(restaurantArray.get(i));
                continue;
            }
            Place queriedPlace = convertToPlace.convert(restaurantArray.get(i).getLatitude(), restaurantArray.get(i).getLongitude());

            if(queriedPlace.getName().toLowerCase().contains(searchTermConvertedFaster)){
                restaurantMatchingQuery.add(restaurantArray.get(i));
            }
        }
        Restaurant[] queriedRestaurants = new Restaurant[restaurantMatchingQuery.size()];
        for(int j = 0; j<restaurantMatchingQuery.size(); j++){
            queriedRestaurants[j] = restaurantMatchingQuery.get(j);
        }
        quickSortString(queriedRestaurants, 0,queriedRestaurants.length -1);
        return queriedRestaurants;


    }
}
