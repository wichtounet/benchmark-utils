package com.wicht.benchmark.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A simple utility class for benchmarks.
 *
 * @author Baptiste Wicht
 * @version 1.0
 */
public class BenchUtils {
    private static final Random random = new Random();

    /**
     * Create a collection containing the given number of random integers.
     *
     * @param size The size of the collection.
     *
     * @return A Collection containing the given number of random integers.
     *
     * @throws IllegalArgumentException If size is less than 0.
     */
    public static Collection<Integer> newRandomIntegerList(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be less than 0. ");
        }

        Collection<Integer> integers = new ArrayList<Integer>(size);

        for (int i = 0; i < size; i++) {
            integers.add(random.nextInt());
        }

        return integers;
    }

    /**
     * Create a Collection containing the given size of the given strings. The order of strings in the collection is
     * random.
     *
     * @param size    The size of the collection to create.
     * @param strings The strings to use to fill the collection.
     *
     * @return A Collection containing the given number of strings taken from the strings.
     *
     * @throws IllegalArgumentException If size is less than 0 or if strings doesn't contains at least one String.
     */
    public static Collection<String> newRandomStringList(int size, String... strings) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be less than 0. ");
        }

        if (strings == null || strings.length == 0) {
            throw new IllegalArgumentException("strings must contains at least one String. ");
        }

        List<String> list = new ArrayList<String>(size);

        for (String string : strings) {
            for (int i = 0; i < size / 3; i++) {
                list.add(string);
            }
        }

        if (list.size() < size) {
            for (int i = list.size(); i < size; i++) {
                list.add(strings[i % strings.length]);
            }
        }

        Collections.shuffle(list, random);

        return list;
    }
}