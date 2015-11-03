import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amazonaws.auth.PropertiesCredentials;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;

import org.json.*;
import org.json.simple.JSONObject;




public class APIService {
    private static CloseableHttpClient httpClient;
    private static APIService apiService;
    private String APIKey = "";
    
    //private String base_URL = "http://access.alchemyapi.com/calls/text/TextGetTextSentiment";

    private APIService(String APIKey){
        this.APIKey = APIKey;
        httpClient = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .build();
    }
    
    public static APIService getInstanceWithKey(String APIKey){
        if(apiService == null) apiService = new APIService(APIKey);
        return apiService;
    }
    
    String getSentiment(String text) throws ClientProtocolException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://access.alchemyapi.com/calls/text/TextGetTextSentiment");
        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        String apiKey = APIKey;
        
        params.add(new BasicNameValuePair("apikey", apiKey));
        params.add(new BasicNameValuePair("text", text));
        params.add(new BasicNameValuePair("outputMode", "json"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        String ret = null;
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                ret = EntityUtils.toString(entity);
            } finally {
                instream.close();
            }
        }
        return ret;
	}
    

}
