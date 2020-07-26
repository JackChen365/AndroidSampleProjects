package com.cz.android.simplehttp.upload;


import javax.swing.text.html.CSS;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUploadTest {
    public static void main(String[] args) throws IOException {
        //setup params
        multipartRequest("http://localhost:8090/");
//        postForm();
    }

    private static void postForm() throws IOException {
        URL url = new URL("http://localhost:8090/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        Map<String,String> params = new HashMap<>();
        params.put("firstParam", "value1");
        params.put("secondParam", "value2");
        params.put("thirdParam", "value3");

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();


        int responseCode = conn.getResponseCode();
        System.out.println("responseCode:"+responseCode);
        conn.disconnect();
    }

    private static String getQuery(Map<String,String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String,String> pair : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private static void multipartRequest(String url) throws IOException {
        String charset = "UTF-8";
        String param = "value";
        File textFile = new File("resources/index.html");
        File binaryFile = new File("resources/help.txt");
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
//        connection.setChunkedStreamingMode(512);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (
                OutputStream output = connection.getOutputStream();
        ) {
            // Send normal param.
            output.write(("--" + boundary+CRLF).getBytes());
            output.write(("Content-Disposition: form-data; name=\"param\""+CRLF).getBytes());
            output.write(("Content-Type: text/plain; charset=" + charset+ CRLF).getBytes());
            output.write(CRLF.getBytes());
            output.write(param.getBytes());
            output.write(CRLF.getBytes());
            output.flush();

            // Send text file.
            output.write(("--" + boundary+CRLF).getBytes());
            output.write(("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\""+CRLF).getBytes());
            output.write(("Content-Type: text/plain; charset=" + charset+CRLF).getBytes()); // Text file itself must be saved in this charset!
            output.write(("Content-Length: " + textFile.length()+CRLF).getBytes()); // Text file itself must be saved in this charset!
            output.write(CRLF.getBytes());
            output.write((Files.readAllBytes(textFile.toPath())));
            output.write((CRLF).getBytes());
            output.flush(); // CRLF is important! It indicates end of boundary.

            // Send binary file.
            output.write(("--" + boundary+CRLF).getBytes());
            output.write(("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\""+CRLF).getBytes());
            output.write(("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())+CRLF).getBytes());
            output.write(("Content-Transfer-Encoding: binary"+CRLF).getBytes());
            output.write(("Content-Length: " + binaryFile.length()+CRLF).getBytes()); // Text file itself must be saved in this charset!
            output.write(CRLF.getBytes());
            output.write(Files.readAllBytes(binaryFile.toPath()));
            output.write(CRLF.getBytes());
            output.flush(); // CRLF is important! It indicates end of boundary.

            // End of multipart/form-data.
            output.write(("--" + boundary + "--"+CRLF).getBytes());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Request is lazily fired whenever you need to obtain information about response.
        int responseCode = connection.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while(null!=(line=reader.readLine())){
            System.out.println(line);
        }
        connection.disconnect();
        System.out.println(responseCode); // Should be 200
    }
}
