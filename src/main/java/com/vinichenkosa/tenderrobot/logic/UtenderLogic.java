/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vinichenkosa.tenderrobot.logic;

import com.impulsm.signatureutils.container.PKCS7Container;
import com.impulsm.signatureutils.exceptions.KeystoreInitializationException;
import com.impulsm.signatureutils.keystore.Keystore;
import com.impulsm.signatureutils.keystore.KeystoreTypes;
import com.impulsm.signatureutils.signature.GOSTSignature;
import com.vinichenkosa.tenderrobot.model.Task;
import com.vinichenkosa.tenderrobot.model.utender.UtenderHttpCommon;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import org.apache.commons.codec.binary.Base64;
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

@Stateless
public class UtenderLogic {

    @Asynchronous
    public Future<HttpPost> prepare(Future<BasicCookieStore> cookiesFutureCont, Task task) throws Exception {

        try {
            logger.debug("Prepare task {}", task.getId());
            HttpClientContext context = HttpClientContext.create();
            while(!cookiesFutureCont.isDone()){}
            BasicCookieStore cookies = cookiesFutureCont.get();
            context.setCookieStore(cookies);
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookies).build();
            Map<String, String> params = loadRequest(task, httpClient, context);
            return new AsyncResult<>(prepareRequestToSend(params, task));
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
            throw ex;
        }
    }
    
    
    private Map<String, String> loadRequest(Task task, CloseableHttpClient httpClient, HttpClientContext context) throws IOException {

        HttpGet httpget = new HttpGet(task.getUrl());
        UtenderHttpCommon.addGetHeaders(httpget);

        try (CloseableHttpResponse response = httpClient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {

                Document doc = Jsoup.parse(content, "utf-8", task.getUrl());
                UtenderHttpCommon.saveResponse(doc, "loadRequestResponse.html");
                
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
            EntityUtils.consume(entity);
            return params;
        }
    }

    @Lock(LockType.WRITE)
    private HttpPost prepareRequestToSend(Map<String, String> params, Task task) throws KeystoreInitializationException, KeyStoreException, Exception {

        Keystore keystore = new Keystore();
        keystore.load(KeystoreTypes.RutokenStore);
//        keystore.load(KeystoreTypes.HDImageStore);
        List<String> aliases = keystore.getAliases();
        logger.debug("Available aliases:");
        for (String alias : aliases) {
            logger.debug("{}", alias);
        }
        PrivateKey pk = keystore.loadKey(aliases.get(0), "12345678");
//        PrivateKey pk = keystore.loadKey("tender", "abc123");
        Certificate cert = keystore.loadCertificate(aliases.get(0));

        PKCS7Container pkcs7 = new PKCS7Container(new GOSTSignature());
        String dataToSign = params.get("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidDataToSign");
        if (dataToSign == null) {
            return null;
        }
        logger.debug("Data to sign {}", dataToSign);
        byte[] signedData = pkcs7.generatePKCS7Signature(pk, cert, dataToSign.getBytes(), false);

        params.put("__EVENTTARGET", "ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest");
        params.put("__EVENTARGUMENT", "sd_" + Base64.encodeBase64String(signedData));
        params.put("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$hfProposalDone", "1");
        params.remove("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidSignedData");
        params.remove("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidDataToSign");

        HttpPost requestToSend = new HttpPost(task.getUrl());
        UtenderHttpCommon.addPostHeaders(requestToSend);
        UrlEncodedFormEntity form = UtenderHttpCommon.addFormParams(params);
        requestToSend.setEntity(form);
        return requestToSend;
    }
    
    private static final Logger logger = LoggerFactory.getLogger(UtenderLogic.class.getName());
}
