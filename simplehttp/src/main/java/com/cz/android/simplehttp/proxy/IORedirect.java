package com.cz.android.simplehttp.proxy;

import java.io.*;

public class IORedirect {

    /**
     * Default buffer size
     */
    public static final int BUFFER_SIZE = 4096;

    /**
     * Transfer bytes between from one stream to another stream
     *
     * @param fromStream source
     * @param toStream   destination
     * @throws IOException io error
     */
    public static void transfer(InputStream fromStream, OutputStream toStream) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];

        while ((bytesRead = fromStream.read(buffer, 0, BUFFER_SIZE)) >= 0) {
            toStream.write(buffer, 0, bytesRead);
        }

        toStream.flush();
    }

    /**
     * Transfer bytes between from one stream to multiple streams
     *
     * @param fromStream source
     * @param toStreams  destinations
     * @throws IOException io error
     */
    public static void transfer(InputStream fromStream, OutputStream... toStreams) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];

        while ((bytesRead = fromStream.read(buffer, 0, BUFFER_SIZE)) >= 0) {
            for (OutputStream out : toStreams)
                out.write(buffer, 0, bytesRead);
        }

        for (OutputStream out : toStreams)
            out.flush();
    }

    /**
     * Send string message to a stream
     *
     * @param input        message to send
     * @param outputStream destination
     * @throws IOException io error
     */
    public static void sendString(String input, OutputStream outputStream) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(input);
        writer.flush();
    }

    /**
     * Receive string from a stream
     *
     * @param inputStream source
     * @return message
     * @throws IOException io error
     */
    public static String receiveString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String buffer;
        StringBuilder builder = new StringBuilder();

        while ((buffer = reader.readLine()) != null && !buffer.isEmpty())
            builder.append(buffer).append('\n');

        return builder.toString();
    }
}
