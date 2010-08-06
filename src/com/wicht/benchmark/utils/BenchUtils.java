package com.wicht.benchmark.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * Copyright JTheque (Baptiste Wicht)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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