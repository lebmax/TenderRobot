package com.vinichenkosa.tenderrobot.timers;

import java.util.Date;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

@Stateless
public class Timer {

    @Schedule(hour = "*", minute = "*", second = "*")
    public void myTimer() {
        System.out.println("Timer event: " + new Date());
        logger.info("Test");
    }

    private static final Logger logger = Logger.getLogger(Timer.class.getName());
}
