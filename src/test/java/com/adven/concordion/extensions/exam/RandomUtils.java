package com.adven.concordion.extensions.exam;

import org.apache.commons.lang3.RandomStringUtils;

public final class RandomUtils {


    private RandomUtils() {
    }

    public static int anyInt() {
        return org.apache.commons.lang3.RandomUtils.nextInt();
    }

    public static long anyLong() {
        return org.apache.commons.lang3.RandomUtils.nextLong();
    }

    public static String anyString() {
        return RandomStringUtils.randomAlphabetic(10);
    }

}
