package com.vinichenkosa.tenderrobot.model.arbitat;

import com.vinichenkosa.tenderrobot.model.itender.*;
import com.vinichenkosa.tenderrobot.model.Task;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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

@Singleton
@Lock(LockType.READ)
public class ArbitatAuth {

    private final ConcurrentHashMap<Task, BasicCookieStore> cookiesMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ArbitatAuth.class.getName());
    //private Map<String, String> cookies = new HashMap<>();

    @Asynchronous
    public Future<BasicCookieStore> getCookies(Task t) throws Exception {

        logger.debug("Getting cookies");
        cookiesMap.putIfAbsent(t, new BasicCookieStore());
        BasicCookieStore cookieStore = cookiesMap.get(t);
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();) {

            logger.debug("Loading main page for authorization");
            Map<String, String> params = loadAuth(httpclient, t);

            if (params.get("ctl00$ctl00$LeftContentLogin$ctl00$Login1$UserName") == null) {
                logger.debug("Куки уже установлены.");
            }

            logger.debug("Authorization...");
            auth(params, httpclient, t);

        }
        return new AsyncResult<>(cookieStore);
    }

    private Map<String, String> loadAuth(CloseableHttpClient httpclient, Task t) 
            throws Exception {

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookiesMap.get(t));
        HttpGet httpget = new HttpGet(t.getAuctionType().getUrl()+"/public/register-party/");

        try (CloseableHttpResponse response = httpclient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {
                //Path respFile = saveResponse(IOUtils.toByteArray(content));
                //try (InputStream is = Files.newInputStream(respFile);) {
                Document doc = Jsoup.parse(content, "utf-8", t.getUrl());
                Elements inputs = doc.select("input");
                for (Element input : inputs) {
                    switch (input.attr("value")) {
                        case "Очистить":
                            break;
                        case "Искать":
                            break;
                        case "Запустить проверку":
                            break;
                        case "Инструкция по настройке браузера":
                            break;
                        case "":
                            break;
                        default:
                            params.put(input.attr("name"), input.attr("value"));
                            logger.debug("{}={}", input.attr("name"), input.attr("value"));
                            break;
                    }
                }

                Elements selects = doc.select("select");
                //logger.debug("Found {} selects.", selects.size());
                for (Element select : selects) {
                    params.put(select.attr("name"), "");
                    //logger.debug("{}={}", select.attr("name"), "");
                }
                //}
            }
            EntityUtils.consume(entity);
            //printCookies();
            return params;
        }
    }

    private void auth(
            Map<String, String> params, CloseableHttpClient httpclient, Task t
    ) throws Exception {

        params.put("ctl00$ctl00$LeftContentLogin$ctl00$Login1$UserName", t.getAuctionType().getLogin());
        params.put("ctl00$ctl00$LeftContentLogin$ctl00$Login1$Password", t.getAuctionType().getPassword());
        params.put("__EVENTTARGET", "");
        params.put("__EVENTARGUMENT", "");
        
        HttpPost request = new HttpPost(t.getAuctionType().getUrl()+"/public/register-party/");
        
        logger.debug("Params to send request fot auth:");
        
        Set<String> keys = params.keySet();
        for (String key : keys) {
            logger.debug("Param {} = {}", key, params.get(key));
        }
        
        ITenderHttpCommon.addPostHeaders(request);
        UrlEncodedFormEntity form = ITenderHttpCommon.addFormParams(params);
        request.setEntity(form);

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookiesMap.get(t));

        try (CloseableHttpResponse response = httpclient.execute(request, context)) {

            HttpEntity entity = response.getEntity();
            Document doc = Jsoup.parse(entity.getContent(), "utf-8", t.getUrl());
            ITenderHttpCommon.saveResponse(doc, "sendAuthResponse.html");
            EntityUtils.consume(entity);

        }

    }
}
