package uk.ac.warwick.cs126.util;

import uk.ac.warwick.cs126.interfaces.IDataChecker;

import uk.ac.warwick.cs126.models.Customer;
import uk.ac.warwick.cs126.models.Restaurant;
import uk.ac.warwick.cs126.models.Favourite;
import uk.ac.warwick.cs126.models.Review;

import java.util.Date;

public class DataChecker implements IDataChecker {

    private int idCount;

    public DataChecker() {
        // Initialise things here
    }

    public Long extractTrueID(String[] repeatedID) {
        // TODO
        int IDLength = repeatedID.length;
        Long consensusVal;
        if (IDLength != 3){
            return null;
        }
        else {
            if(repeatedID[0].equals(repeatedID[1])){
                consensusVal = Long.parseLong(repeatedID[0]);
                return consensusVal;
            }
            else if(repeatedID[0].equals(repeatedID[2])){
                consensusVal = Long.parseLong(repeatedID[0]);
                return consensusVal;
            }
            else if (repeatedID[1].equals(repeatedID[2])){
                consensusVal = Long.parseLong(repeatedID[1]);
                return consensusVal;
            }
            else{
                return null;
            }
        }
    }
    /**
     * This method checks if the inputID is valid, and if it is not the right size, or does not match requirements return false.
     * Stores the occurrence of each digit as the index of array
     * @param inputID
     * @return
     */
    public boolean isValid(Long inputID) {
        // TODO
        if(inputID == null){
            return false;
        }
        String str = Long.toString(inputID);
        char[] IDChar = str.toCharArray();
        int[] digit_count = new int[10];
        int checkDigit = 0;
        for (int i = 0; i < IDChar.length; i++){
            if (IDChar[i] > '0' && IDChar[i] <= '9'){
                checkDigit++;//checks for valid digits
            } else{

                return false;
            }
            if(checkDigit > 16){
                return false;
            }
            int digit = (int)(inputID % 10);
            inputID = inputID/10;
            digit_count[digit] += 1;
            if(digit_count[digit] > 3){
                return false;
            }
        }
        if(checkDigit < 16){
            return false;
        }
        return true;
    }
    /**
     * Uses lazy or operator, so if even one of the conditions is true(if the attribute is false) then return false
     * This allows for efficiency iin validation.
     * @param customer
     * @return
     */
    public boolean isValid(Customer customer) {
        // TODO
        if (customer != null){
            if (this.isValid(customer.getID()) == false || customer.getFirstName() == null || customer.getLastName() == null || customer.getDateJoined() == null || customer.getLatitude() == 0.0f || customer.getLongitude() == 0.0f){//use lazy || as it will check for only one to be true, i.e. only one field to be null
                return false;
            }
            return true;//valid customer
        }
        return false;//if customer is of null type
    }

    public boolean isValid(Restaurant restaurant) {
        // TODO
        if (restaurant != null){
            Long restaurantID = this.extractTrueID(restaurant.getRepeatedID());
            if(restaurantID == null){
                return false;
            }
            if (this.isValid(restaurantID) == false || restaurant.getName() == null || restaurant.getOwnerFirstName() == null || restaurant.getOwnerLastName() == null || restaurant.getCuisine() == null || restaurant.getEstablishmentType() == null || restaurant.getPriceRange() == null || restaurant.getDateEstablished() == null || restaurant.getLatitude() == 0.0f || restaurant.getLongitude() == 0.0f || restaurant.getLastInspectedDate() == null) {
                return false;
            }
            
            if(restaurant.getLastInspectedDate().compareTo(restaurant.getDateEstablished()) < 0){
                return false;
            }
            if(restaurant.getFoodInspectionRating() < 0 || restaurant.getFoodInspectionRating() > 5){
                return false;
            }
            if(restaurant.getWarwickStars() < 0 || restaurant.getWarwickStars() > 3 ){
                return false;
            }
            if((restaurant.getCustomerRating() != 0.0f) && (restaurant.getCustomerRating() < 1.0f || restaurant.getCustomerRating() >5.0f)){
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isValid(Favourite favourite) {
        if (favourite != null){
            if (this.isValid(favourite.getID()) == false || this.isValid(favourite.getCustomerID()) == false || this.isValid(favourite.getRestaurantID()) == false || favourite.getDateFavourited() == null){
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isValid(Review review) {
        // TODO
        return false;
    }
}