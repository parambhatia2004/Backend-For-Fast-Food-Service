package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IFavouriteStore;
import uk.ac.warwick.cs126.models.Favourite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.print.DocFlavor.STRING;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyArrayList;
import uk.ac.warwick.cs126.structures.HashMap;
import uk.ac.warwick.cs126.util.DataChecker;

public class FavouriteStore implements IFavouriteStore {
    /**
     * Here I declare and initialise all classes used in the file.
     * Design Decision to initialise all immediately rather than in individual methods
     * This will help with efficiency, as only the initial time will be longer, but faster usage.
     */
    private MyArrayList<Favourite> favouriteArray;
    private DataChecker dataChecker;
    private HashMap<String, String> BlacklistedFavourite;
    private HashMap<String, Favourite> Backlog;
    private MyArrayList<Favourite> favouriteMatchingCustID;
    private MyArrayList<Favourite> favouriteMatchingRestID;
    private MyArrayList<Favourite> commonRestaurants;
    private MyArrayList<Favourite> Customer2;
    private HashMap<String, Favourite> possibleCommon;
    private HashMap<String, Favourite> possibleDifference;
    private HashMap<String, Favourite> possibleSetDifference1;
    private HashMap<String, Favourite> possibleSetDifference2;
    private HashMap<String, String> encounteredCustomer;
    private MyArrayList<String> customerList;
    private HashMap<String, String> encounteredRestaurant;
    private MyArrayList<String> restaurantList;


    public FavouriteStore() {
        // Initialise variables here
        favouriteArray = new MyArrayList<>();
        dataChecker = new DataChecker();
        BlacklistedFavourite = new HashMap<>();
        Backlog = new HashMap<>();
        favouriteMatchingCustID = new MyArrayList<>();
        favouriteMatchingRestID = new MyArrayList<>();
        commonRestaurants = new MyArrayList<>();
        possibleCommon = new HashMap<>();
        possibleDifference = new HashMap<>();
        possibleSetDifference1 = new HashMap<>();
        possibleSetDifference2 = new HashMap<>();
        encounteredCustomer = new HashMap<>();
        customerList = new MyArrayList<>();
        encounteredRestaurant = new HashMap<>();
        restaurantList = new MyArrayList<>();
    }

    public Favourite[] loadFavouriteDataToArray(InputStream resource) {
        Favourite[] favouriteArray = new Favourite[0];

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

            Favourite[] loadedFavourites = new Favourite[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int favouriteCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");
                    Favourite favourite = new Favourite(
                            Long.parseLong(data[0]),
                            Long.parseLong(data[1]),
                            Long.parseLong(data[2]),
                            formatter.parse(data[3]));
                    loadedFavourites[favouriteCount++] = favourite;
                }
            }
            csvReader.close();

