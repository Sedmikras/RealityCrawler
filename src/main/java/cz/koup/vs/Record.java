package cz.koup.vs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: koup
 * Date: 13.03.2020
 */
public class Record implements Serializable {
    String title;
    String location;
    String URL;
    List<Parameter> parameters;

    public Record(String title, String location, String URL, List<String> parameterName, List<String> parameterValue) {
        this.title = title;
        this.location = location;
        this.URL = URL;
        parameters = new ArrayList<Parameter>(parameterName.size());
        for(int i = 0; i < parameterName.size(); i++) {
            parameters.add(new Parameter(parameterName.get(i), parameterValue.get(i)));
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
