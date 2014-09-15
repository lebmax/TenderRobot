package com.vinichenkosa.tenderrobot.model.utender;

import org.codehaus.jackson.annotate.JsonProperty;

public class UtenderDateTime {
    
    @JsonProperty(value = "Date")
    private String date;
    @JsonProperty(value =  "Time")
    private String time;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    
    
}
