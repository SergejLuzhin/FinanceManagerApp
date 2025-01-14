import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;

public class FileManager {
    private static final Gson gson = new Gson();

    public static <T> void saveToFile(String fileName, T data) {
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных в файл: " + fileName);
        }
    }

    public static <T> T loadFromFile(String fileName, Type typeOfT) {
        try (Reader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, typeOfT);
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + fileName + ". Будет создан новый.");
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + fileName);
        }
        return null;
    }
}
