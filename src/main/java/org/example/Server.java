package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private OutputStream out;
    private BufferedReader in;

    public String findRequestedFile(String route, String dir) {
        /* Recursively find the requested file */
        String file_path = "static/404.html";
        File folder = new File(dir);
        for (File fileEntry : folder.listFiles()) {
            String file_name = dir + "/" + fileEntry.getName();
            if (fileEntry.isDirectory()) {
                file_path = findRequestedFile(route, file_name);
            }
            else if (route.equals(file_name)) {
                return file_name;
            }
        }

        return file_path;
    }


    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            String host_address = serverSocket.getInetAddress().getHostAddress();
            System.out.println("Server listening on http://" + host_address + ":" + serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                out = clientSocket.getOutputStream();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String route_line = in.readLine();
                route_line = route_line.strip();
                System.out.println(route_line);

                String[] route_items = route_line.split(" ");
                String route = "static".concat(route_items[1]);

                String html_ext = route.substring(route.length()-5);
                String ico_ext = route.substring(route.length()-4);

                if (!html_ext.equals(".html") && !ico_ext.equals(".ico") && route.charAt(route.length()-1) != '/') {
                   route = route.concat("/");
                }

                if (route.charAt(route.length() - 1) == '/') {
                    route = route.concat("index.html");
                }

                String file_path = findRequestedFile(route, "static");

                if (file_path.equals("static/favicon.ico")) {
                    byte[] res_header_buf = "HTTP/1.1 200 OK\n\n".getBytes();
                    byte[] img_buf = readImageFile(file_path);

                    ByteBuffer buf = ByteBuffer.allocate(res_header_buf.length + img_buf.length);
                    buf.put(res_header_buf);
                    buf.put(img_buf);

                    out.write(buf.array());
                    out.flush();
                } else {
                    String html = readHtmlFile(file_path);
                    byte[] buf = "HTTP/1.1 200 OK\n\n".concat(html).getBytes();
                    out.write(buf);
                    out.flush();
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println(e);
            printCodeError(e.getStackTrace());
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println(e);
            printCodeError(e.getStackTrace());
        }
    }

    public String readHtmlFile(String path) {
        File myObj = new File(path);
        try {
            Scanner myReader = new Scanner(myObj);
            String html = new String();
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                html = html.concat(data + "\n");
            }
            return html;
        } catch (FileNotFoundException e) {
            System.out.println(e);
            printCodeError(e.getStackTrace());
        }
        return "";
    }

    public byte[] readImageFile(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, "png", os);

            return os.toByteArray();
        } catch (IOException e) {
            System.out.println(e);
            printCodeError(e.getStackTrace());
        }
        return "".getBytes();
    }

    public void printCodeError(StackTraceElement[] stackTraceElement) {
        for (StackTraceElement stack : stackTraceElement) {
            if (stack.toString().contains("Server.java")) {
                System.out.println("at ".concat(stack.toString()));
                break;
            }
        }
    }
}
