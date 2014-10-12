package com.vinichenkosa.tenderrobot.model.itender;

import com.vinichenkosa.tenderrobot.model.Task;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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
public class ITenderAuth {

    private final ConcurrentHashMap<Task, BasicCookieStore> cookiesMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ITenderAuth.class.getName());
    //private Map<String, String> cookies = new HashMap<>();

    @Asynchronous
    public Future<BasicCookieStore> getCookies(Task t) throws Exception {

        logger.debug("Getting cookies");
        cookiesMap.putIfAbsent(t, new BasicCookieStore());
        BasicCookieStore cookieStore = cookiesMap.get(t);
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();) {

            logger.debug("Loading main page for authorization");
            Map<String, String> params = loadMain(httpclient, t);

            if (params.get("ctl00$ctl00$LeftContentLogin$ctl00$Login1$UserName") == null) {
                logger.debug("Куки уже установлены.");
            }

            logger.debug("Authorization...");
            auth(params, httpclient, t);

        }
        return new AsyncResult<>(cookieStore);
    }

    private Map<String, String> loadMain(CloseableHttpClient httpclient, Task t) 
            throws Exception {

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookiesMap.get(t));
        HttpGet httpget = new HttpGet(t.getUrl());

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
        params.put("hiddenInputToUpdateATBuffer_CommonToolkitScripts", "1");
        params.put("ctl00$ctl00$MainExpandableArea$phExpandCollapse$scPurchaseAllSearch$Purchase_bargainTypeID_Типторгов$ddlBargainType", "10,11,12,111,13");

        HttpPost request = new HttpPost(t.getAuctionType().getUrl());
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
