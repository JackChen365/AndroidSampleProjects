package com.cz.android.simplehttp.proxy;

public class Request {

    /**
     * Http Request message
     */
    private final String message;

    /**
     * Constructor
     *
     * @param message http request message
     */
    public Request(String message) {
        this.message = message;
    }

    /**
     * Get request message
     *
     * @return request message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get first line of the request message
     * For example, "GET url HTTP/1.1"
     *
     * @return request line
     */
    public String getRequestLine() {
        return message.substring(0, message.indexOf('\n'));
    }

    /**
     * Get request method
     * For example, "GET", "CONNECT"
     *
     * @return method
     */
    public String getMethod() {
        String requestLine = getRequestLine();
        return requestLine.substring(0, requestLine.indexOf(' '));
    }

    /**
     * Get request url
     * For example, "http://www.example.com/", "https://www.example.com:443"
     *
     * @return url
     */
    public String getURL() {
        String requestLine = getRequestLine();
        int startIndex = requestLine.indexOf(' ') + 1;
        String url = requestLine.substring(startIndex, requestLine.lastIndexOf(' '));

        return isHttpsConnect() ? "https://" + url : url;
    }

    /**
     * Get host name of the url
     * For example, "www.example.com"
     *
     * @return host name
     */
    public String getHostName() {
        String url = getURL();
        int slashIndex = url.indexOf("//");

        if (slashIndex == -1)
            return url;

        slashIndex += 2;

        return isHttpsConnect() ?
                url.substring(slashIndex, url.lastIndexOf(':')) :
                url.substring(slashIndex, url.indexOf('/', slashIndex));
    }

    /**
     * Get port number of the https url
     *
     * @return port number
     */
    public int getPortNo() {
        String url = getURL();
        String port = url.substring(url.lastIndexOf(':') + 1);
        return Integer.parseInt(port);
    }

    /**
     * Get file name from the url
     * For example, "ui.css"
     *
     * @return file name
     */
    public String getFileName() {
        String url = getURL();
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        return fileName.equals("") ? getHostName() : fileName;
    }

    /**
     * Check if a request is a https request
     *
     * @return true if https request
     */
    public boolean isHttpsConnect() {
        return getMethod().equals("CONNECT");
    }
}
