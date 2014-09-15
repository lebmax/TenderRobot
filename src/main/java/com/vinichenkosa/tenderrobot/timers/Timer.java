package com.vinichenkosa.tenderrobot.timers;

import com.vinichenkosa.tenderrobot.model.Task;
import com.vinichenkosa.tenderrobot.service.TaskFacadeREST;
import java.io.IOException;
import java.util.List;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class Timer {

    @Inject
    private TaskFacadeREST taskFacade;
    
    @Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
    public void myTimer() throws IOException {
        logger.info("timer started");
        List<Task> activeTasks = taskFacade.findByStatusCode();
        logger.info("{} acyive tasks founded.", activeTasks.size());
        
    }

    private static final Logger logger = LoggerFactory.getLogger(Timer.class.getName());
}
