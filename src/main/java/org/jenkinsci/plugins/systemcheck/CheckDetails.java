package org.jenkinsci.plugins.systemcheck;

public class CheckDetails {
    
    private String name;
    private String state;
    private String date;
    private String description;

    public CheckDetails(String name, String state, String date, String description) {
        this.name = name;
        this.state = state;
        this.date = date;
        this.description = description;
    }

    public CheckDetails getDetails(){
        return this;
    }
    public String getName() {
        return name;
    }
    public String getState() {
        return state;
    }
    public String getDate() {
        return date;
    }
    public String getDescription() {
        return description;
    }

}
