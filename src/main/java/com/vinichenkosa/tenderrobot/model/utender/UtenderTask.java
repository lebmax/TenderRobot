package com.vinichenkosa.tenderrobot.model.utender;

import com.impulsm.signatureutils.container.PKCS7Container;
import com.impulsm.signatureutils.exceptions.KeystoreInitializationException;
import com.impulsm.signatureutils.keystore.Keystore;
import com.impulsm.signatureutils.keystore.KeystoreTypes;
import com.impulsm.signatureutils.signature.GOSTSignature;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.LocalTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtenderTask {

    private UtenderAuth auth;

    public UtenderTask(UtenderAuth auth) {
        this.auth = auth;
    }
    
    public void execute(String requestUrl) throws Exception{
        try(CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(auth.getCookieStore()).build();){
            logger.info("Sending request");
            Map<String, String> params = loadRequest(requestUrl, httpclient);
            sendRequest(params, requestUrl, httpclient);
        }
    }
    
    public static LocalTime getTime() throws IOException{
        Client client = ClientBuilder.newClient();
        javax.ws.rs.core.Response response = client.target("http://utender.ru/public/services/datetime//GetDateTime")
                .request(MediaType.APPLICATION_JSON).header("Content-Type", "application/json").post(null);
        String d = response.readEntity(com.vinichenkosa.tenderrobot.model.utender.Response.class).getD();
        ObjectMapper mapper = new ObjectMapper();
        UtenderDateTime dateTime = mapper.readValue(d, UtenderDateTime.class);
        LocalTime localTime = new LocalTime(dateTime.getTime().substring(0, 8).trim());
        logger.info("Server date: {}", localTime);
        return localTime;
    }
    
    

    private Map<String, String> loadRequest(String requestUrl, CloseableHttpClient httpclient) throws IOException {

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(auth.getCookieStore());
        HttpGet httpget = new HttpGet(requestUrl);
        UtenderHttpCommon.addGetHeaders(httpget);

        try (CloseableHttpResponse response = httpclient.execute(httpget, context)) {

            HttpEntity entity = response.getEntity();
            Map<String, String> params = new HashMap<>();
            try (InputStream content = response.getEntity().getContent()) {
                //Path respFile = saveResponse(IOUtils.toByteArray(content));
                //try (InputStream is = Files.newInputStream(respFile);) {
                    Document doc = Jsoup.parse(content, "utf-8", requestUrl);
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

    private void sendRequest(Map<String, String> params, String requestUrl, CloseableHttpClient httpclient) throws KeystoreInitializationException, KeyStoreException, Exception {

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
        UtenderHttpCommon.addPostHeaders(request);
        UrlEncodedFormEntity form = UtenderHttpCommon.addFormParams(params);
        request.setEntity(form);

        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(auth.getCookieStore());

        try (CloseableHttpResponse response = httpclient.execute(request, context)) {

            HttpEntity entity = response.getEntity();
            //saveResponse(IOUtils.toByteArray(entity.getContent()));
            EntityUtils.consume(entity);
            //printCookies();

        }
    }

    private static final Logger logger = LoggerFactory.getLogger(UtenderTask.class.getName());
    
}
