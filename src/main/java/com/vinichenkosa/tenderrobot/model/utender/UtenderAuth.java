/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vinichenkosa.tenderrobot.model.utender;

import com.vinichenkosa.tenderrobot.model.Task;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
public class UtenderAuth {

    private final BasicCookieStore cookieStore = new BasicCookieStore();
    private final Logger logger = LoggerFactory.getLogger(UtenderAuth.class.getName());
    //private Map<String, String> cookies = new HashMap<>();

    @Asynchronous
    @Lock(LockType.WRITE)
    public Future<BasicCookieStore> getCookies() throws Exception {

        logger.debug("Getting cookies");
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();) {

            logger.debug("Loading main page for authorization");
            Map<String, String> params = loadMain(httpclient);

            if (params.get("ctl00$ctl00$LeftContentLogin$ctl00$Login1$UserName") == null) {
                logger.debug("Куки уже установлены.");
            }

            logger.debug("Authorization...");
            auth(params, httpclient);

        }
        return new AsyncResult<>(cookieStore);
    }

    private Map<String, String> loadMain(CloseableHttpClient httpclient) throws Exception {

        String url = "http://utender.ru/";

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        HttpGet httpget = new HttpGet(url);

        try (CloseableHttpResponse response = httpclient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {
                //Path respFile = saveResponse(IOUtils.toByteArray(content));
                //try (InputStream is = Files.newInputStream(respFile);) {
                Document doc = Jsoup.parse(content, "utf-8", url);
                Elements inputs = doc.select("input");
                for (Element input : inputs) {
                    switch (input.attr("value")) {
                        case "Очистить":
                            break;
                        case "Искать":
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

    private void auth(Map<String, String> params, CloseableHttpClient httpclient) throws Exception {

        String url = "http://utender.ru/";
        params.put("ctl00$ctl00$LeftContentLogin$ctl00$Login1$UserName", "chudilos");
        params.put("ctl00$ctl00$LeftContentLogin$ctl00$Login1$Password", "123123test+");
        params.put("__EVENTTARGET", "");
        params.put("__EVENTARGUMENT", "");
        params.put("hiddenInputToUpdateATBuffer_CommonToolkitScripts", "1");
        params.put("ctl00$ctl00$MainExpandableArea$phExpandCollapse$scPurchaseAllSearch$Purchase_bargainTypeID_Типторгов$ddlBargainType", "10,11,12,111,13");

        HttpPost request = new HttpPost(url);
        UtenderHttpCommon.addPostHeaders(request);
        UrlEncodedFormEntity form = UtenderHttpCommon.addFormParams(params);
        request.setEntity(form);

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        try (CloseableHttpResponse response = httpclient.execute(request, context)) {

            HttpEntity entity = response.getEntity();
            Document doc = Jsoup.parse(entity.getContent(), "utf-8", url);
            UtenderHttpCommon.saveResponse(doc, "sendAuthResponse.html");
            EntityUtils.consume(entity);

        }

    }

    private boolean isAuthorized(Date date, CloseableHttpClient client) throws Exception {
        if (cookieStore.getCookies().isEmpty() || cookieStore.clearExpired(date)) {
            logger.debug("First authorization");
            return false;
        }
        Map<String, String> params = this.loadMain(client);
        return params.get("ctl00$ctl00$LeftContentLogin$ctl00$Login1$UserName") == null;

    }
}
