package com.vinichenkosa.tenderrobot.timers;

import com.vinichenkosa.tenderrobot.logic.utender.UtenderLogic;
import com.vinichenkosa.tenderrobot.model.Task;
import com.vinichenkosa.tenderrobot.model.utender.UtenderAuth;
import com.vinichenkosa.tenderrobot.model.utender.UtenderHttpCommon;
import com.vinichenkosa.tenderrobot.model.utender.UtenderTask;
import com.vinichenkosa.tenderrobot.service.TaskFacadeREST;
import com.vinichenkosa.tenderrobot.service.TaskStatusFacadeREST;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import org.apache.http.impl.client.BasicCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Timer {

    @Inject
    private TaskFacadeREST taskFacade;
    @Inject
    private TaskStatusFacadeREST taskStatusFacade;
    @Inject
    private UtenderAuth authService;
    @Inject
    private UtenderLogic utenderLogic;
    private final ConcurrentHashMap<Task, ScheduledFuture<Task>> futures = new ConcurrentHashMap<>();
    //private ConcurrentLinkedQueue<UtenderTask> tasksToPrepare = new ConcurrentLinkedQueue<>();

    @Resource(name = "concurrent/__defaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService executor;

    @Schedule(hour = "*", minute = "*", second = "*", persistent = false)
    //@Asynchronous
    public void myTimer() {
        try {

            List<Task> tasksToExecute = taskFacade.findByStatusCode(1);

            for (Task task : tasksToExecute) {
                try {

                    long diff = task.getBeginDate().getTime() - UtenderHttpCommon.getTime().getMillis();

                    if (diff > 300000) {
                        //logger.debug("No tasks to execute");
                        break;
                    }
                    logger.debug("Mill to execute: {}", diff);

                    Future<BasicCookieStore> cookiesFutureCont = null;
                    UtenderTask utask = new UtenderTask(task);

                    if (diff < 0) {
                        cookiesFutureCont = authService.getCookies(new Date());
                    } else {
                        cookiesFutureCont = authService.getCookies(task.getBeginDate());
                    }
                    utask.setCookiesFutureCont(cookiesFutureCont);
                    utask.setRequestFutureCont(utenderLogic.prepare(cookiesFutureCont, task));

                    ScheduledFuture<Task> f = executor.schedule(utask, diff, TimeUnit.MILLISECONDS);
                    logger.debug("Task {} scheduled", task);
                    futures.put(task, f);
                    logger.debug("Task {} added to future", task);

                    task.setStatus(taskStatusFacade.findByCode(2));
                    taskFacade.edit(task.getId(), task);
                    logger.debug("Tasks status changed to 2");

                } catch (Exception ex) {
                    logger.error("Ощибка добавления задачи {} в очередь. ", task.getId(), ex);
                    task.setStatus(taskStatusFacade.findByCode(4));
                    taskFacade.edit(task.getId(), task);
                }

            }
        } catch (Exception ex) {
            logger.error("Ощибка добавления задач в очередь. ", ex);
        }

    }

    @Schedule(hour = "*", minute = "*", second = "*/10", persistent = false)
    //@Asynchronous
    private void checkTasks() throws InterruptedException {

        Enumeration<Task> tasks = futures.keys();

        while (tasks.hasMoreElements()) {

            Task task = tasks.nextElement();
            logger.debug("Check task {}", task);
            ScheduledFuture<Task> job = futures.get(task);

            if (job.isDone()) {
                

                if (job.isCancelled()) {
                    task.setStatus(taskStatusFacade.findByCode(4));
                    logger.error("Task was cancelled ");
                } else {
                    try {
                        job.get();
                        task.setStatus(taskStatusFacade.findByCode(3));
                        logger.debug("Task successfully done.");
                    } catch (ExecutionException ex) {
                        task.setStatus(taskStatusFacade.findByCode(4));
                        logger.error("Ошибка выполнения задачи:", ex);
                    }
                }
                taskFacade.edit(task.getId(), task);
                futures.remove(task);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Timer.class.getName());
}
