package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.ICustomerStore;
import uk.ac.warwick.cs126.models.Customer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyArrayList;
import uk.ac.warwick.cs126.structures.HashMap;

import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.StringFormatter;

public class CustomerStore implements ICustomerStore {

    private MyArrayList<Customer> customerArray;
    private DataChecker dataChecker;
    private HashMap<String, String> Blacklisted;
    private StringFormatter stringFormatter;
    private MyArrayList<Customer> matchingCustomers;

    /**
     * Initialsie all data structures and classes to be used in the method
     */
    public CustomerStore() {
        // Initialise variables here
        customerArray = new MyArrayList<>();
        dataChecker = new DataChecker();
        Blacklisted = new HashMap<>();
        stringFormatter = new StringFormatter();
        matchingCustomers = new MyArrayList<>();
    }

    public Customer[] loadCustomerDataToArray(InputStream resource) {
        Customer[] customerArray = new Customer[0];

        try {
            byte[] inputStreamBytes = IOUtils.toByteArray(resource);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int lineCount = 0;
            String line;
            while ((line=lineReader.readLine()) != null) {
                if (!("".equals(line))) {
                    lineCount++;
                }
            }
            lineReader.close();

            Customer[] loadedCustomers = new Customer[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int customerCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");

                    Customer customer = (new Customer(
                            Long.parseLong(data[0]),
                            data[1],
                            data[2],
                            formatter.parse(data[3]),
                            Float.parseFloat(data[4]),
                            Float.parseFloat(data[5])));

                    loadedCustomers[customerCount++] = customer;
                }
            }
            csvReader.close();

            customerArray = loadedCustomers;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return customerArray;
    }


    /**
     * Check if the customer is a valid customer, using the isValid method in dataChecker
     * Once validated, it checks if the customer ID of the customer has been blacklisted
     * If any of the above two checks fail, return false.
     * Next, it checks to see if any customers inside the store have the same ID as the customer to be added
     * If this is true, then the ID is blacklisted and the customer removed from store, and this customer is not added.
     * Otherwise the customer will be added to the store
     *
     * @param customer  Customer object to be added to store
     * @return          Status of adding customer
     */
    public boolean addCustomer(Customer customer) {
        // TODO
        String currentStringID = String.valueOf(customer.getID());//Use string value of ID
        if(dataChecker.isValid(customer) == false){
            return false;
        }
        //provides fast lookup time, O(1), to see if an ID is blaclisted
        if(Blacklisted.contains(currentStringID) == true){
            return false;
        }

        for(int j = 0; j<customerArray.size(); j++){
            if(customer.getID().equals(customerArray.get(j).getID())){
                Blacklisted.add(currentStringID, currentStringID);//adding blacklisted IDs to a hashmap
                customerArray.remove(customerArray.get(j));
                return false;
            }
        }
        customerArray.add(customer);//only adds customer if all checks are passed
        return true;
    }

    /**
     * Firstly, it checks to see if the array is null, or if the array is empty.
     * If not, then for each element in the array, addCustomer() is called to validate and add the element
     * Boolean value keeps track whether all values have been added successfully.
     *
     * @param customers     Array of customers to be added
     * @return              boolean value to check if all customers have been added.
     */
    public boolean addCustomer(Customer[] customers) {
        //check for validity of array
        if(customers == null || customers.length == 0){
            return false;
        }
        boolean allAdded = true;//initially true
        for(int i = 0; i<customers.length; i++){
            if(this.addCustomer(customers[i]) == false){
                allAdded = false;//if even one customer is invalid, add others, but overall return false
            }
        }
        return allAdded;
    }

    /**
     * Loops through the customer store to check if a customer with the same id exists
     *
     * @param id    ID of the customer to be found
     * @return      If found, return customer
     */
    public Customer getCustomer(Long id) {

        for(int i = 0; i<customerArray.size(); i++){
            if(id.equals(customerArray.get(i).getID())){
                return customerArray.get(i);
            }
        }
        return null;
    }
    /**
     * Return array of customers in store ordered by id
     *
     * @return  the return value of getCustomers(Customer[] customers)
     */
    public Customer[] getCustomers() {
        // TODO
        Customer Customer[] = new Customer[customerArray.size()];
        for(int i = 0; i<customerArray.size(); i++){
            Customer[i] = customerArray.get(i);
        }
        return getCustomers(Customer);
    }

