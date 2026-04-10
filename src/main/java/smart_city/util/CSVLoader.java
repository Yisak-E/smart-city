package smart_city.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {

    public static List<String[]> load(String filePath) {
        List<String[]> events = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                events.add(line.split(","));
            }

        } catch (Exception e) {
            System.out.println("❌ CSV error: " + e.getMessage());
        }

        return events;
    }
}