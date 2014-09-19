package com.vinichenkosa.tenderrobot.model.utender;

import com.impulsm.signatureutils.container.PKCS7Container;
import com.impulsm.signatureutils.exceptions.KeystoreInitializationException;
import com.impulsm.signatureutils.keystore.Keystore;
import com.impulsm.signatureutils.keystore.KeystoreTypes;
import com.impulsm.signatureutils.signature.GOSTSignature;
import com.vinichenkosa.tenderrobot.model.Task;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
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

public class UtenderTask implements Callable<Task> {

    private final HttpClientContext context = HttpClientContext.create();
    private BasicCookieStore cookies;
    private final Task task;
    private CloseableHttpClient httpclient;
    private HttpPost requestToSend;
    private boolean prepared = false;

    public UtenderTask(Task task) throws Exception {
        this.task = task;
    }

    public void prepare(BasicCookieStore cookies) throws Exception {

        logger.debug("Prepare task {}", task.getId());
        this.cookies = cookies;

        context.setCookieStore(this.cookies);
        this.httpclient = HttpClients.custom().setDefaultCookieStore(this.cookies).build();
        Map<String, String> params = loadRequest();
        prepareRequestToSend(params);
        prepared = true;

    }

    private Map<String, String> loadRequest() throws IOException {

        HttpGet httpget = new HttpGet(task.getUrl());
        UtenderHttpCommon.addGetHeaders(httpget);

        try (CloseableHttpResponse response = httpclient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {
                //Path respFile = saveResponse(IOUtils.toByteArray(content));
                //try (InputStream is = Files.newInputStream(respFile);) {
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
                //}
            }
            EntityUtils.consume(entity);
            //printCookies();
            return params;
        }
    }

    private void prepareRequestToSend(Map<String, String> params) throws KeystoreInitializationException, KeyStoreException, Exception {

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
        params.put("__EVENTARGUMENT", "sd_" + Base64.encodeBase64String(signedData));
        params.put("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$hfProposalDone", "1");
        params.remove("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidSignedData");
        params.remove("ctl00$ctl00$MainContent$ContentPlaceHolderMiddle$ctl00$scRequest$hidDataToSign");

        requestToSend = new HttpPost(task.getUrl());
        UtenderHttpCommon.addPostHeaders(requestToSend);
        UrlEncodedFormEntity form = UtenderHttpCommon.addFormParams(params);
        requestToSend.setEntity(form);
    }

    private static final Logger logger = LoggerFactory.getLogger(UtenderTask.class.getName());

    @Override
    public Task call() throws Exception {

        try {
            logger.debug("task called");
            while (!prepared) {
                logger.debug("Steel preparing");
            }
            task.setStartTime(new Date());
            try (CloseableHttpResponse response = httpclient.execute(requestToSend, context);) {
                task.setEndTime(new Date());
                logger.debug("Task finished");
            }
            //HttpEntity entity = response.getEntity();
            //Document doc = Jsoup.parse(entity.getContent(), "utf-8", requestUrl);
            //UtenderHttpCommon.saveResponse(doc, "sendRequestResponse.html");
            //EntityUtils.consume(entity);

        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                logger.error("Can't close httpClient.", ex);
            }
        }
        return task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public String toString() {
        return "UtenderTask{" + "context=" + context + ", cookies=" + cookies + ", task=" + task + ", httpclient=" + httpclient + ", requestToSend=" + requestToSend + '}';
    }

}
