package org.example;

public class Main {
    public static void main(String[] args) {
        int port = 9000;
        Server server = new Server();
        server.start(port);
        server.close();
    }
}