package com.cz.android.simplehttp.upload;

import com.sun.tools.javac.util.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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

        List<Pair<String,String>> params = new ArrayList<>();
        params.add(new Pair<>("firstParam", "value1"));
        params.add(new Pair<>("secondParam", "value2"));
        params.add(new Pair<>("thirdParam", "value3"));

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();


        int responseCode = conn.getResponseCode();
        System.out.println("responseCode:"+responseCode);
        conn.disconnect();
    }

    private static String getQuery(List<Pair<String,String>> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String,String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.fst, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.snd, "UTF-8"));
        }

        return result.toString();
    }

    private static void multipartRequest(String url) throws IOException {
        String charset = "UTF-8";
        String param = "value";
        File textFile = new File("resources/report/index.html");
        File binaryFile = new File("resources/report/tab_sample.html");
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(512);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
        ) {
            // Send normal param.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
            writer.append(CRLF).append(param).append(CRLF).flush();

            // Send text file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
            writer.append("Content-Length: " + textFile.length()).append(CRLF); // Text file itself must be saved in this charset!
            writer.append(CRLF);
            Files.copy(textFile.toPath(), output);
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append("Content-Length: " + binaryFile.length()).append(CRLF); // Text file itself must be saved in this charset!
            writer.append(CRLF);
            Files.copy(binaryFile.toPath(), output);
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();
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