            favouriteArray = loadedFavourites;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return favouriteArray;
    }


    /**
     * After validating the ID, check if it exists in current store
     * If it does then remove corressponding, and add back favourite with same customer + restaurant ID
     * If there is a valid id and a favourite with same customer+restaurant id, then compare date
     * Add relevant favourite to the backlog
     *
     * @param favourite     Object to be added to favouriteArray
     * @return              boolean stating result of process
     */
    public boolean addFavourite(Favourite favourite) {
        // TODO

        String currentStringID = String.valueOf(favourite.getID());
        if(dataChecker.isValid(favourite) == false){
            return false;
        }
        if(BlacklistedFavourite.contains(currentStringID) == true){
            for(int i =0;i<favouriteArray.size();i++){
                if(favouriteArray.get(i).getID().equals(favourite.getID())){
                    favouriteArray.remove(favouriteArray.get(i));//removes from current store
                }
            }
            return false;
        }
        for(int j = 0; j<favouriteArray.size(); j++){
            //checks to see if current ID exists in array
            if(favourite.getID().equals(favouriteArray.get(j).getID())){
                String customerID = String.valueOf(favouriteArray.get(j).getCustomerID());
                String restID = String.valueOf(favouriteArray.get(j).getRestaurantID());
                String ConcatID = customerID + restID;

                if(Backlog.contains(ConcatID)){
                    Favourite addBack = Backlog.get(ConcatID);//add back to array
                    Backlog.remove(ConcatID);
                    BlacklistedFavourite.remove(String.valueOf(addBack.getID()));
                    favouriteArray.add(addBack);
                }


                BlacklistedFavourite.add(currentStringID, currentStringID);
                favouriteArray.remove(favouriteArray.get(j));

                return false;
            }
        }
        for(int i=0; i<favouriteArray.size(); i++){

            if((favouriteArray.get(i).getCustomerID().equals(favourite.getCustomerID()) && (favouriteArray.get(i).getRestaurantID().equals(favourite.getRestaurantID())))){
                //compare both favourites to find the recent one by date favourited
                if(favouriteArray.get(i).getDateFavourited().compareTo(favourite.getDateFavourited()) > 0){
                    BlacklistedFavourite.add(String.valueOf(favouriteArray.get(i).getID()),String.valueOf(favouriteArray.get(i).getID()));
                    String concatentatedID = String.valueOf(favouriteArray.get(i).getCustomerID()) + String.valueOf(favouriteArray.get(i).getRestaurantID());
                    Backlog.add(concatentatedID, favouriteArray.get(i));
                    favouriteArray.add(favourite);
                    favouriteArray.remove(favouriteArray.get(i));

                    return true;
                }
                else{
                    BlacklistedFavourite.add(String.valueOf(favourite.getID()),String.valueOf(favourite.getID()));
                    String concatentatedID = String.valueOf(favourite.getCustomerID()) + String.valueOf(favourite.getRestaurantID());
                    Backlog.add(concatentatedID, favourite);
                    return false;
                }
            }
        }
        favouriteArray.add(favourite);
        return true;//only true if added
    }
    /**
     * Loop through input array and check if each element has been added successfully
     * Only return true if all elements in array have been added
     *
     * @param favourites    array of favourites to be added
     * @return              boolean status of added
     */
    public boolean addFavourite(Favourite[] favourites) {
        // TODO
        if(favourites == null || favourites.length == 0){
            return false;
        }
        boolean allAdded = true;
        for(int i = 0; i<favourites.length; i++){
            if(this.addFavourite(favourites[i]) == false){
                allAdded = false;//if even one customer is invalid, add others, but overall return false
            }
        }
        for(int i = 0; i<favouriteArray.size(); i++){
        }
        return allAdded;
    }

    /**
     * Get corresponding favourite element
     * @param id
     * @return
     */
    public Favourite getFavourite(Long id) {
        // TODO
        for(int i = 0; i<favouriteArray.size(); i++){
            if(id.equals(favouriteArray.get(i).getID())){
                return favouriteArray.get(i);
            }
        }
        return null;
    }

    /**
     * Converts the favourite array into an array(from arraylist)
     * Calls quicksort method which takes O(nlogn) time
     * @return      favourite array sorted
     */
    public Favourite[] getFavourites() {
        // TODO
        Favourite favourites[] = new Favourite[favouriteArray.size()];
        for(int i = 0; i<favouriteArray.size(); i++){
            favourites[i] = favouriteArray.get(i);
        }
        quickSort(favourites, 0, favourites.length -1);
        return favourites;
    }

    /**
     * Quicksort method which recursively calls the partitionIndex method which is used to sort the array
     * This method can be considered to have O(1) time complexity since it takes constant time to call function.
     *
     * @param inputArray    Array to be sorted
     * @param indexStart    Index to start sorting
     * @param end           Last index to end sorting
     */
    public void quickSort(Favourite inputArray[], int indexStart, int end) {
        if (indexStart < end) {
            int partitionIndex = partition(inputArray, indexStart, end);
            //recursion to split initial arrays into smaller arrays
            quickSort(inputArray, indexStart, partitionIndex-1);
            quickSort(inputArray, partitionIndex+1, end);
        }
    }


    /**
     * Using the last elements ID, compare all favourites(in input array) ID to the last element's ID.
     * If it is lower than the last element, then swap the elements
     * @param favouriteArray     Input Array to srt
     * @param first             Index to start sorting by
     * @param end               Inde x to finish sorting by
     * @return                  Index to further split the array
     */
    private int partition(Favourite favouriteArray[], int first, int end) {
        long pivot = favouriteArray[end].getID();
        int i = first-1;

        for (int j = first; j < end; j++) {
            //compare all elements in array specified by the index, by pivot ID.
            if (favouriteArray[j].getID() <= pivot) {
                i++;
                Favourite tempFavourite = favouriteArray[i];
                favouriteArray[i] = favouriteArray[j];
                favouriteArray[j] = tempFavourite;
            }
        }

        Favourite tempFavourite2 = favouriteArray[i+1];
        favouriteArray[i+1] = favouriteArray[end];//swap the last customer
        favouriteArray[end] = tempFavourite2;

        return i+1; //index to further split the array
    }


    /**
     * Create arraylist of all favourites with matching customer id
     * add by date to arraylist
     * convert arraylist to array
     * @param id    match the CustomerID in the favouriteArray
     * @return      array of favourites with matching customerIDs
     */
    public Favourite[] getFavouritesByCustomerID(Long id) {
        favouriteMatchingCustID.clear();

        for(int i = 0; i<favouriteArray.size(); i++){
            if(id.equals(favouriteArray.get(i).getCustomerID())){
                this.addByDate(favouriteArray.get(i));//add by date
            }
        }
        Favourite favouritesWithCustomerID[] = new Favourite[favouriteMatchingCustID.size()];
        for(int j = 0; j<favouriteMatchingCustID.size();j++){
            favouritesWithCustomerID[j] = favouriteMatchingCustID.get(j);
        }
        return favouritesWithCustomerID;
    }

    public boolean addByDate(Favourite favourite) {
        int value = 0;
        // Method to add element to the list, inserted in the correct place for the ordering of favourite.
        if (favouriteMatchingCustID.size() == 0) {
          favouriteMatchingCustID.add(favourite);
        }
        else{
          for (int i = 0;i<favouriteMatchingCustID.size() ;i++ ) {
            if(favourite.getDateFavourited().compareTo(favouriteMatchingCustID.get(i).getDateFavourited())>0) {
              value++;
            }
          }
          if (value == 0) {
            favouriteMatchingCustID.add(favourite);
          }
          else{
            for (int j = favouriteMatchingCustID.size() - value; j<favouriteMatchingCustID.size() ;j++ ) {
              favourite=favouriteMatchingCustID.set(j, favourite);
            }
            favouriteMatchingCustID.add(favourite);
          }
        }
        return true;
    }



    /**
     * Similar to above method, but adds to arraylist by matching restaurant ID
     * Convert to array
     * @param id
     * @return
     */
    public Favourite[] getFavouritesByRestaurantID(Long id) {
        // TODO
        favouriteMatchingRestID.clear();
        //use an arraylist because there is an unspecified size
        for(int i = 0; i<favouriteArray.size(); i++){
            if(id.equals(favouriteArray.get(i).getRestaurantID())){
                this.addByDateRest(favouriteArray.get(i));
            }
        }
        Favourite favouritesWithRestaurantID[] = new Favourite[favouriteMatchingRestID.size()];
        for(int j = 0; j<favouriteMatchingRestID.size();j++){
            favouritesWithRestaurantID[j] = favouriteMatchingRestID.get(j);
        }
        return favouritesWithRestaurantID;
    }




    public boolean addByDateRest(Favourite favourite) {
        int value = 0;
        // Method to add element to the list, inserted in the correct place for the ordering of E.
        if (favouriteMatchingRestID.size() == 0) {
          favouriteMatchingRestID.add(favourite);
        }
        else{
          for (int i = 0;i<favouriteMatchingRestID.size() ;i++ ) {
            if(favourite.getDateFavourited().compareTo(favouriteMatchingRestID.get(i).getDateFavourited())>0) {
              value++;
            }
          }
          if (value == 0) {
            favouriteMatchingRestID.add(favourite);
          }
          else{
            //value--;
            for (int j = favouriteMatchingRestID.size() - value; j<favouriteMatchingRestID.size() ;j++ ) {
              favourite=favouriteMatchingRestID.set(j, favourite);
            }
            favouriteMatchingRestID.add(favourite);
          }
        }
        //super.add(element); // This will mean elements are stored in the order that they were added
        return true;
    }



    /**
     * Use a hashmap to store favourite objects related to customer 1
     * Use the hashmap and loop through favouriteArray to see which favourites are common with the hashmap and customer2
     * Add common restaurants to an arraylist, since it will be an unspecified size
     * Only add the favourite which was the latest to be added
     * Sort the favourite array by Date Favourited
     * @param customer1ID
     * @param customer2ID
     * @return
     */
    public Long[] getCommonFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        commonRestaurants.clear();

        for(int i =0; i<favouriteArray.size(); i++){
            //only get fav matching customer id1
            if(favouriteArray.get(i).getCustomerID().equals(customer1ID)){
                possibleCommon.add(String.valueOf(favouriteArray.get(i).getRestaurantID()), favouriteArray.get(i));
            }
        }
        for(int j =0; j<favouriteArray.size(); j++){
            if(favouriteArray.get(j).getCustomerID().equals(customer2ID)){
                if(possibleCommon.contains(String.valueOf(favouriteArray.get(j).getRestaurantID()))){
                    Favourite temp = possibleCommon.get(String.valueOf(favouriteArray.get(j).getRestaurantID()));
                    if(temp.getDateFavourited().compareTo(favouriteArray.get(j).getDateFavourited()) > 0){
                        commonRestaurants.add(temp);//only add recent favourite
                    }
                    else{
                        commonRestaurants.add(favouriteArray.get(j));
                    }
                }
            }
        }
        Favourite[] tempArray = new Favourite[commonRestaurants.size()];
        for(int i = 0; i<commonRestaurants.size(); i++){
            tempArray[i] = commonRestaurants.get(i);
        }
        quickSortDate(tempArray, 0, tempArray.length - 1);

        Long[] commonRest = new Long[tempArray.length];
        for(int k=0; k<tempArray.length; k++){
            commonRest[k] = tempArray[k].getRestaurantID();
        }
        return commonRest;
    }

    /**
     * Similar to quickSort, but this sorts by date favourited
     * @param inputArray
     * @param indexStart
     * @param end
     */
    public void quickSortDate(Favourite inputArray[], int indexStart, int end){
        if (indexStart < end) {
            int partitionIndex = partitionDate(inputArray, indexStart, end);

            quickSortDate(inputArray, indexStart, partitionIndex-1);
            quickSortDate(inputArray, partitionIndex+1, end);
        }
    }
    private int partitionDate(Favourite restArray[], int first, int end) {
        Date pivot = restArray[end].getDateFavourited();
        int i = (first-1);

        for (int j = first; j < end; j++) {
            if (restArray[j].getDateFavourited().compareTo(pivot) > 0) {
                i++;

                Favourite favtemp = restArray[i];
                restArray[i] = restArray[j];
                restArray[j] = favtemp;
            }
            if(restArray[j].getDateFavourited().compareTo(pivot) == 0){
                //if date is same then add by id
                long pivotID = restArray[end].getID();
                if (restArray[j].getID() <= pivotID) {
                    i++;
                    Favourite tempFav2 = restArray[i];
                    restArray[i] = restArray[j];
                    restArray[j] = tempFav2;
                }
            }
        }
        Favourite tempFav3 = restArray[i+1];//swap last element
        restArray[i+1] = restArray[end];
        restArray[end] = tempFav3;

        return i+1;
    }



    /**
     * use hashmap to store all favourites matching customerID to allow for O(1) lookup
     * If favourite in hashmap is also favourited by customer2ID, remove from result arraylist
     * Convert resulting arraylist to array and sort by date favourited
     * @param customer1ID
     * @param customer2ID
     * @return
     */
    public Long[] getMissingFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        // TODO
        commonRestaurants.clear();
        for(int i =0; i<favouriteArray.size(); i++){
            if(favouriteArray.get(i).getCustomerID().equals(customer1ID)){
                commonRestaurants.add(favouriteArray.get(i));//add to resulting array
                possibleDifference.add(String.valueOf(favouriteArray.get(i).getRestaurantID()), favouriteArray.get(i));
            }
        }
        for(int j =0; j<favouriteArray.size(); j++){
            if(favouriteArray.get(j).getCustomerID().equals(customer2ID)){
                if(possibleDifference.contains(String.valueOf(favouriteArray.get(j).getRestaurantID()))){
                    Favourite Temp = possibleDifference.get(String.valueOf(favouriteArray.get(j).getRestaurantID()));
                    commonRestaurants.remove(Temp);
                }
            }
        }

        Favourite[] tempArray = new Favourite[commonRestaurants.size()];
        for(int i = 0; i<commonRestaurants.size(); i++){
            tempArray[i] = commonRestaurants.get(i);
        }
        quickSortDate(tempArray, 0, tempArray.length - 1);

        Long[] uncommonRest = new Long[tempArray.length];
        for(int k=0; k<tempArray.length; k++){
            uncommonRest[k] = tempArray[k].getRestaurantID();
        }
        return uncommonRest;

    }



    /**
     * Add favourites of Customer1ID to hashmap
     * if any of the favourites are in hashmap (O(1) lookup), then remove from resulting arraylist
     * Do the same for favourites of Customer2ID
     * Convert resulting arraylist to array, and sort by date
     * Extract restaurant ID from sorted array, so that long array is sorted
     * @param customer1ID           Customer 1 ID
     * @param customer2ID           Customer 2 ID
     * @return                      Array of restaurantID sorted by date
     */
    public Long[] getNotCommonFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        // TODO

        commonRestaurants.clear();
        for(int i =0; i<favouriteArray.size(); i++){
            if(favouriteArray.get(i).getCustomerID().equals(customer1ID)){
                commonRestaurants.add(favouriteArray.get(i));
                possibleSetDifference1.add(String.valueOf(favouriteArray.get(i).getRestaurantID()), favouriteArray.get(i));
            }
        }

        for(int j =0; j<favouriteArray.size(); j++){
            if(favouriteArray.get(j).getCustomerID().equals(customer2ID)){
                if(possibleSetDifference1.contains(String.valueOf(favouriteArray.get(j).getRestaurantID()))){
                    Favourite Temp = possibleSetDifference1.get(String.valueOf(favouriteArray.get(j).getRestaurantID()));
                    commonRestaurants.remove(Temp);
                }
            }
        }

        for(int i =0; i<favouriteArray.size(); i++){
            if(favouriteArray.get(i).getCustomerID().equals(customer2ID)){
                commonRestaurants.add(favouriteArray.get(i));//add to final list
                possibleSetDifference2.add(String.valueOf(favouriteArray.get(i).getRestaurantID()), favouriteArray.get(i));
            }
        }

        for(int j =0; j<favouriteArray.size(); j++){
            if(favouriteArray.get(j).getCustomerID().equals(customer1ID)){
                if(possibleSetDifference2.contains(String.valueOf(favouriteArray.get(j).getRestaurantID()))){
                    Favourite Temp = possibleSetDifference2.get(String.valueOf(favouriteArray.get(j).getRestaurantID()));
                    commonRestaurants.remove(Temp);//remove from list if favourited by customer 1
                }
            }
        }


        Favourite[] tempArray = new Favourite[commonRestaurants.size()];
        for(int i = 0; i<commonRestaurants.size(); i++){
            tempArray[i] = commonRestaurants.get(i);
        }
        quickSortDate(tempArray, 0, tempArray.length - 1);

        Long[] uncommonRest = new Long[tempArray.length];
        for(int k=0; k<tempArray.length; k++){
            uncommonRest[k] = tempArray[k].getRestaurantID();
        }


        return uncommonRest;

    }

    /**
     * This method is the longest
     * If encountering a customerID for the first time, concatenate the customer id, count and date
     * Increase index
     * Add to arraylist at index, and add ID to hashmap for instant lookup access
     *
     * If encountering a customerID not for the first time, use hashmap to get location of customerID in arraylist
     * This will be O(1) lookup time.
     * Extract the string and increment count
     * Extract date and compare with current favourite, choose latest.
     * Update concatenated string.
     *
     * Finally store strings in ann array and call custom sorting method
     *
     * Now the array is sorted and extract string to get CustomerID and convert to Long
     * @return      Sorted customers by most favourites
     */
    public Long[] getTopCustomersByFavouriteCount() {
        // TODO
        int index = 0;

        for(int i =0; i<favouriteArray.size(); i++){
            if(encounteredCustomer.contains(String.valueOf(favouriteArray.get(i).getCustomerID()))){
                String location = encounteredCustomer.get(String.valueOf(favouriteArray.get(i).getCustomerID()));
                String details = customerList.get(Integer.parseInt(location));
                String ID = details.substring(0, 16);
                String Count = details.substring(16, 20);
                int countID = Integer.parseInt(Count) + 1;
                String stringDate = details.substring(20);
                Date oldDate = null;
                try{
                    Date dateread= new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(stringDate);
                    oldDate = dateread;
                }catch (ParseException e) {
                    System.out.println("Erro");
                }
                Date newDate = favouriteArray.get(i).getDateFavourited();
                if(oldDate.compareTo(newDate) < 0){
                    oldDate = newDate;
                }
                DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                String dateFinal = df.format(oldDate);
                String newCount = "";
                if (countID < 10) {
                    newCount = "000" + String.valueOf(countID);
                }
                else if (countID < 100 && countID > 9){
                    newCount = "00" + String.valueOf(countID);
                }
                else if(countID < 1000 && countID > 99){
                    newCount = "0" + String.valueOf(countID);
                }
                else if(countID >999 && countID <10000){
                    newCount = String.valueOf(countID);
                }
                String updatedDetails = ID + newCount + dateFinal;
                customerList.set(Integer.parseInt(location), updatedDetails);

            }
            else{
                String cID = String.valueOf(favouriteArray.get(i).getCustomerID());
                String date = String.valueOf(favouriteArray.get(i).getDateFavourited());
                String formatted = cID + "0001" + date;
                customerList.add(formatted);
                encounteredCustomer.add(String.valueOf(favouriteArray.get(i).getCustomerID()), String.valueOf(index));
                index++;
            }
        }
        String[] newArr = new String[customerList.size()];
        for(int i = 0; i<customerList.size(); i++){
            newArr[i] = customerList.get(i);
        }
        quickSortAmount(newArr, 0, newArr.length -1);
        Long[] finalreturn = new Long[20];
        for(int i = 0; i<newArr.length; i++){
            if (i > 19) {
                break;//size limited to twenty
            }
            String ID = newArr[i].substring(0, 16);

            finalreturn[i] = Long.parseLong(ID);
        }

        return finalreturn;
    }

    public void quickSortAmount(String inputArray[], int indexStart, int end) {
        if (indexStart < end) {
            int partitionIndex = partitionAmount(inputArray, indexStart, end);
            //recursion to split initial arrays into smaller arrays
            quickSortAmount(inputArray, indexStart, partitionIndex-1);
            quickSortAmount(inputArray, partitionIndex+1, end);
        }
    }

    /**
     * This method inputs a string array, and extracts the count from the string
     * If the count is same, then compare by latest date favourited
     * Quicksort method, time is O(nlogn)
     * @param inputArray
     * @param first
     * @param end
     * @return          index to recursively split array further
     */
    private int partitionAmount(String inputArray[], int first, int end) {
        String pivotString = inputArray[end].substring(16, 20);
        int pivot = Integer.parseInt(pivotString);//convert to int

        int i = first-1;

        for (int j = first; j < end; j++) {
            String currentString = inputArray[j].substring(16, 20);
            //compare all elements in array specified by the index, by pivot ID.
            if (Integer.parseInt(currentString) > pivot) {
                i++;
                String temp = inputArray[i];
                inputArray[i] = inputArray[j];
                inputArray[j] = temp;
            }
            else if(Integer.parseInt(currentString) == pivot){
                String firstdate = inputArray[j].substring(20);
                String seconddate = inputArray[end].substring(20);

                Date oldDate = null;
                Date CheckDate = null;
                try{
                    Date DateOne= new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(firstdate);
                    oldDate = DateOne;//converting date to specified format
                    Date DateTwo= new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(seconddate);
                    CheckDate = DateTwo;
                }catch (ParseException e) {
                    System.out.println("Erro");
                }
                if(oldDate.compareTo(CheckDate) < 0){
                    i++;

                    String restTemp = inputArray[i];
                    inputArray[i] = inputArray[j];
                    inputArray[j] = restTemp;
                }

            }
        }

        String tempTwo = inputArray[i+1];
        inputArray[i+1] = inputArray[end];//swap the last customer
        inputArray[end] = tempTwo;

        return i+1; //index to further split the array
    }



    /**
     * Similar method as top Customers, just with restaurant ID
     * @return
     */
    public Long[] getTopRestaurantsByFavouriteCount() {
        int index = 0;

        for(int i =0; i<favouriteArray.size(); i++){
            if(encounteredRestaurant.contains(String.valueOf(favouriteArray.get(i).getRestaurantID()))){
                String location = encounteredRestaurant.get(String.valueOf(favouriteArray.get(i).getRestaurantID()));
                String details = restaurantList.get(Integer.parseInt(location));
                String ID = details.substring(0, 16);
                String Count = details.substring(16, 20);
                int countID = Integer.parseInt(Count) + 1;
                String stringDate = details.substring(20);
                Date oldDate = null;
                try{
                    Date dateread= new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(stringDate);
                    oldDate = dateread;
                }catch (ParseException e) {
                    System.out.println("Erro");
                }
                Date newDate = favouriteArray.get(i).getDateFavourited();
                if(oldDate.compareTo(newDate) < 0){
                    oldDate = newDate;
                }
                DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                String dateFinal = df.format(oldDate);
                String newCount = "";
                if (countID < 10) {
                    newCount = "000" + String.valueOf(countID);
                }
                else if (countID < 100 && countID > 9){
                    newCount = "00" + String.valueOf(countID);
                }
                else if(countID < 1000 && countID > 99){
                    newCount = "0" + String.valueOf(countID);
                }
                else if(countID >999 && countID <10000){
                    newCount = String.valueOf(countID);
                }
                String updatedDetails = ID + newCount + dateFinal;
                restaurantList.set(Integer.parseInt(location), updatedDetails);
            }
            else{
                String rID = String.valueOf(favouriteArray.get(i).getRestaurantID());
                String date = String.valueOf(favouriteArray.get(i).getDateFavourited());
                String formatted = rID + "0001" + date;
                restaurantList.add(formatted);
                encounteredRestaurant.add(String.valueOf(favouriteArray.get(i).getRestaurantID()), String.valueOf(index));
                index++;
            }
        }
        String[] newArr = new String[restaurantList.size()];
        for(int i = 0; i<restaurantList.size(); i++){
            newArr[i] = restaurantList.get(i);
        }
        quickSortAmount(newArr, 0, newArr.length -1);
        Long[] finalreturn = new Long[20];
        for(int i = 0; i<newArr.length; i++){
            if (i > 19) {
                break;//size is limited to 20
            }
            String ID = newArr[i].substring(0, 16);

            finalreturn[i] = Long.parseLong(ID);
        }

        return finalreturn;
    }
}
