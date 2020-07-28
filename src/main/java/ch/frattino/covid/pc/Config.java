package ch.frattino.covid.pc;

import java.util.Properties;

public class Config {

    private String covidInput;
    private String populationInput;
    private String output;

    public Config(Properties p) {
        this.covidInput = p.getProperty("covidInput");
        this.populationInput = p.getProperty("populationInput");
        this.output = p.getProperty("output");
    }

    public String getCovidInput() {
        return covidInput;
    }

    public String getPopulationInput() {
        return populationInput;
    }

    public String getOutput() {
        return output;
    }

}
