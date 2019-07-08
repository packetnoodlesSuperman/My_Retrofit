Http协议是基于TCP/IP协议的应用层协议 
它不涉及数据包传输，主要规定客户端与服务器之间的通信格式 默认使用80端口

HTTP 0.9版本

request --->
GET /index.html

<--- Response
<html>
    <body>Hello World</body>
</html>    

HTTP 1.0版本


HTTP 1.1版本

request --->
GET  /index.html?xx=xx  HTTP/1.1 --> 请求行 （请求方法 空格 URL 空格 协议版本 回车符换行符）
Host：www.baidu.com              --> 请求头  (头部字段名：值 回车符换行符)
User-Agent: ···               --> 请求头  (头部字段名：值 回车符换行符)
Accept: text/html, ···        --> 请求头  (头部字段名：值 回车符换行符)
                                     <回车符换行符>   
{"content": "hello world"}       --> 请求体  (一般使用JS数据格式)

<--- Response
HTTP/1.1 200 OK                  --> 响应行  (版本 空格 状态码 空格 解释状态码短语 回车符换行符)
Server: nginx                    --> 响应头  (头部字段名：值 回车符换行符) 
Date: ···                     --> 响应头  (头部字段名：值 回车符换行符)
Content-Type: application/json   --> 响应头  (头部字段名：值 回车符换行符)
Connection: close                --> 响应头  (头部字段名：值 回车符换行符)
                                     <回车符换行符>    
{"code":200,"msg":"","data":""}  --> 响应体  (一般使用JS数据格式)

HTTP 2版本 (它不叫 HTTP/2.0，因为标准委员会不打算再发布子版本 下一个新版本将是 HTTP/3)
    