package com.vinichenkosa.tenderrobot.timers;

import com.vinichenkosa.tenderrobot.model.utender.UtenderDateTime;
import java.io.IOException;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class Timer {

    @Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
    public void myTimer() throws IOException {
        logger.info("Test1");
        
    }

    private static final Logger logger = LoggerFactory.getLogger(Timer.class.getName());
}