    /**
     * This sort method is quickSort, which breaks down the input array into smaller arrays recursively
     * The smaller arrays are then sorted, and join together to form the overall sorted array
     * Only continue if elements are not sorted.
     *
     * @param inputArray    Input Array of customer objects
     * @param indexStart    Starting index to be sorted by
     * @param end           Last index to be sorted till
     */
    public void quickSort(Customer inputArray[], int indexStart, int end) {
        if (indexStart < end) {
            int partitionIndex = partition(inputArray, indexStart, end);
            //recursion to split initial arrays into smaller arrays
            quickSort(inputArray, indexStart, partitionIndex-1);
            quickSort(inputArray, partitionIndex+1, end);
        }
    }

    /**
     * This method is similar to quickSort(), however it calls partitionString, since the sorting has to be done by name then ID, not just ID.
     *
     * @param inputArray    Input array of type customer[]
     * @param indexStart    Starting index of array to be sorted
     * @param end           Last index to be sarted till
     */
    public void quickSortString(Customer inputArray[], int indexStart, int end){
        if (indexStart < end) {
            int partitionIndex = partitionString(inputArray, indexStart, end);

            quickSortString(inputArray, indexStart, partitionIndex-1);
            quickSortString(inputArray, partitionIndex+1, end);
        }
    }

    /**
     * Using the last element's last name as the pivot, compare all customers (in input) last names against the pivot.
     * If the pivot has a last name alphabetticaly higher than the compared customer, then that customer is swapped with the pivot.
     * If the pivot has a same last name as the compared customer, repeat the above comparison for the first name.
     * If the pivot has the same first name, compare IDs
     * quick sort is a suitable algorithm as it takes O(nlogn) on average
     * @param custArray     Input array to be sorted
     * @param first         Index to start sorting by
     * @param end           Last index to sort by
     * @return              The index by which the array is split
     */
    private int partitionString(Customer custArray[], int first, int end) {
        String pivot = custArray[end].getLastName();//compare by last name initially
        int i = (first-1);

        for (int j = first; j < end; j++) {
            if (custArray[j].getLastName().compareToIgnoreCase(pivot) < 0) {
                i++;

                Customer swapTemp = custArray[i];//swap the elements if last name is lower than the pivot's last name
                custArray[i] = custArray[j];
                custArray[j] = swapTemp;//use temp customer object to swap array elements
            }
            if(custArray[j].getLastName().compareToIgnoreCase(pivot) == 0){
                //compare by firstname if last names is same
                if(custArray[j].getFirstName().compareToIgnoreCase(custArray[end].getFirstName()) < 0){
                    i++;

                    Customer swapTemp = custArray[i];//swap elements if first name is lower than pivot's first name
                    custArray[i]=custArray[j];
                    custArray[j]=swapTemp;
                }
                if(custArray[j].getFirstName().compareToIgnoreCase(custArray[end].getFirstName()) == 0){
                    //compare by id if both names are exactly same
                    long pivotID = custArray[end].getID();
                    if (custArray[j].getID() <= pivotID) {
                        i++;
                        Customer swapTempCustomer = custArray[i];
                        custArray[i] = custArray[j];
                        custArray[j] = swapTempCustomer;
                    }
                }
            }
        }
        Customer swapTemp = custArray[i+1];
        custArray[i+1] = custArray[end];//swap the last customer
        custArray[end] = swapTemp;

        return i+1;
    }

