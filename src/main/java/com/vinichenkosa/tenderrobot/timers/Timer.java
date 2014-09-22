package com.vinichenkosa.tenderrobot.timers;

import com.vinichenkosa.tenderrobot.model.Task;
import com.vinichenkosa.tenderrobot.model.utender.UtenderAuth;
import com.vinichenkosa.tenderrobot.model.utender.UtenderHttpCommon;
import com.vinichenkosa.tenderrobot.model.utender.UtenderTask;
import com.vinichenkosa.tenderrobot.service.TaskFacadeREST;
import com.vinichenkosa.tenderrobot.service.TaskStatusFacadeREST;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Timer {

    @Inject
    private TaskFacadeREST taskFacade;
    @Inject
    private TaskStatusFacadeREST taskStatusFacade;
    private final ConcurrentHashMap<Task, ScheduledFuture<Task>> futures = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<UtenderTask> tasksToPrepare = new ConcurrentLinkedQueue<>();

    @Resource(name = "concurrent/__defaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService executor;

    @Schedule(hour = "*", minute = "*", second = "*", persistent = false)
    @Asynchronous
    public void myTimer() {
        try {

            List<Task> tasksToExecute = taskFacade.findByStatusCode(1);
            
            for (Task task : tasksToExecute) {
                try {

                    long diff = task.getBeginDate().getTime() - UtenderHttpCommon.getTime().getMillis();
                    

                    if (diff > 300000) {
                        logger.debug("No tasks to execute");
                        break;
                    }
                    
                    logger.debug("Mill to execute: {}", diff);

                    logger.debug("Add task {} to active", task);
                    UtenderTask utask = new UtenderTask(task);
                    ScheduledFuture<Task> f = executor.schedule(utask, diff, TimeUnit.MILLISECONDS);
                    logger.debug("Task {} scheduled", task);
                    futures.put(task, f);
                    logger.debug("Task {} added to future", task);
                    tasksToPrepare.add(utask);
                    logger.debug("Task {} added to prepared", task);
                    
                    

                    task.setStatus(taskStatusFacade.findByCode(2));
                    taskFacade.edit(task.getId(), task);
                    logger.debug("Tasks status chandef to 2");
                    

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

    @Schedule(hour = "*", minute = "*", second = "*", persistent = false)
    @Asynchronous
    private void prepareTasks() {
        
        for (UtenderTask task = tasksToPrepare.poll(); task != null; task = null) {
            try {
                task.prepare(UtenderAuth.getCookies(task.getTask().getBeginDate()));
                logger.debug("Task prepared");
            } catch (Exception ex) {
                logger.error("Ошибка подготовки задач", ex);
                ScheduledFuture<Task> t = futures.get(task.getTask());
                if(t != null){
                    t.cancel(true);
                }else{
                    logger.debug("Не смог отменить задачу {}", task);
                }
                
            }
        }
    }

    @Schedule(hour = "*", minute = "*", second = "0", persistent = false)
    @Asynchronous
    private void checkTasks() {
        try {

            Enumeration<Task> keys = futures.keys();
            

            while (keys.hasMoreElements()) {

                Task key = keys.nextElement();
                logger.debug("Check task {}", key);
                ScheduledFuture<Task> task = futures.get(key);

                if (task.isDone()) {

                    try {
                        key.setStatus(taskStatusFacade.findByCode(3));
                        logger.debug("Task successfully done.");
                    } catch (Exception ex) {
                        key.setStatus(taskStatusFacade.findByCode(4));
                        logger.error("Task execution error: ", ex);
                    } finally {
                        taskFacade.edit(key.getId(), key);
                        futures.remove(key);
                    }

                } else if (task.isCancelled()) {
                    key.setStatus(taskStatusFacade.findByCode(4));
                    logger.error("Task was cancelled ");
                    taskFacade.edit(key.getId(), key);
                    futures.remove(key);
                }
            }
        } catch (Exception ex) {
            logger.error("Ошибка при проверке результатов задач. ", ex);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Timer.class.getName());
}
