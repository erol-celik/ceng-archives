import java.util.Iterator;
import java.util.NoSuchElementException;


public class HashedDictionary<K, V> implements DictionaryInterface<K, V> {
    // The dictionary:
    private int numberOfEntries;
    private static final int DEFAULT_CAPACITY = 31;     // Must be prime
    private static final int MAX_CAPACITY = 100000;
    private double loadFactor=0.5; // Default fraction of hash table
    private boolean collisionHandling; //true for lineer probing false for double hashing.
    private CalculateHash hashCalculator;
    private int collisionCount=0;
    private Entry<K, V>[] hashTable;
    private int tableSize;  //  must be prime
    private boolean integrityOK = false;


    @SuppressWarnings("unchecked")
    public HashedDictionary() {
        tableSize = DEFAULT_CAPACITY;
        hashTable = (Entry<K, V>[]) new Entry[tableSize];
        numberOfEntries = 0;
        integrityOK = true;
        hashCalculator = new CalculateHash();
        collisionHandling=true;//set linear probing as default.
    }//end constructure



    public V add(K key, V value) {
        checkIntegrity();
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value cannot be null.");
        }
        int index = hashCalculator.calculateHash(key.toString())%tableSize;
        int originalIndex = index;
        int stepSize;

        if (collisionHandling) {stepSize = 1;} // for lineerporbing step is 1
        else {stepSize = getDoubleHashStep(key);} // for double hashing gets step

        while (hashTable[index] != null) {
            if (hashTable[index].key.equals(key)) {
                V oldValue = hashTable[index].value;
                hashTable[index].value = value; // update value
                return oldValue;
            }
            collisionCount++;

            if (collisionHandling) {index = (index + stepSize) % tableSize;} //For Lineer Probing goes next index
            else {index = (index + stepSize) % tableSize;} // For Double Hashing determines next step to go

            if (index == originalIndex) {
                throw new IllegalStateException("Hash table is full.");
            }
        }
        hashTable[index] = new Entry<>(key, value);
        numberOfEntries++;

        if (isHashTableTooFull()) {
            enlargeTable();
        }

