package me.josielcm.jcm.api.utils;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Enhanced utility class for generating random values.
 * Provides methods for generating random numbers, strings, UUIDs,
 * selecting random elements from collections, and more.
 * 
 * @author JosielCM
 */
@UtilityClass
public class RandomUtils {
    
    // SecureRandom for cryptographically strong random values
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // Constants for generating strings
    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHA_CAPS = ALPHA + ALPHA.toUpperCase();
    private static final String ALPHA_NUMERIC = ALPHA_CAPS + "0123456789";
    private static final String ALPHA_NUMERIC_SYMBOLS = ALPHA_NUMERIC + "!@#$%^&*()_+-=[]{}|;:,.<>?";

    /**
     * Generates a random integer between min and max (inclusive)
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random integer between min and max
     */
    public static int randomInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generates a random integer between min and max (exclusive)
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return Random integer between min and max-1
     */
    public static int randomIntExclusive(int min, int max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /**
     * Generates a random boolean value
     *
     * @return Random boolean (true or false)
     */
    public static boolean randomBool() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Generates a random float between 0 and 1
     *
     * @return Random float between 0.0 and 1.0
     */
    public static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }
    
    /**
     * Generates a random float within a range
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random float between min and max
     */
    public static float randomFloat(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }
    
    /**
     * Generates a random double between 0 and 1
     *
     * @return Random double between 0.0 and 1.0
     */
    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }
    
    /**
     * Generates a random double within a range
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random double between min and max
     */
    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
    
    /**
     * Returns true with the given probability
     *
     * @param probability Chance between 0.0 and 1.0
     * @return True if random value is less than probability
     */
    public static boolean chance(double probability) {
        return randomDouble() < probability;
    }
    
    /**
     * Returns true with the given percentage chance
     *
     * @param percentage Chance between 0 and 100
     * @return True if random value is less than percentage
     */
    public static boolean percentChance(double percentage) {
        return chance(percentage / 100.0);
    }
    
    /**
     * Selects a random element from an array
     *
     * @param <T> Array element type
     * @param array Source array
     * @return Random element from array or null if array is empty
     */
    public static <T> T randomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
    
    /**
     * Selects a random element from a collection
     *
     * @param <T> Collection element type
     * @param collection Source collection
     * @return Random element from collection or null if collection is empty
     */
    public static <T> T randomElement(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        
        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            return list.get(ThreadLocalRandom.current().nextInt(list.size()));
        }
        
        // For non-list collections, convert to list first
        List<T> list = new ArrayList<>(collection);
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
    
    /**
     * Selects random elements from a collection
     *
     * @param <T> Collection element type
     * @param collection Source collection
     * @param count Number of elements to select
     * @return List containing random elements
     */
    public static <T> List<T> randomElements(Collection<T> collection, int count) {
        if (collection == null || collection.isEmpty() || count <= 0) {
            return new ArrayList<>();
        }
        
        List<T> source = new ArrayList<>(collection);
        List<T> result = new ArrayList<>(Math.min(count, source.size()));
        
        // If requesting more elements than exist, return all shuffled
        if (count >= source.size()) {
            java.util.Collections.shuffle(source, ThreadLocalRandom.current());
            return source;
        }
        
        // Select random elements without replacement
        for (int i = 0; i < count; i++) {
            int index = ThreadLocalRandom.current().nextInt(source.size());
            result.add(source.remove(index));
        }
        
        return result;
    }
    
    /**
     * Shuffles the given list in place
     *
     * @param <T> List element type
     * @param list List to shuffle
     */
    public static <T> void shuffle(List<T> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        java.util.Collections.shuffle(list, ThreadLocalRandom.current());
    }
    
    /**
     * Generates a random string of specified length
     *
     * @param length Length of the string
     * @param includeNumbers Whether to include numbers
     * @param includeSymbols Whether to include symbols
     * @return Random string
     */
    public static String randomString(int length, boolean includeNumbers, boolean includeSymbols) {
        if (length <= 0) {
            return "";
        }
        
        String chars;
        if (includeSymbols) {
            chars = ALPHA_NUMERIC_SYMBOLS;
        } else if (includeNumbers) {
            chars = ALPHA_NUMERIC;
        } else {
            chars = ALPHA_CAPS;
        }
        
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * Generates a random alphanumeric string
     *
     * @param length Length of the string
     * @return Random alphanumeric string
     */
    public static String randomAlphaNumeric(int length) {
        return randomString(length, true, false);
    }
    
    /**
     * Generates a random alphabetic string
     *
     * @param length Length of the string
     * @return Random alphabetic string
     */
    public static String randomAlpha(int length) {
        return randomString(length, false, false);
    }
    
    /**
     * Generates a random UUID
     *
     * @return Random UUID
     */
    public static UUID randomUUID() {
        return UUID.randomUUID();
    }
    
    /**
     * Generates a cryptographically secure random integer
     *
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Secure random integer
     */
    public static int secureRandomInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return SECURE_RANDOM.nextInt(max - min + 1) + min;
    }
    
    /**
     * Generates a cryptographically secure random bytes array
     *
     * @param length Length of the array
     * @return Array of random bytes
     */
    public static byte[] secureRandomBytes(int length) {
        if (length <= 0) {
            return new byte[0];
        }
        
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Returns a weighted random index based on provided weights
     *
     * @param weights Array of weights
     * @return Random index based on weights
     */
    public static int randomWeightedIndex(double[] weights) {
        if (weights == null || weights.length == 0) {
            return -1;
        }
        
        double totalWeight = 0.0;
        for (double weight : weights) {
            totalWeight += Math.max(0.0, weight);
        }
        
        if (totalWeight <= 0.0) {
            return ThreadLocalRandom.current().nextInt(weights.length);
        }
        
        double value = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double currentWeight = 0.0;
        
        for (int i = 0; i < weights.length; i++) {
            currentWeight += Math.max(0.0, weights[i]);
            if (value < currentWeight) {
                return i;
            }
        }
        
        // Fallback (shouldn't happen with proper weights)
        return weights.length - 1;
    }
    
    /**
     * Selects a random element based on weights
     *
     * @param <T> Element type
     * @param elements Array of elements
     * @param weights Array of weights
     * @return Randomly selected element based on weights
     */
    public static <T> T randomWeightedElement(T[] elements, double[] weights) {
        if (elements == null || elements.length == 0 || weights == null || weights.length == 0) {
            return null;
        }
        
        int index = randomWeightedIndex(weights);
        if (index < 0 || index >= elements.length) {
            return null;
        }
        
        return elements[index];
    }
}