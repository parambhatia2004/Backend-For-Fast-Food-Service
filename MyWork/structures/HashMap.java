package uk.ac.warwick.cs126.structures;


@SuppressWarnings("unchecked")
public class HashMap<K extends Comparable<K>,V> implements IMap<K,V> {

    protected KeyValuePairLinkedList[] table;

    public HashMap() {
        /* next prime number after the largest amount of data needed to store (string formatter) */
        this(1511);
    }

    public HashMap(int size) {
        table = new KeyValuePairLinkedList[size];
        initTable();
    }

    // INCOMPLETE.
    public int find(K key) {
        int comparison = 1;
        //returns the number of comparisons required to find element using Linear Search.
        //Few comparisons compared to ordinary linear search due to key-value pairs.
        int index = hash(key) % table.length;
        ListElement<KeyValuePair<K,V>> traverse = (table[index]).getHead();
        while (traverse.getValue().getKey().compareTo(key) != 0) {
            comparison++;
            traverse = traverse.getNext();
        }
        return comparison;
    }

    protected void initTable() {
        for(int i = 0; i < table.length; i++) {
            table[i] = new KeyValuePairLinkedList<>();
        }
    }

    protected int hash(K key) {
        int code = key.hashCode();
        int absVal = Math.abs(code);
        return absVal;
    }


    public void add(K key, V value) {
        int hash_code = hash(key);
        int location = hash_code % table.length;


        table[location].add(key,value);
    }

    public V get(K key) {
        int hash_code = hash(key);
        int location = hash_code % table.length;

        ListElement<KeyValuePair> ptr = table[location].head;

        if(table[location].get(key) == null){
            return null;
        }
        return (V)table[location].get(key).getValue();
    }

    public boolean remove(K key){
        int hash_code = hash(key);
        int location = hash_code % table.length;
        ListElement<KeyValuePair> ptr = table[location].head;
        if(ptr == null || ptr.getNext() == null){
            return false;
        }
        if(table[location].get(key) == null){
            return false;
        }
        V val = (V)table[location].get(key).getValue();
        
        while(ptr.getNext().getNext() != null){
            if(val.equals(ptr.getNext().getValue())){
                ptr.setNext(ptr.getNext().getNext());
                return true;
            }
            ptr=ptr.getNext();
        }
        if(val.equals(ptr.getNext().getValue())){
            ptr.setNext(null);
            return true;
        }
        return false;
    }
    //Check if the value exists in the map
    public boolean contains(K key) {
        int hash_code = hash(key);
        int location = hash_code % table.length;

        ListElement<KeyValuePair> ptr = table[location].head;

        if(table[location].get(key) == null){
            return false;
        } else return true;
    }
}
