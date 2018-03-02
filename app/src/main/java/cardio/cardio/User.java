package cardio.cardio;

import android.content.Context;

/**
 * Created by Administrateur on 04-Jan-18.
 */

class User {
    private static String name;
    private static String password;
    private static Context c;
    private static final User ourInstance = new User();
    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        User.name = name;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        User.password = password;
    }

    public static Context getC() {
        return c;
    }

    public static void setC(Context c) {
        User.c = c;
    }

    static User getInstance() {
        return ourInstance;
    }

    private User() {
    }
}