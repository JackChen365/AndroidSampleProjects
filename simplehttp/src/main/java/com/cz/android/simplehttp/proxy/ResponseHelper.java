package com.cz.android.simplehttp.proxy;

public class ResponseHelper {

    /**
     * Generate a response
     *
     * @param responseCode    response code
     * @param responseMessage response message
     * @return response message
     */
    public static String buildResponse(int responseCode, String responseMessage) {
        return String.format(
                "HTTP/1.0 %d %s\r\n" +
                        "Proxy-agent: ProxyServer/1.0\r\n\r\n",
                responseCode,
                responseMessage
        );
    }

    /**
     * Generate a success response
     *
     * @return success response
     */
    public static String buildSuccessResponse() {
        return "HTTP/1.0 200 Connection established\r\n" +
                "Proxy-agent: ProxyServer/1.0\r\n\r\n";
    }

    /**
     * Generate a error response
     *
     * @return error response
     */
    public static String buildErrorResponse() {
        return "HTTP/1.0 404 Not Found\r\n" +
                "Proxy-agent: ProxyServer/1.0\r\n\r\n";
    }
}
