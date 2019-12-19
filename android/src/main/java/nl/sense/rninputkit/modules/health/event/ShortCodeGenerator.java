package nl.sense.rninputkit.modules.health.event;

import java.security.SecureRandom;

/**
 * Created by panjiyudasetya on 7/24/17.
 */

public class ShortCodeGenerator {
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();
    private ShortCodeGenerator() { }

    public static String generateEventID() {
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        return currentTimeInSeconds + ":" + getCode(4);
    }

    private static String getCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(
                    AB.charAt(
                            rnd.nextInt(AB.length())
                    )
            );
        }
        return sb.toString();
    }
}
