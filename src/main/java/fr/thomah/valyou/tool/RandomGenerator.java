package fr.thomah.valyou.tool;

import java.util.Random;

public class RandomGenerator {

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String randomString() {
        int count = 12;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String randomColor() {
        final Random random = new Random();
        final String[] letters = "0123456789ABCDEF".split("");
        String color = "";
        for (int i = 0; i < 6; i++) {
            color += letters[Math.round(random.nextFloat() * 15)];
        }
        return color;
    }
}
