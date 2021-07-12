package org.dubhe.mtq.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//————————————————
//版权声明：本文为CSDN博主「要不一起ci个饭」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//原文链接：https://blog.csdn.net/char_m/article/details/107001846

public class HttpServer {
    private ServerSocket serverSocket = null;

    public HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        System.out.println("服务器启动");
        ExecutorService executorService = Executors.newCachedThreadPool();
        while (true) {
            Socket clientSocket = serverSocket.accept();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    process(clientSocket);
                }
            });
        }
    }

    public void process(Socket clientSocket) {
        try {
            // 1. 读取并解析请求
            HttpRequest request = HttpRequest.build(clientSocket.getInputStream());
            System.out.println("request: " + request);
            HttpResponse response = HttpResponse.build(clientSocket.getOutputStream());
            response.setHeader("Content-Type", "text/html");
            // 2. 根据请求计算响应
            if (request.getUrl().startsWith("/hello")) {
                response.setStatus(200);
                response.setMessage("OK");
                response.writeBody("<h1>hello</h1>");
            } else if (request.getUrl().startsWith("/calc")) {
                // 这个逻辑要根据参数的内容进行计算
                // 先获取到 a 和 b 两个参数的值
                String aStr = request.getParameter("a");
                String bStr = request.getParameter("b");
                // System.out.println("a: " + aStr + ", b: " + bStr);
                int a = Integer.parseInt(aStr);
                int b = Integer.parseInt(bStr);
                int result = a + b;
                response.setStatus(200);
                response.setMessage("OK");
                response.writeBody("<h1> result = " + result + "</h1>");
            } else if (request.getUrl().startsWith("/cookieUser")) {
                response.setStatus(200);
                response.setMessage("OK");
                // HTTP 的 header 中允许有多个 Set-Cookie 字段. 但是
                // 此处 response 中使用 HashMap 来表示 header 的. 此时相同的 key 就覆盖
                response.setHeader("Set-Cookie", "user=tz");
                response.writeBody("<h1>set cookieUser</h1>");
            } else if (request.getUrl().startsWith("/cookieTime")) {
                response.setStatus(200);
                response.setMessage("OK");
                // HTTP 的 header 中允许有多个 Set-Cookie 字段. 但是
                // 此处 response 中使用 HashMap 来表示 header 的. 此时相同的 key 就覆盖
                response.setHeader("Set-Cookie", "time=" + (System.currentTimeMillis() / 1000));
                response.writeBody("<h1>set cookieTime</h1>");
            } else {
                response.setStatus(200);
                response.setMessage("OK");
                response.writeBody("<h1>default</h1>");
            }
            // 3. 把响应写回到客户端
            response.flush();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            try {
                // 这个操作会同时关闭 getInputStream 和 getOutputStream 对象
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer(9090);
        server.start();
    }
}
