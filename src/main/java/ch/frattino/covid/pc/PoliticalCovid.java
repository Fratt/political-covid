package ch.frattino.covid.pc;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PoliticalCovid {

    private Config config;

    // Raw data
    private List<String> rawCovidData = new ArrayList<>();
    private List<String> rawPopulationData = new ArrayList<>();

    private List<String> states = new ArrayList<>();
    private Map<String, Integer> populations = new HashMap<>();
    // date -> (state -> cases)
    private Map<Date, Map<String, Double>> parsedData = new HashMap<>();
    private List<String> graphData = new ArrayList<>();

    public PoliticalCovid(Config config) {
        this.config = config;
    }

    public void run() {

        // We grab the raw population data
        if (!grabRawPopulationData()) {
            return;
        }
        // We parse the population data
        parsePopulationData();

        // We grab the raw COVID data
        if (!grabRawCovidData()) {
            return;
        }
        // We parse the COVID data
        parseCovidData();

        // We compute the graph data
        computeGraphData();

        // We print the graph data (number of cases)
        try {
            printGraphData();
        } catch (IOException e) {
            System.err.println("Impossible to print graph data!");
            e.printStackTrace();
            return;
        }

        // We print the graph data (number of cases / population)

    }

    public boolean grabRawCovidData() {
        URL url = Helpers.toUrl(config.getCovidInput());
        if (url == null) {
            return false;
        }
        System.out.println("Obtaining raw COVID data from " + url + "...");
        rawCovidData = Helpers.readTextFileFromUrl(url, true);
        if (rawCovidData == null) {
            return false;
        }
        System.out.println("Raw COVID data obtained! (" + rawCovidData.size() + " lines)");
        return true;
    }

    public void parseCovidData() {
        Set<String> skippedStates = new HashSet<>();
        System.out.println("Parsing COVID data...");
        for (String line : rawCovidData) {
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
            // If we don't have a population for this state, skip!
            if (!populations.containsKey(state)) {
                skippedStates.add(state);
                continue;
            }
            // We update the "states" list
            if (!states.contains(state)) {
                states.add(state);
            }
            // We update the "data" list
            if (!parsedData.containsKey(date)) {
                parsedData.put(date, new HashMap<String, Double>());
            }
            parsedData.get(date).put(state, (1000000 * cases.doubleValue()) / populations.get(state).doubleValue());
        }
        // We sort the states alphabetically
        Collections.sort(states);
        System.out.println("COVID data parsed! (" + parsedData.size() + " days)");
        if (skippedStates.size() > 0) {
            System.out.println("The following states have been skipped because no population data was found: " + skippedStates);
        }
    }

    public boolean grabRawPopulationData() {
        URL url = Helpers.toUrl(config.getPopulationInput());
        if (url == null) {
            return false;
        }
        System.out.println("Obtaining raw population data from " + url + "...");
        rawPopulationData = Helpers.readTextFileFromUrl(url, true);
        System.out.println("Raw population data obtained! (" + rawPopulationData.size() + " states)");
        if (rawPopulationData == null) {
            return false;
        }
        return true;
    }

    public void parsePopulationData() {
        System.out.println("Parsing population data...");
        for (String line : rawPopulationData) {
            String state;
            Integer population;
            // state,state_name,geo_id,population,pop_density
            // AL,Alabama,01,4887871,96.50938865
            String[] tokens = line.split(",");
            state = tokens[1];
            population = Integer.parseInt(tokens[3]);
            populations.put(state, population);
        }
        System.out.println("COVID data parsed! (" + populations.size() + " states)");

    }

    public void computeGraphData() {
        System.out.println("Computing graph data...");

        // We compute the header
        graphData.add("date," + states.stream().collect(Collectors.joining(",")));

        // We compute the data
        List<Date> dates = new ArrayList<>(parsedData.keySet());
        Collections.sort(dates);
        for (Date date : dates) {
            String line = new SimpleDateFormat("yyyy-MM-dd").format(date);
            for (String state : states) {
                Map<String, Double> subData = parsedData.get(date);
                if (subData.containsKey(state)) {
                    line += "," + String.format("%.2f", subData.get(state));
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
