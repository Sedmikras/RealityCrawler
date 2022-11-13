package cz.koup.vs;

public class Parameter {
    String parameterName;
    String parameterValue;

    public Parameter(String name, String value) {
        this.parameterName = name;
        this.parameterValue = value;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
}
