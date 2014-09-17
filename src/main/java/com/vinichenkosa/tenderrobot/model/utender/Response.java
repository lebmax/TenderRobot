package com.vinichenkosa.tenderrobot.model.utender;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Response {
    @XmlElement(name = "d")
    private UtenderDateTime d;

    public UtenderDateTime getD() {
        return d;
    }

    public void setD(UtenderDateTime d) {
        this.d = d;
    }
    
    
    
}
