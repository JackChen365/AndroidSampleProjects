package com.cz.android.simplehttp;

import com.okay.java.sample.http.header.RawHeaders;
import com.okay.java.sample.http.header.ResponseHeaders;
import com.okay.java.sample.http.pool.Connection;
import com.okay.java.sample.http.pool.ConnectionPool;

import java.io.*;
import java.net.*;

public class HttpClient {
	private static final int STATUS_LINE=0;
	private static final int HEADER_LINE=1;
	private static final int BODY_LINE=2;
	public static void main(String[] args) throws IOException {
//		httpPost();
//		httpGet();
//		testConnectionPool();
		httpGetTimeOut();
	}

	private static void testConnectionPool() throws IOException {
		System.setProperty("http.keepAliveDuration",String.valueOf(2*1000));
		for(int i=0;i<3;i++){
			httpGetUseConnectionPool();
			try {
				Thread.sleep(3*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void httpGet() throws IOException {
		URI uri = URI.create("http://localhost:8090/");
		InetAddress inetAddress = InetAddress.getByName(uri.getHost());
		Socket socket = new Socket(inetAddress, uri.getPort());
		String path = "/";

		// Send headers
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		wr.write("Get "+path+" HTTP/1.0\r\n");
		wr.write("Host: localhost:8090\r\n");
		wr.write("User-Agent: Java/1.8.0_161\r\n");
		wr.write("\r\n");

		// Send parameters
		wr.write("\r\n");
		wr.flush();

		// Get response
		InputStream inputStream = socket.getInputStream();
		RawHeaders rawHeaders = RawHeaders.fromBytes(inputStream);
		ResponseHeaders responseHeaders = new ResponseHeaders(uri, rawHeaders);

		int responseCode = rawHeaders.getResponseCode();
		if(HttpURLConnection.HTTP_OK==responseCode){
			int contentLength = responseHeaders.getContentLength();
			byte[] bytes=new byte[contentLength];
			int read = inputStream.read(bytes);
			String result = new String(bytes);
			System.out.println(read);
		}
		inputStream.close();
		wr.close();
		socket.close();
	}

	private static void httpGetTimeOut() throws IOException {
		URI uri = URI.create("http://localhost:8090/");
		InetSocketAddress httpSocketAddress = new InetSocketAddress(uri.getHost(),uri.getPort());
		Socket socket = new Socket();
		socket.connect(httpSocketAddress, 2*1000);
		socket.setSoTimeout(2*1000);
		String path = "/";

		// Send headers
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		wr.write("Get "+path+" HTTP/1.0\r\n");
		wr.write("Host: localhost:8090\r\n");
		wr.write("User-Agent: Java/1.8.0_161\r\n");
		wr.write("\r\n");

		// Send parameters
		wr.write("\r\n");
		wr.flush();

		// Get response
		InputStream inputStream = socket.getInputStream();
		RawHeaders rawHeaders = RawHeaders.fromBytes(inputStream);
		ResponseHeaders responseHeaders = new ResponseHeaders(uri, rawHeaders);

		int responseCode = rawHeaders.getResponseCode();
		if(HttpURLConnection.HTTP_OK==responseCode){
			int contentLength = responseHeaders.getContentLength();
			byte[] bytes=new byte[contentLength];
			int read = inputStream.read(bytes);
			String result = new String(bytes);
			System.out.println(read);
		}
		socket.close();
	}

	private static void httpGetUseConnectionPool() throws IOException {
		URI uri = URI.create("http://localhost:8090/");
		ConnectionPool connectionPool = ConnectionPool.getDefault();
		String host = uri.getHost();
		int port = uri.getPort();
		Connection connection = connectionPool.get(host,port);
		if(null==connection){
			System.out.println("Create a new connection");
			connection=new Connection();
			connection.connect(host,port,10*1000,10*1000);
		}
		Socket socket = connection.getSocket();
		String path = "/";

		// Send headers
		sendRequest(socket, path);

		// Get response
		InputStream inputStream = socket.getInputStream();

		//For testing
//		String result = streamToString(inputStream, "utf-8");
//		System.out.println(result);
//		System.out.println("----------------");

		RawHeaders rawHeaders = RawHeaders.fromBytes(inputStream);
		int responseCode = rawHeaders.getResponseCode();
		if(HttpURLConnection.HTTP_OK==responseCode){
			String text = streamToString(inputStream, "utf-8");
			System.out.println(text);
		}
		connectionPool.recycle(connection);
	}

	private static void sendRequest(Socket socket, String path) throws IOException {
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		wr.write("Get "+path+" HTTP/1.0\r\n");
		wr.write("Host: localhost:8090\r\n");
		wr.write("User-Agent: Java/1.8.0_161\r\n");
		wr.write("\r\n");
		// Send parameters
		wr.write("\r\n");
		wr.flush();
	}

	private static String streamToString(InputStream inputStream,String encodeType){
		String resultString = null ;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int len;
		byte data[]=new byte[1024];
		try {
			while((len=inputStream.read(data))!=-1){
				byteArrayOutputStream.write(data,0,len);
				if(len < data.length){
					break;
				}
			}
			byte[] allData = byteArrayOutputStream.toByteArray();
			resultString = new String(allData,encodeType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultString ;
	}

	private static void httpPost() throws IOException {
		String params = URLEncoder.encode("param1"+"="+"value1", "UTF-8");
		params += "&";
		params += URLEncoder.encode("param2"+"="+"value2", "UTF-8");

		String hostname = "localhost";
		int port = 8090;

		InetAddress inetAddress = InetAddress.getByName(hostname);
		Socket socket = new Socket(inetAddress, port);
		String path = "/";

		// Send headers
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		wr.write("POST "+path+" HTTP/1.0\r\n");
		wr.write("Content-Length: "+params.length()+"\r\n");
		wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
		wr.write("\r\n");

		// Send parameters
		wr.write(params);
		wr.flush();

		// Get response
		BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line;

		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
		wr.close();
		rd.close();
	}

}