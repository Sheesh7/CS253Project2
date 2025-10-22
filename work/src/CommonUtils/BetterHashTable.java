package CommonUtils;

import java.util.Objects;

/**
 * Implements our {@link BetterHashTableInterface} and adds two constructors.
 * <p>
 * <b>251 students: You are explicitly forbidden from using java.util.Hashtable (including any subclass
 *   like HashMap) and any other java.util.* library EXCEPT java.util.Arrays and java.util.Objects.
 *   Write your own implementation of a hash table.</b>
 *
 * @implNote Implements a hash table using an array with initial capacity 8.
 *
 * @param <K> Type of key the hash table is holding
 * @param <V> Type of value the hash table is holding
 */
public class BetterHashTable<K, V> implements BetterHashTableInterface<K, V> {
    /**
     * Initial size of hash table.
     */
    private final int INIT_CAPACITY = (1 << 4) + 3; //16+3
    /**
     * Determines the maximum ratio of number of elements to array size.
     * <p>
     * If the number of elements stored is > INCREASE_LOAD_FACTOR, it should increase
     *   the capacity of the array according to INCREASE_FACTOR.
     * <p>
     * Our hash table will not decrease its size for the duration of its lifetime.
     */
    private final double INCREASE_LOAD_FACTOR = 0.75;
    private final double DECREASE_LOAD_FACTOR = 0.1;
    /**
     * Determines how much to increase the array's capacity.
     * <p>
     * If the array needs to increase in size (see load factor), it should prefer to
     *   increase by the old capacity * INCREASE_FACTOR.  If it cannot increase by
     *   that much (max int), it should increase by some small quantity (<100).
     */
    private final int INCREASE_FACTOR = 2;
    /**
     * "some small quantity (<100)"
     */
    private final int CAPACITY_INCREMENT = 1 << 20;     // ~1_000_000

    /**
     * Simple storage unit for our hash table
     */
    private static class Node<K, V> {
        final K key;
        V value;

        Node(K key, V value) { this.key = key; this.value = value; }

        //default intellij-generated tostring
        @Override
        public String toString() {
            return "Node{" + "key=" + key + ", value=" + value + '}';
        }

        //default intellij-generated equals
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node<?, ?> node = (Node<?, ?>) o;
            return Objects.equals(key, node.key) && Objects.equals(value, node.value);
        }
    }

    /**
     * Default entry to use when marking a slot as deleted.
     *
     * <bold>253 students: When you remove an entry in the table, you cannot simply set it to null.
     *    Because we are using quadratic probing, there could be a "chain" of probing misses
     *    which would be disrupted if we just set the current node to null.  Having a "deleted"
     *    default value allows us to continue successfully searching for any object whose hash
     *    matches the object we are deleting.  It is up to you to figure out the best way to
     *    use this field, no further guidance will be given.</bold>
     */
    private final Node<K, V> DELETED = new Node<>(null, null);

    /**
     * Array to store elements  (according to the implementation
     *   note in the class header comment).
     */
    Node<K, V>[] table;
    private int size;


    /**
     * Constructs the hash table with a default size
     */
    @SuppressWarnings("unchecked")
    public BetterHashTable() {
        table = (Node<K, V>[]) new Node[INIT_CAPACITY];
        size = 0;
    }

    /**
     * Constructor that initializes the hash table with an initial capacity
     * @param initialCapacity initial table capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    @SuppressWarnings("unchecked")
    public BetterHashTable(int initialCapacity) throws IllegalArgumentException {
        if (initialCapacity < 0) throw new IllegalArgumentException("Capacity cannot be negative");
        table = (Node<K, V>[]) new Node[initialCapacity];
        size = 0;
    }

    /**
     * Returns a positive hash of the given thing
     * @param thing thing to hash
     * @return positive hashed value
     */
    private int usefulHash(K thing) {
        return Math.abs(Objects.hash(thing));
    }


    /**
     * Places the item in the hash table. Passing key=null will not change the state of the table.
     * Passing a key already in the table will modify the original entry in the table.
     *
     * @param key   key to associate with the value
     * @param value item to store in the hash table
     */
    @Override
    public void insert(K key, V value) {
        if (key == null) return;
        if ((double) (size + 1) / table.length > INCREASE_LOAD_FACTOR) {
            resize(table.length * INCREASE_FACTOR);
        }
        int index = probeIndex(key, true);
        if (index == -1) return;
        Node<K, V> node = table[index];
        if (node == null || node == DELETED) {
            table[index] = new Node<>(key, value);
            size++;
        } else {
            node.value = value;
        }
    }


    /**
     * Removes the key, value pair associated with the given key
     *
     * @param key key/value to remove
     */
    @Override
    public void remove(K key) {
        if (key == null) return;
        int hash = usefulHash(key);
        int capacity = table.length;
        for (int i = 0; i < capacity; i++) {
            int index = (hash + i * i) % capacity;
            Node<K, V> node = table[index];

            if (node == null) return;
            if (node == DELETED) continue;
            if (Objects.equals(node.key, key)) {
                table[index] = DELETED;
                size--;
                return;
            }
        }
    }

    /**
     * Retrieves a value based on the given key
     *
     * @param key key to search by
     * @return value associated with the key, or <code>null</code> if it does not exist
     */
    @Override
    public V get(K key) {
        if (key == null) return null;
        int hash = usefulHash(key);
        int capacity = table.length;
        for (int i = 0; i < capacity; i++) {
            int index = (hash + i * i) % capacity;
            Node<K, V> node = table[index];
            if (node == null) return null;
            if (node == DELETED) continue;
            if (Objects.equals(node.key, key)) return node.value;
        }
        return null;
    }

    /**
     * Returns <code>true</code> if this hash table contains the given key.
     *
     * @param key key to check for
     * @return true iff the hash table contains a mapping for the specified key,
     *          as ultimately determined by the equals method
     */
    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * Empties the hash table.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        table = (Node<K, V>[]) new Node[INIT_CAPACITY];
        size = 0;
    }

    /**
     * Returns the number of items in the hash table
     *
     * @return integer representing the number of elements in the hash table
     */
    @Override
    public int size() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    // extra methods for help
    private int probeIndex(K key, boolean forInsert) {
        int hash = usefulHash(key);
        int capacity = table.length;
        for (int i = 0; i < capacity; i++) {
            int index = (hash + i * i) % capacity;
            Node<K, V> node = table[index];
            if (node == null) {
                if (forInsert) {
                    return index;
                } else {
                    return -1;
                }
            }
            if (node == DELETED) {
                if (forInsert) {
                    return index;
                } else {
                    continue;
                }
            }
            if (Objects.equals(node.key, key)) {
                return index;
            }
        }
        return -1;
    }
    private void resize(int newCapacity) {
        Node<K, V>[] oldTable = table;
        table = (Node<K, V>[]) new Node[newCapacity];
        size = 0;
        for (Node<K, V> node : oldTable) {
            if (node != null && node != DELETED) {
                insert(node.key, node.value);
            }
        }
    }
}
