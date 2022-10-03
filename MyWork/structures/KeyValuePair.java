package uk.ac.warwick.cs126.structures;

public class KeyValuePair<K extends Comparable<K>,V> implements Comparable<KeyValuePair<K,V>> {

    protected K key;
    protected V value;

    public KeyValuePair(K k, V v) {
        key = k;
        value = v;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public int compareTo(KeyValuePair<K,V> o) {
        return o.getKey().compareTo(this.getKey());
    }
}
