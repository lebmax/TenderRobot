package com.impulsm.tenderrobot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpURLConnectionExample {

    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpclient;
    //private Map<String, String> cookies = new HashMap<>();

    public HttpURLConnectionExample() {
        cookieStore = new BasicCookieStore();
        httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    }

    public static void main(String[] args) throws Exception {

        HttpURLConnectionExample http = new HttpURLConnectionExample();

        try {
            Map<String, String> params = http.loadMain();
            http.auth(params);
            http.loadRequest();
        } finally {
            http.httpclient.close();
        }

    }

    private Map<String, String> loadMain() throws Exception {

        String url = "http://utender.ru/";

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        HttpGet httpget = new HttpGet(url);

        try (CloseableHttpResponse response = httpclient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {
                Path respFile = saveResponse(IOUtils.toByteArray(content));
                try (InputStream is = Files.newInputStream(respFile);) {
                    Document doc = Jsoup.parse(is, "utf-8", url);
                    Elements inputs = doc.select("input");
                    for (Element input : inputs) {
                        switch (input.attr("value")) {
                            case "Очистить":
                                break;
                            case "Искать":
                                break;
                            default:
                                params.put(input.attr("name"), input.attr("value"));
                                //logger.debug("{}={}", input.attr("name"), input.attr("value"));
                                break;
                        }
                    }

                    Elements selects = doc.select("select");
                    //logger.debug("Found {} selects.", selects.size());
                    for (Element select : selects) {
                        params.put(select.attr("name"), "");
                        //logger.debug("{}={}", select.attr("name"), "");
                    }
                }
            }
            EntityUtils.consume(entity);
            printCookies();
            return params;
        }
    }

    private void auth(Map<String, String> params) throws Exception {

        String url = "http://utender.ru/";
        params.put("ctl00$ctl00$LeftContentLogin$ctl00$Login1$UserName", "chudilos");
        params.put("ctl00$ctl00$LeftContentLogin$ctl00$Login1$Password", "123123test+");
        params.put("__EVENTTARGET", "");
        params.put("__EVENTARGUMENT", "");
        params.put("hiddenInputToUpdateATBuffer_CommonToolkitScripts", "1");
        params.put("ctl00$ctl00$MainExpandableArea$phExpandCollapse$scPurchaseAllSearch$Purchase_bargainTypeID_Типторгов$ddlBargainType", "10,11,12,111,13");

        List<NameValuePair> formparams = new ArrayList<>();
        for (String key : params.keySet()) {
            //logger.debug("{}={}", key, params.get(key));
            formparams.add(new BasicNameValuePair(key, params.get(key)));
        }

        UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost login = new HttpPost(url);

//        addCommonHeaders(login);
//        login.addHeader("Origin", "http://utender.ru");
//        login.addHeader("Content-Type", "application/x-www-form-urlencoded");
//        login.addHeader("Referer", "http://utender.ru/");

        login.setEntity(form);

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        try (CloseableHttpResponse response = httpclient.execute(login, context)) {

            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            printCookies();

        }

    }

    private void loadRequest() throws IOException{
        String url = "http://utender.ru/supplier/contests/lots/540137/request/32237/";

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        HttpGet httpget = new HttpGet(url);

        try (CloseableHttpResponse response = httpclient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {
                Path respFile = saveResponse(IOUtils.toByteArray(content));
                try (InputStream is = Files.newInputStream(respFile);) {
                    Document doc = Jsoup.parse(is, "utf-8", url);
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
                    logger.debug("Found {} selects.", selects.size());
                    for (Element select : selects) {
                        params.put(select.attr("name"), "");
                        logger.debug("{}={}", select.attr("name"), "");
                    }
                }
            }
            EntityUtils.consume(entity);
            printCookies();
            //return params;
        }
    }
    
    
    
    private void addCommonHeaders(HttpPost method) {
        method.addHeader("Host", "utender.ru");
        method.addHeader("Connection", "keep-alive");
        method.addHeader("Cache-Control", "max-age=0");
        method.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        method.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36");
        method.addHeader("Accept-Encoding", "gzip,deflate");
        method.addHeader("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        //addCookieHeader(method);
    }


    private void printResponseHeaders(HttpResponse resp) {
        Header[] headers = resp.getAllHeaders();

        for (Header header : headers) {
            logger.debug("Header: {}", header.toString());
        }
    }


    private Path saveResponse(byte[] response) throws IOException {
        Path path = Paths.get("/home/vinichenkosa/Desktop/response.html");
        return Files.write(path, response);
    }

    private void printCookies() {
        List<Cookie> cookies = cookieStore.getCookies();

        if (cookies.isEmpty()) {
            logger.debug("No cookies");
        } else {
            for (Cookie cookie : cookies) {
                logger.debug("Cookie: {}", cookie);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpURLConnectionExample.class.getName());

}