    /**
     * Using the last elements ID, compare all customers(in input array) ID to the last element's ID.
     * If it is lower than the last element, then swap the elements
     * @param customerArray     Input Array to srt
     * @param first             Index to start sorting by
     * @param end               Inde x to finish sorting by
     * @return                  Index to further split the array
     */
    private int partition(Customer customerArray[], int first, int end) {
        long pivot = customerArray[end].getID();
        int i = first-1;

        for (int j = first; j < end; j++) {
            //compare all elements in array specified by the index, by pivot ID.
            if (customerArray[j].getID() <= pivot) {
                i++;
                Customer tempCustomer = customerArray[i];
                customerArray[i] = customerArray[j];
                customerArray[j] = tempCustomer;
            }
        }

        Customer tempCustomerTwo = customerArray[i+1];
        customerArray[i+1] = customerArray[end];//swap the last customer
        customerArray[end] = tempCustomerTwo;

        return i+1; //index to further split the array
    }

    /**
     * Sorts input array by calling the quickSort method
     *
     * @param customers     Array of customer objects
     * @return              Same array, sorted by ID
     */
    public Customer[] getCustomers(Customer[] customers) {
        for(int i = 0; i<customers.length; i++){
            //validation takes O(n)
            if(dataChecker.isValid(customers[i]) == false){
                return new Customer[0];
            }
        }
        quickSort(customers, 0, customers.length - 1);
        return customers; //returns same array, but sorted
    }
    /**
     * Converts the customer store arraylist into an array
     * @return      The return value of the getCustomersByName(Customer[] customers)
     */
    public Customer[] getCustomersByName() {

        Customer Customer[] = new Customer[customerArray.size()];//same size array as list
        for(int i = 0; i<customerArray.size(); i++){
            Customer[i] = customerArray.get(i);
        }
        return getCustomersByName(Customer);
    }

    /**
     * Inputs the customers array and calls the quickSortString method which sorts by name
     *
     * @param customers     Array of type customer[]
     * @return              Same array as input array, but sorted by name and ID
     */
    public Customer[] getCustomersByName(Customer[] customers) {
        for(int i = 0; i<customers.length; i++){
            //validation takes O(n)
            if(dataChecker.isValid(customers[i]) == false){
                return new Customer[0];
            }
        }
        quickSortString(customers, 0, customers.length - 1);
        return customers;
    }

    /**
     * Check if the searchTerm is empty and return empty customer array
     * Use StringFormatter class method convertAccentsFaster to strip accents from searchTerm
     * Convert the resulting searchTermConvertedFaster to lowercase and trim leading and trailing spaces.
     * Loop through customer store, and concatenate the first and last name
     * Use .contains() method to check if the search term is a sub string of any customers full name.
     * Use an intermediate arraylist since we do not know how many Customers will have matching names
     * Convert arraylist to array after searching, and sort this array by name
     *
     * @param searchTerm    The string to be searched for
     * @return              Matching array of customers who have a substring of the search term
     */
    public Customer[] getCustomersContaining(String searchTerm) {
        // TODO

        if(searchTerm == ""){
            return new Customer[0];
        }
        matchingCustomers.clear();
        String searchTermConverted = stringFormatter.convertAccents(searchTerm);
        String searchTermConvertedFaster = stringFormatter.convertAccentsFaster(searchTerm);

        searchTermConvertedFaster = searchTermConvertedFaster.toLowerCase().trim();//remove trailing+leading spaces


        for(int i = 0; i<customerArray.size(); i++){
            //concatenate strings to have one string value to compare
            String fullName = customerArray.get(i).getFirstName() +" "+ customerArray.get(i).getLastName();
            fullName = fullName.toLowerCase();


            if(fullName.contains(searchTermConvertedFaster)){
                matchingCustomers.add(customerArray.get(i));//add to arraylist
            }

        }
        Customer[] queriedCustomers = new Customer[matchingCustomers.size()];
        for(int j = 0; j<matchingCustomers.size(); j++){
            queriedCustomers[j] = matchingCustomers.get(j);//convert to array
        }
        quickSortString(queriedCustomers, 0, queriedCustomers.length - 1);//sort by name
        return queriedCustomers;
    }

}
