package com.vinichenkosa.tenderrobot.model.utender;

import javax.xml.bind.annotation.XmlElement;

public class UtenderDateTime {
    
    @XmlElement(name = "Date")
    private String date;
    @XmlElement(name = "Time")
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
