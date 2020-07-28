package ch.frattino.covid.pc;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PoliticalCovid {

    private Config config;

    private List<String> rawData = new ArrayList<>();
    private List<String> states = new ArrayList<>();
    // date -> (state -> cases)
    private Map<Date, Map<String, Integer>> parsedData = new HashMap<>();
    private List<String> graphData = new ArrayList<>();

    public PoliticalCovid(Config config) {
        this.config = config;
    }

    public void run() {
        // We grab the raw data
        try {
            grabRawData();
        } catch (IOException e) {
            System.err.println("Impossible to grab raw data!");
            e.printStackTrace();
            return;
        }

        // We parse the data
        parseData();

        // We compute the graph data
        computeGraphData();

        // We print the graph data
        try {
            printGraphData();
        } catch (IOException e) {
            System.err.println("Impossible to print graph data!");
            e.printStackTrace();
            return;
        }
    }

    public void grabRawData() throws IOException {
        System.out.println("Obtaining raw data from " + config.getCsvSource() + "...");
        URL url = new URL(config.getCsvSource());
        try (BufferedReader fr = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = fr.readLine()) != null) {
                rawData.add(line);
            }
        }
        // We drop the first line
        rawData.remove(0);
        System.out.println("Raw data obtained! (" + rawData.size() + " lines)");
    }

    public void parseData() {
        System.out.println("Parsing data...");
        for (String line : rawData) {
            Date date;
            String state;
            Integer cases;
            try {
                // 2020-01-21,Washington,53,1,0
                String[] tokens = line.split(",");
                date = new SimpleDateFormat("yyyy-MM-dd").parse(tokens[0]);
                state = tokens[1];
                cases = Integer.parseInt(tokens[3]);
            } catch (ParseException e) {
                System.err.println("Skipped invalid line: " + line);
                e.printStackTrace();
                continue;
            }
            // We update the "states" list
            if (!states.contains(state)) {
                states.add(state);
            }
            // We update the "data" list
            if (!parsedData.containsKey(date)) {
                parsedData.put(date, new HashMap<String, Integer>());
            }
            parsedData.get(date).put(state, cases);
        }
        // We sort the states alphabetically
        Collections.sort(states);
        System.out.println("Data parsed! (" + parsedData.size() + " days)");
    }

    public void computeGraphData() {
        System.out.println("Computing graph data...");
        // We compute the header
        graphData.add("date," + states.stream().collect(Collectors.joining(",")));

        // We compute the data
        List<Date> dates = new ArrayList<>(parsedData.keySet());
        Collections.sort(dates);
        for (Date date : dates) {
            String line = new SimpleDateFormat("dd.MM.yyyy").format(date);
            for (String state : states) {
                Map<String, Integer> subData = parsedData.get(date);
                if (subData.containsKey(state)) {
                    line += "," + subData.get(state);
                } else {
                    line += ",0";
                }
            }
            graphData.add(line);
        }
        System.out.println("Graph data computed!");
    }

    public void printGraphData() throws IOException {
        File output = new File(config.getOutput());
        System.out.println("Printing graph data to " + output.getAbsolutePath() + "...");
        // We print it all!
        try (FileWriter fw = new FileWriter(output, false);
        BufferedWriter bw = new BufferedWriter(fw)) {
            for (String line : graphData) {
                bw.write(line);
                bw.newLine();
            }
        }
        System.out.println("Graph data written!");
    }

    public static void main(String[] args) throws IOException {
        Properties p = new Properties();
        p.load(PoliticalCovid.class.getResourceAsStream("/application.properties"));
        Config config = new Config(p);
        new PoliticalCovid(config).run();
    }
}
