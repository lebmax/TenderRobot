package com.vinichenkosa.tenderrobot.timers;

import com.vinichenkosa.tenderrobot.model.Task;
import com.vinichenkosa.tenderrobot.model.utender.UtenderAuth;
import com.vinichenkosa.tenderrobot.model.utender.UtenderTask;
import com.vinichenkosa.tenderrobot.service.TaskFacadeREST;
import com.vinichenkosa.tenderrobot.service.TaskStatusFacadeREST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Timer {

    @Inject
    private TaskFacadeREST taskFacade;
    @Inject
    private TaskStatusFacadeREST taskStatusFacade;

    @Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
    public void myTimer() throws IOException, Exception {
        logger.info("timer started");
        List<Task> tasksToExecute = taskFacade.findByStatusCode(1);
        logger.info("{} active tasks founded.", tasksToExecute.size());
        DateTime now = new DateTime();
        UtenderAuth uAuth = null;
        for (Task task : tasksToExecute) {
            DateTime beginDate = new DateTime(task.getBeginDate());
            Period dayDiff = new Period(now, beginDate, PeriodType.days());
            logger.info("Period: {}", dayDiff.getDays());

            if (dayDiff.getDays() > 0) {
                break;
            }

            LocalTime time = UtenderTask.getTime();
            Period minDiff = new Period(time.toDateTimeToday(), beginDate, PeriodType.seconds());
            logger.info("Diff in seconds: {}", minDiff.getSeconds());
            if (minDiff.getSeconds() > 0) {
                break;
            }
            task.setStatus(taskStatusFacade.findByCode(2));
            task.setStartTime(new Date());
            taskFacade.edit(task.getId(), task);
            if (uAuth == null) {
                uAuth = new UtenderAuth();
            }

            UtenderTask utask = new UtenderTask(uAuth);
            try {
                utask.execute(task.getUrl());
                task.setStatus(taskStatusFacade.findByCode(3));
            } catch (Exception ex) {
                task.setStatus(taskStatusFacade.findByCode(4));
            } finally {
                task.setEndTime(new Date());
                taskFacade.edit(task.getId(), task);
            }

            //utask.setStartTime(new DateTime().plus(minDiff));
            //activeTasks.add(utask);
        }

    }

    @Schedule(hour = "*", minute = "*", second = "*", persistent = false)
    public void execute() {
        if (activeTasks.isEmpty()) {
            List<Task> findByStatusCode = taskFacade.findByStatusCode(2);
        }
        for (UtenderTask activeTask : activeTasks) {
            //logger.info("begin date: {}", activeTask.getStartTime());
        }
    }

    private List<UtenderTask> activeTasks = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Timer.class.getName());
}
