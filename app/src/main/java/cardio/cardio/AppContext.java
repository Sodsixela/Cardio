package cardio.cardio;

import android.app.Application;

/**
 * Created by Administrateur on 19-Feb-18.
 */

class AppContext extends Application {
    private static AppContext instance;

    public AppContext() {
        instance = this;
    }

    public static AppContext getInstance() {
        return instance;
    }
}
