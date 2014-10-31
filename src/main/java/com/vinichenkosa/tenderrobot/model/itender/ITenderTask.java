package com.vinichenkosa.tenderrobot.model.itender;

import com.vinichenkosa.tenderrobot.logic.itender.ITenderLogic;
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
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ITenderTask implements Callable<Task> {

    private final Task task;
    private final HttpClientContext context = HttpClientContext.create();
    private CloseableHttpClient httpclient;
    private Future<BasicCookieStore> cookiesFutureCont;
    private Future<HttpPost> requestFutureCont;

    public ITenderTask(Task task) throws Exception {
        this.task = task;
    }

    private static final Logger logger = LoggerFactory.getLogger(ITenderTask.class.getName());

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
                ITenderLogic requestFactory = lookupUtenderLogicBean();
                requestFutureCont = requestFactory.prepare(cookiesFutureCont, task);
                while (!requestFutureCont.isDone()) {
                }
                request = requestFutureCont.get();
            }

            if (request == null) {
                throw new Exception("Не удалось сформировать запрос по адресу " + task.getUrl());
            }
            logger.debug("Request is prepared.");

            return sendRequest(request);

        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.error("Can't close httpClient.", ex);
            }
        }

    }

    private Task sendRequest(HttpPost request) throws IOException, Exception {

        try {
            DateTime endTime = new DateTime().plusMinutes(1);
            Elements errorsCont = null;
            Document doc = null;

            while (new DateTime().isBefore(endTime)) {
                try (CloseableHttpResponse response = httpclient.execute(request, context);) {

                    HttpEntity entity = response.getEntity();
                    doc = Jsoup.parse(entity.getContent(), "utf-8", task.getUrl());
                    errorsCont = doc.getElementsByAttributeValue("id", "ctl00_ctl00_MainContent_ContentPlaceHolderMiddle_ctl00_BidsValidationSummary");

                    if (errorsCont.isEmpty()) {
                        logger.debug("Task finished");
                        task.setEndTime(new Date());
                        return task;
                    }
                    EntityUtils.consume(entity);
                }
            }

            if (errorsCont == null || doc == null) {
                throw new Exception("Не было отправлено ни одного запроса, либо не было получено ни одного ответа.");
            }

            ITenderHttpCommon.saveResponse(doc, "sendRequestResponse.html");
            StringBuilder sb = new StringBuilder("Запрос не прошел валидацию на сервере.\n");
            Elements errorList = errorsCont.get(0).getElementsByTag("li");
            sb.append("Ответ от сервера содержит ").append(errorList.size()).append(" ошибок.\n");
            for (Element error : errorList) {
                sb.append("Ошибка 1: ").append(error.text()).append("\n");
            }

            throw new Exception(sb.toString());
        } finally {
            task.setEndTime(new Date());
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

    private ITenderLogic lookupUtenderLogicBean() {
        try {
            Context c = new InitialContext();
            return (ITenderLogic) c.lookup("java:global/TenderRobot/ITenderLogic");
        } catch (NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

}
