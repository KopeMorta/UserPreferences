package example;

import com.kopemorta.userpreferences.Config;
import com.kopemorta.userpreferences.UserPreferences;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;


public class SimpleExample {

    private static final File PREF_FILE = new File("user.preferences.txt");
    private static final Config config = Config.builder().build();


    public static void main(String[] args) throws Throwable {
        final SimpleExamplePref preferences = new SimpleExamplePref();
        UserPreferences.init(config); // не обязательно, в нашем случае для проверки
        UserPreferences.registerInstance(preferences); // регистрируем экземпляр, обязательно


        { // Проверяем стандартные настройки
            Util.checkInstance(preferences, 123, 1234L, "my custom string field @!!##^%#^463*)^$%*#RF{F");
            Util.checkFile(PREF_FILE, 123, 1234L, "my custom string field @!!##^%#^463*)^$%*#RF{F");

            System.out.println(preferences);
        }


        { // Обновляем настройки
            preferences.setMyIntegerField(1234567);
            preferences.setMyLongField(123456789123456789L);
            preferences.setMyStringField("my new custom string field");
        }

        { // Проверяем новые настройки
            Thread.sleep(1111); // ждем, что бы файл обновится успел

            Util.checkInstance(preferences, 1234567, 123456789123456789L, "my new custom string field");
            Util.checkFile(PREF_FILE, 1234567, 123456789123456789L, "my new custom string field");

            System.out.println(preferences);
        }


        { // Обновите файл и нажмите любую кнопку
            System.out.println("Обновите файл, что бы он выглядел следующим образом и нажмите ENTER:" + System.lineSeparator() +
                    "{\n" +
                    "  \"myIntegerField\": 1,\n" +
                    "  \"myLongField\": 12,\n" +
                    "  \"myStringField\": \"STRING\"\n" +
                    "}");

            System.in.read();
        }

        { // Проверяем новые настройки
            Thread.sleep(1111); // ждем, что бы файл обновится успел

            Util.checkInstance(preferences, 1, 12, "STRING");
            Util.checkFile(PREF_FILE, 1, 12, "STRING");

            System.out.println(preferences);
        }


        { // Обновляем настройки на стандартные, что бы убедится что при перезапуске всё работает
            preferences.setMyIntegerField(123);
            preferences.setMyLongField(1234L);
            preferences.setMyStringField("my custom string field @!!##^%#^463*)^$%*#RF{F");
        }

        { // Еще раз проверяем новые настройки
            Thread.sleep(1111); // ждем, что бы файл обновится успел

            Util.checkInstance(preferences, 123, 1234L, "my custom string field @!!##^%#^463*)^$%*#RF{F");
            Util.checkFile(PREF_FILE, 123, 1234L, "my custom string field @!!##^%#^463*)^$%*#RF{F");

            System.out.println(preferences);
        }


        // Если сюда дошли - значит всё работает хорошо
    }


    @EqualsAndHashCode(callSuper = false)
    @ToString
    @Getter
    @Setter
    private static class SimpleExamplePref implements UserPreferences {

        private int myIntegerField = 123;
        private long myLongField = 1234L;
        private String myStringField = "my custom string field @!!##^%#^463*)^$%*#RF{F";


        @Override
        public File userPreferencesFile() {
            return PREF_FILE;
        }
    }

    private static class Util {
        private static void checkInstance(final SimpleExamplePref simpleExamplePref,
                                          final int integerField,
                                          final long longField,
                                          final String stringField) throws Exception {

            if (simpleExamplePref.getMyIntegerField() != integerField ||
                    simpleExamplePref.getMyLongField() != longField ||
                    !simpleExamplePref.getMyStringField().equals(stringField)) {

                throw new Exception("Bad data");
            }
        }

        private static void checkFile(final File file,
                                      final int integerField,
                                      final long longField,
                                      final String stringField) throws Exception {

            final SimpleExamplePref simpleExamplePref = config.getGson()
                    .fromJson(Files.lines(file.toPath())
                            .collect(Collectors.joining(System.lineSeparator())), SimpleExamplePref.class);

            if (simpleExamplePref.getMyIntegerField() != integerField ||
                    simpleExamplePref.getMyLongField() != longField ||
                    !simpleExamplePref.getMyStringField().equals(stringField)) {

                throw new Exception("Bad data");
            }
        }
    }
}
