package com.cz.android.simplehttp.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Properties;

public class HttpUrlConnectionProxyTest {
    public static void main(String[] args) throws IOException {
        String url = "http://localhost:8090/";
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8099));
        HttpURLConnection connection= (HttpURLConnection) new URL(url).openConnection(proxy);
        int responseCode = connection.getResponseCode();
        if(HttpURLConnection.HTTP_OK==responseCode){
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while(null!=(line=reader.readLine())){
                System.out.println(line);
            }
        }
    }
}
