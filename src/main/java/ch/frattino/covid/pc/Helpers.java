package ch.frattino.covid.pc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Helpers {

    public static URL toUrl(String string) {
        try {
            return new URL(string);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> readTextFileFromUrl(URL url, boolean dropFirstLine) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader fr = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = fr.readLine()) != null) {
                lines.add(line);
            }
            if (dropFirstLine) {
                lines.remove(0);
            }
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
