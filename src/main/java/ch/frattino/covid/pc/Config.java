package ch.frattino.covid.pc;

import java.util.Properties;

public class Config {

    private String csvSource;
    private String output;

    public Config(Properties p) {
        this.csvSource = p.getProperty("csvSource");
        this.output = p.getProperty("output");
    }

    public String getCsvSource() {
        return csvSource;
    }

    public String getOutput() {
        return output;
    }

}
