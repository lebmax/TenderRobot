package com.impulsm.tenderrobot;

import com.impulsm.signatureutils.container.PKCS7Container;
import com.impulsm.signatureutils.exceptions.KeystoreInitializationException;
import com.impulsm.signatureutils.keystore.Keystore;
import com.impulsm.signatureutils.keystore.KeystoreTypes;
import com.impulsm.signatureutils.signature.GOSTSignature;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
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

    private final BasicCookieStore cookieStore;
    private final CloseableHttpClient httpclient;
    //private Map<String, String> cookies = new HashMap<>();

    public HttpURLConnectionExample() {
        cookieStore = new BasicCookieStore();
        httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    }

    public static void main(String[] args) throws Exception {

        String url = "http://utender.ru/supplier/public-offers/lots/539816/request/32268/";

        HttpURLConnectionExample http = new HttpURLConnectionExample();

        try {
            Map<String, String> params = http.loadMain();
            http.auth(params);
            Map<String, String> paramsToSendRequest = http.loadRequest(url);
            http.sendRequest(paramsToSendRequest, url);
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

        HttpPost request = new HttpPost(url);
        addPostHeaders(request);
        UrlEncodedFormEntity form = addFormParams(params);
        request.setEntity(form);

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        try (CloseableHttpResponse response = httpclient.execute(request, context)) {

            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            printCookies();

        }

    }

    private Map<String, String> loadRequest(String requestUrl) throws IOException {

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        HttpGet httpget = new HttpGet(requestUrl);
        addGetHeaders(httpget);

        try (CloseableHttpResponse response = httpclient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {
                Path respFile = saveResponse(IOUtils.toByteArray(content));
                try (InputStream is = Files.newInputStream(respFile);) {
                    Document doc = Jsoup.parse(is, "utf-8", requestUrl);
                    Elements inputs = doc.select("input");
                    for (Element input : inputs) {
                        if (!input.attr("name").contains("btn")
                                && !input.attr("name").contains("Button")) {
                            params.put(input.attr("name"), input.attr("value"));
                            logger.debug("{}={}", input.attr("name"), input.attr("value"));
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
            return params;
        }
    }

    private void sendRequest(Map<String, String> params, String requestUrl) throws KeystoreInitializationException, KeyStoreException, Exception {

        Keystore keystore = new Keystore();
        keystore.load(KeystoreTypes.RutokenStore);
        List<String> aliases = keystore.getAliases();
        logger.debug("Available aliases:");
        for (String alias : aliases) {
            logger.debug("{}", alias);
        }
        PrivateKey pk = keystore.loadKey(aliases.get(0), "12345678");
        Certificate cert = keystore.loadCertificate(aliases.get(0));

        PKCS7Container pkcs7 = new PKCS7Container(new GOSTSignature());
        String dataToSign = params.get("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidDataToSign");
        logger.debug("Data to sign {}", dataToSign);
        byte[] signedData = pkcs7.generatePKCS7Signature(pk, cert, dataToSign.getBytes(), false);

        params.put("__EVENTTARGET", "ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest");
        params.put("__EVENTARGUMENT", "sd_"+Base64.encodeBase64String(signedData));
        params.put("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$hfProposalDone", "1");
        params.remove("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidSignedData");
        params.remove("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidDataToSign");

        HttpPost request = new HttpPost(requestUrl);
        addPostHeaders(request);
        UrlEncodedFormEntity form = addFormParams(params);
        request.setEntity(form);

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        try (CloseableHttpResponse response = httpclient.execute(request, context)) {

            HttpEntity entity = response.getEntity();
            saveResponse(IOUtils.toByteArray(entity.getContent()));
            EntityUtils.consume(entity);
            printCookies();

        }
    }

    private void addPostHeaders(HttpPost method) {
        method.addHeader("Cache-Control", "max-age=0");
        method.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        method.addHeader("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        method.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
    }

    private void addGetHeaders(HttpGet method) {
        method.addHeader("Cache-Control", "max-age=0");
        method.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        method.addHeader("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        method.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
    }

    private void printResponseHeaders(HttpResponse resp) {
        Header[] headers = resp.getAllHeaders();

        for (Header header : headers) {
            logger.debug("Header: {}", header.toString());
        }
    }

    private Path saveResponse(byte[] response) throws IOException {
        Path path = Paths.get("/tmp/response.html");
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

    private UrlEncodedFormEntity addFormParams(Map<String, String> params) {
        List<NameValuePair> formparams = new ArrayList<>();
        logger.debug("Params list to send:");
        for (String key : params.keySet()) {
            logger.debug("{}={}", key, params.get(key));
            formparams.add(new BasicNameValuePair(key, params.get(key)));
        }

        return new UrlEncodedFormEntity(formparams, Consts.UTF_8);
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpURLConnectionExample.class.getName());

}
