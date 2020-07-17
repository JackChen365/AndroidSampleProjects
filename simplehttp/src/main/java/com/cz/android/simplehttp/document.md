
```
The Get method request message;

GET / HTTP/1.1
User-Agent: Java/1.8.0_161
Host: localhost:8090
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive

```

```
POST / HTTP/1.1
User-Agent: Java/1.8.0_161
Host: localhost:8090
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive
Content-type: application/x-www-form-urlencoded
Content-Length: 32

userPassword=123456&userName=mmt
Channel read request message.
```

Upload file to server

```
POST / HTTP/1.1
User-Agent: Android Multipart HTTP Client 1.0
Content-Type: multipart/form-data; boundary=*****1594976824894*****
Host: localhost:8090
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
Connection: keep-alive
Content-Length: 6974787

--*****1594976824894*****
Content-Disposition: form-data; name="video"; filename="upload_video.mp4"
Content-Type: video/mp4
Content-Transfer-Encoding: binary

//binary data...
```


### Response
```
HTTP/1.1 301 Moved Permanently
Location: http://www.google.com/
Content-Type: text/html; charset=UTF-8
Date: Wed, 25 Mar 2020 18:53:12 GMT
Expires: Fri, 24 Apr 2020 18:53:12 GMT
Cache-Control: public, max-age=2592000
Server: gws
Content-Length: 219
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN

<HTML><HEAD><meta http-equiv="content-type" content="text/html;charset=utf-8">
<TITLE>301 Moved</TITLE></HEAD><BODY>
<H1>301 Moved</H1>
The document has moved
<A HREF="http://www.google.com/">here</A>.
</BODY></HTML>
```