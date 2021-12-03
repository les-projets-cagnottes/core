package fr.lesprojetscagnottes.core.common.strings;

import io.jsonwebtoken.Clock;
import io.jsonwebtoken.impl.DefaultClock;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringGenerator {

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final Clock clock = DefaultClock.INSTANCE;

    public static String randomString() {
        int count = 12;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String imageName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        final Date createdDate = clock.now();
        return "IMG_" + df.format(createdDate);
    }

}
