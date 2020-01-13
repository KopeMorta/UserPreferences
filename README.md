# UserPreferences

Small Utility for manage User Preferences

## What for

Provides the functionality of two-way object update.
There is an object in which user settings are stored, and we want these parameters to be automatically updated in the file if the object has been changed. 
Conversely, the object will be updated when the user edits the file.


## Using

More in [Example Package](./src/main/java/example)
```java
import com.kopemorta.userpreferences.Config;
import com.kopemorta.userpreferences.UserPreferences;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class Example {

    public static void main(String[] args) {
        final Config config = Config.builder() // if need
                .delay(100)
                .initialDelay(20)
                // etc
                .build();

        UserPreferences.init(config); // if don't use custom config - skip init

        final Settings settings = Settings.getInstance();
        UserPreferences.registerInstance(settings); // register instance

        /*
         * Done.
         * You can change settings object as you want.
         * Edit file, call setters, by reflection, etc.
         */
        settings.setConTimeout(231);
        settings.setMyCustomFile(new File("new file"));
        settings.setMyProxy(Proxy.NO_PROXY);
        settings.setMySuperString("MY VERY SUPER STRING !!!");
    }
}


@EqualsAndHashCode(callSuper = false)
class Settings extends UserPreferences {

    private static final File PREF_FILE = new File("settings.cfg");

    private static volatile Settings instance;

    public static Settings getInstance() {
        Settings localInstance = instance;
        if (localInstance == null) {
            synchronized (Settings.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Settings();
                }
            }
        }
        return localInstance;
    }


    @Getter
    @Setter
    private File myCustomFile = new File("mysupefile.txt");

    @Getter
    @Setter
    private Proxy myProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9150));

    @Getter
    @Setter
    private long timeout = 25;

    @Getter
    @Setter
    private long conTimeout = 10;

    @Getter
    @Setter
    private long readTimeout = 10;

    @Getter
    @Setter
    private String mySuperString = "aooaooo";


    private Settings() {
    }


    @Override
    protected File userPreferencesFile() {
        return PREF_FILE;
    }

}
```