        return null;
    }//end add

    public V remove(K key) {
        checkIntegrity();
        int index = getHashIndex(key);
        int originalIndex = index;
        int stepSize;

        if (collisionHandling) {stepSize = 1;} // for linearporbing step is 1
        else {stepSize = getDoubleHashStep(key);} // for double hashing gets step

        while (hashTable[index] != null) {
            if (hashTable[index].key.equals(key)) {
                V removedValue = hashTable[index].value;
                hashTable[index].setRemoved();
                hashTable[index] = new Entry<>(null, null);//convert to dummy value
                numberOfEntries--;
                return removedValue;
            }
            index = (index + stepSize) % tableSize;
            if (index == originalIndex) {
                break;
            }
        }
        return null;
    }//end remove

    public V getValue(K key) {
        checkIntegrity();
        int index = getHashIndex(key);
        int originalIndex = index;
        int step = 1;

        while (hashTable[index] != null) {
            if (hashTable[index].getKey().equals(key)) {
                return hashTable[index].getValue();
            }
            if (collisionHandling) {            //getValue for lineerProbing
                index = (index + 1) % hashTable.length;
            } else {                            //getValue for Double Hashing
                step = getDoubleHashStep(key);
                index = (index + step) % hashTable.length;
            }
            if (index == originalIndex) {
                break;
            }
        }
        return null;
    }//end getValue

    public void setCollisionHandling(boolean collisionHandling) {
        this.collisionHandling = collisionHandling;
    }//end setCollisionHandling

    public void setLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }//end setLoadFactor

    public boolean contains(K key) {
        return getValue(key) != null;
    } // end contains

    public boolean isEmpty() {
        return numberOfEntries == 0;
    } // end isEmpty

    public int getSize() {
        return numberOfEntries;
    } // end getSize

    public final void clear() {
        checkIntegrity();
        for (int index = 0; index < hashTable.length; index++)
            hashTable[index] = null;
        numberOfEntries = 0;
    } // end clear

    private int getHashIndex(K key) {
        String stringKey = (String) key;
        int hashIndex = (hashCalculator.calculateHash(stringKey)) % hashTable.length;
        return hashIndex;
    }//end getHashIndex

    private void enlargeTable() {    // Increases the size of the hash table to a prime >= twice its old size.

        int newSize = getNextPrime(2 * tableSize);
        @SuppressWarnings("unchecked")
        Entry<K, V>[] oldTable = hashTable;
        hashTable = (Entry<K, V>[]) new Entry[newSize];
        tableSize = newSize;
        numberOfEntries = 0;
        for (Entry<K, V> entry : oldTable) {
            if (entry != null && entry.key != null) {
                add(entry.key, entry.value);
            }
        }
    }//end enlargeTable

    private boolean isHashTableTooFull() {
        return numberOfEntries > loadFactor * hashTable.length;
    } // end isHashTableTooFull

    private int getNextPrime(int integer) {
        if (integer % 2 == 0) {
            integer++;
        } // end if
        // test odd integers
        while (!isPrime(integer)) {
            integer = integer + 2;
        } // end while
        return integer;
    } // end getNextPrime

    private int getDoubleHashStep(K key) {//determines next double hashing step
        int primeStep = tableSize - 1;
        while (!isPrime(primeStep)) {
            primeStep--;
        }
        int hashStep = primeStep - (hashCalculator.calculateHash(key.toString()) % primeStep);
        if (hashStep < 0) {
            hashStep += tableSize;
        }
        return hashStep;
    }//end getDoubleHashStep

    private boolean isPrime(int integer) {    // Returns true if the given intege is prime.

        boolean result;
        boolean done = false;

        // 1 and even numbers are not prime
        if ((integer == 1) || (integer % 2 == 0)) {
            result = false;
        }

        // 2 and 3 are prime
        else if ((integer == 2) || (integer == 3)) {
            result = true;
        } else // integer is odd and >= 5
        {
            assert (integer % 2 != 0) && (integer >= 5);

            // a prime is odd and not divisible by every odd integer up to its square root
            result = true; // assume prime
            for (int divisor = 3; !done && (divisor * divisor <= integer); divisor = divisor + 2) {
                if (integer % divisor == 0) {
                    result = false; // divisible; not prime
                    done = true;
                } // end if
            } // end for
        } // end if

        return result;
    } // end isPrime

    private void checkIntegrity() {    // Throws an exception if this object is not initialized.
        if (!integrityOK)
            throw new SecurityException("HashedDictionary object is corrupt.");
    } // end checkIntegrity

    public CalculateHash getHashCalculator() {
        return hashCalculator;
    }

    public int getCollisionCount() {
        return collisionCount;
    }




    public Iterator<K> getKeyIterator() {
        return new KeyIterator();
    }

    public Iterator<V> getValueIterator() {
        return new ValueIterator();
    }

    private class KeyIterator implements Iterator<K> {
        private int currentIndex;
        private int numberLeft;

        public KeyIterator() {
            currentIndex = 0;
            numberLeft = numberOfEntries;
        }

        @Override
        public boolean hasNext() {
            return numberLeft > 0;
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            while (hashTable[currentIndex] == null || hashTable[currentIndex].isRemoved()) {
                currentIndex++;
            }

            K key = hashTable[currentIndex].getKey();
            currentIndex++;
            numberLeft--;
            return key;
        }
    }

    private class ValueIterator implements Iterator<V> {
        private int currentIndex;
        private int numberLeft;

        public ValueIterator() {
            currentIndex = 0;
            numberLeft = numberOfEntries;
        }

        @Override
        public boolean hasNext() {
            return numberLeft > 0;
        }

        @Override
        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            while (hashTable[currentIndex] == null || hashTable[currentIndex].isRemoved()) {
                currentIndex++;
            }

            V value = hashTable[currentIndex].getValue();
            currentIndex++;
            numberLeft--;
            return value;
        }
    }





    public class CalculateHash {
        private boolean hashMethod; // true for SSF, false for PAF

        // Sets the hashing method to SSF or PAF
        public void setHashMethod(boolean isSSF) {
            this.hashMethod = isSSF;
        }

        // Chooses which hashing method to use (SSF or PAF)
        public int calculateHash(String key) {
            if (hashMethod) {
                return getSSFHash(key);  // SSF
            } else {
                return getPAFHash(key);  // PAF
            }
        }

        // SSF Hash function
        private int getSSFHash(String key) {
            int length = key.length();
            int hash = 0;
            for (int i = 0; i < length; i++) {
                hash += key.charAt(i);
            }
            return hash;
        }

        // PAF Hash function
        private int getPAFHash(String key) {
            int hash = 0;
            int z = 41;// a prime number
            int mod=tableSize;//for handling possible overflows
            for (int i = 1; i < key.length(); i++) {
                hash = (hash * z + key.charAt(i))%mod;
            }
            return hash;
        }
    }



    public class Entry<K, V> {
        private K key;
        private V value;
        private boolean isRemoved;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.isRemoved = false;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }


        public boolean isRemoved() {
            return isRemoved;
        }

        public void setRemoved() {
            this.isRemoved = true;
        }

    }

} // end HashedDictionary


