package com.vinichenkosa.tenderrobot.model.utender;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtenderTask {

    
    public LocalTime getTime() throws IOException{
        Client client = ClientBuilder.newClient();
        javax.ws.rs.core.Response response = client.target("http://utender.ru/public/services/datetime//GetDateTime")
                .request(MediaType.APPLICATION_JSON).header("Content-Type", "application/json").post(null);
        String d = response.readEntity(com.vinichenkosa.tenderrobot.model.utender.Response.class).getD();
        ObjectMapper mapper = new ObjectMapper();
        UtenderDateTime dateTime = mapper.readValue(d, UtenderDateTime.class);
        LocalTime localTime = new LocalTime(dateTime.getTime().substring(0, 8));
        logger.info("Server date: {}", localTime);
        return localTime;
    }
    
    private static final Logger logger = LoggerFactory.getLogger(UtenderTask.class.getName());
    
}
