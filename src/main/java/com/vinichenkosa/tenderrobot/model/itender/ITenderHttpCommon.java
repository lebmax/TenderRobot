package com.vinichenkosa.tenderrobot.model.itender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ITenderHttpCommon {

    public static void addPostHeaders(HttpPost method) {
        method.addHeader("Cache-Control", "max-age=0");
        method.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        method.addHeader("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        method.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
    }

    public static void addGetHeaders(HttpGet method) {
        method.addHeader("Cache-Control", "max-age=0");
        method.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        method.addHeader("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        method.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
    }

    public static UrlEncodedFormEntity addFormParams(Map<String, String> params) {
        List<NameValuePair> formparams = new ArrayList<>();
        //logger.debug("Params list to send:");
        for (String key : params.keySet()) {
            //logger.debug("{}={}", key, params.get(key));
            formparams.add(new BasicNameValuePair(key, params.get(key)));
        }

        return new UrlEncodedFormEntity(formparams, Consts.UTF_8);
    }

    public static void saveResponse(Document doc, String path) throws IOException {
        Files.write(Paths.get(path), doc.toString().getBytes());
    }

    public static DateTime getTime(String url) {

        DateTime result = null;
        DateTime cur;
        Client client = ClientBuilder.newClient();
        Invocation.Builder builder = client.target(url+"/public/services/datetime/GetDateTime")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        Response response = null;
        while (true) {
            try {
                response = builder.post(Entity.json(""));
                cur = new DateTime(response.getDate());
                if (result == null) {
                    result = cur;
                }
                if (cur.isAfter(result)) {
                    return cur;
                }
                //logger.debug("Server time is: {}, current result time is {}", cur, result);
            } catch (Exception ex) {
                logger.warn("Ошибка при попытке получить данные о вермени отсервера utender: {}.", ex.getMessage());
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(ITenderHttpCommon.class.getName());
}
