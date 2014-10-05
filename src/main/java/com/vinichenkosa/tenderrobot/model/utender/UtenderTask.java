package com.vinichenkosa.tenderrobot.model.utender;

import com.vinichenkosa.tenderrobot.logic.utender.UtenderLogic;
import com.vinichenkosa.tenderrobot.model.Task;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtenderTask implements Callable<Task> {

    private final Task task;
    private final HttpClientContext context = HttpClientContext.create();
    private CloseableHttpClient httpclient;
    private Future<BasicCookieStore> cookiesFutureCont;
    private Future<HttpPost> requestFutureCont;

    public UtenderTask(Task task) throws Exception {
        this.task = task;
    }

    private static final Logger logger = LoggerFactory.getLogger(UtenderTask.class.getName());

    @Override
    public Task call() throws Exception {

        try {
            logger.debug("task called");
            task.setStartTime(new Date());
            while (!cookiesFutureCont.isDone()) {
            }
            BasicCookieStore cookies = cookiesFutureCont.get();
            context.setCookieStore(cookies);
            this.httpclient = HttpClients.custom().setDefaultCookieStore(cookies).build();
            logger.debug("Authorization done.");
            
            while (!requestFutureCont.isDone()) {
            }
            HttpPost request = requestFutureCont.get();
            if (request == null) {
                logger.debug("Request not formed. Trying again...");
                UtenderLogic requestFactory = lookupUtenderLogicBean();
                requestFutureCont = requestFactory.prepare(cookiesFutureCont, task);
                while (!requestFutureCont.isDone()) {
                }
                request = requestFutureCont.get();
            }

            if (request == null) {
                throw new Exception("Не удалось сформировать запрос по адресу " + task.getUrl());
            }
            logger.debug("Request is prepared.");

            try (CloseableHttpResponse response = httpclient.execute(request, context);) {
                task.setEndTime(new Date());
                logger.debug("Task finished");
                HttpEntity entity = response.getEntity();
                Document doc = Jsoup.parse(entity.getContent(), "utf-8", task.getUrl());
                Elements errorsCont = doc.getElementsByAttributeValue("id", "ctl00_ctl00_MainContent_ContentPlaceHolderMiddle_ctl00_BidsValidationSummary");
                
                UtenderHttpCommon.saveResponse(doc, "sendRequestResponse.html");
                
                if(!errorsCont.isEmpty()){
                    StringBuilder sb = new StringBuilder("Запрос не прошел валидацию на сервере.\n");
                    Elements errorList = errorsCont.get(0).getElementsByTag("li");
                    sb.append("Ответ от сервера содержит ").append(errorList.size()).append(" ошибок.\n");
                    for (Element error : errorList) {
                        sb.append("Ошибка 1: ").append(error.text()).append("\n");
                    }
                    
                    throw new Exception(sb.toString());
                }
                EntityUtils.consume(entity);
                return task;
            }

        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.error("Can't close httpClient.", ex);
            }
        }
        
    }

    public Task getTask() {
        return task;
    }

    public Future<BasicCookieStore> getCookiesFutureCont() {
        return cookiesFutureCont;
    }

    public void setCookiesFutureCont(Future<BasicCookieStore> cookiesFutureCont) {
        this.cookiesFutureCont = cookiesFutureCont;
    }

    public Future<HttpPost> getRequestFutureCont() {
        return requestFutureCont;
    }

    public void setRequestFutureCont(Future<HttpPost> requestFutureCont) {
        this.requestFutureCont = requestFutureCont;
    }

    private UtenderLogic lookupUtenderLogicBean() {
        try {
            Context c = new InitialContext();
            return (UtenderLogic) c.lookup("java:global/TenderRobot/UtenderLogic");
        } catch (NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

}
