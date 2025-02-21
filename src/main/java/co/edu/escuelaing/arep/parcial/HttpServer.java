package co.edu.escuelaing.arep.parcial;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpServer {
    private static final int PORT = 35000;
    private static final String WEB_ROOT = "src/main/resources/static";

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Escuchando por el puerto: "+ PORT);
            while (true){
                try(Socket clientSocket = serverSocket.accept()){
                    handleRequest(clientSocket);
                }
            }
        }catch (IOException e){
            System.err.println(e.getMessage());
            System.exit(0);
        }
    }

    private static void handleRequest(Socket clientSocket) throws  IOException {
        try(PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedOutputStream dataout = new BufferedOutputStream(clientSocket.getOutputStream())){

            String requestLine = in.readLine();
            if (requestLine == null) return;
            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String fileRequested = tokens[1];
            if ("GET".equalsIgnoreCase(method)){
                handleGetRequest(fileRequested, out, dataout);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePostRequest(in, out);
            }
            String inputLine;
            while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()){
                System.out.println("Recibí: "+ inputLine);
            }
        }
    }

    private static void handlePostRequest(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        while(!(line = in.readLine()).isEmpty()){
            System.out.println("Cabecera: "+line);
        }
        while(in.ready()){
            requestBody.append((char)in.read());
        }
        String expression = requestBody.toString().trim();
        System.out.println("Cuerpo de la solicitud: "+ expression);
        if (!expression.isEmpty()){
            try{
                String operation;
                double[] numbers;

                int start = expression.indexOf('(');
                int end = expression.indexOf(')');
                if (start != -1 && end != -1){
                    operation = expression.substring(0, start).trim();
                    String numberPart = expression.substring(start + 1, end);
                    String[] values = numberPart.split(",");
                    numbers = Arrays.stream(values).mapToDouble(Double::parseDouble).toArray();
                    if(operation.equals("bbl")){
                        ArrayList<Double> result = BubbleSort.ordenar(numbers);

                        String response;
                        if(result != null){
                            response = "{\"answer\": " + result + "}";
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: application/json");
                            out.println("Content-Length: "+response.length());
                            out.println();
                            out.println(response);
                        }else{
                            response = "{\"error\": \"Invalid Operation or Parameters\"}";
                            out.println("HTTP/1.1 400 Bad Request");
                            out.println("Content-Type: application/json");
                            out.println("Content-Length: "+response.length());
                            out.println();
                            out.println(response);
                        }
                    }else{

                        Double result = ReflexCalculator.calculate(operation, numbers);
                        String response;
                        if(result != null){
                            response = "{\"answer\": " + result + "}";
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: application/json");
                            out.println("Content-Length: "+response.length());
                            out.println();
                            out.println(response);
                        }else{
                            response = "{\"error\": \"Invalid Operation or Parameters\"}";
                            out.println("HTTP/1.1 400 Bad Request");
                            out.println("Content-Type: application/json");
                            out.println("Content-Length: "+response.length());
                            out.println();
                            out.println(response);
                        }
                    }
                }else{
                    String response = "{\"error\": \"Invalid expression format\"}";
                    out.println("HTTP/1.1 400 Bad Request");
                    out.println("Content-Type: application/json");
                    out.println("Content-Length: "+response.length());
                    out.println();
                    out.println(response);
                }

            }catch (Exception e){
                String response = "{\"error\": \"Invalid expression format\"}";
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: application/json");
                out.println("Content-Length: "+response.length());
                out.println();
                out.println(response);
            }
        }
    }

    private static void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataout) throws IOException {
        if (fileRequested.startsWith("/math")){
            String[] values = getValues(fileRequested);
            String operation = values[0];
            double[] numbers = Arrays.stream(values, 1, values.length).mapToDouble(Double::parseDouble).toArray();
            Double result = ReflexCalculator.calculate(operation, numbers);
            String response;
            if (result != null){
                response = "{\"answer\": " + result + "}";
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Content-Length: "+response.length());
                out.println();
                out.println(response);
            }else{
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>Invalid Operation</h1></body></html>");
            }
            out.flush();
        }else{
            File file = new File(WEB_ROOT, fileRequested);
            String contentType = getContentType(fileRequested);
            byte[] fileData = Files.readAllBytes(file.toPath());
            if(file.exists() && !file.isDirectory()){
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: "+contentType);
                out.println("Content-Length: "+fileData.length);
                out.println();
                out.flush();
                dataout.write(fileData, 0 , fileData.length);
                dataout.flush();
            }else{
                out.println("HTTP/1.1 400 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>File Not Found</h1></body></html>");
                out.flush();
            }
        }
    }

    private static String[] getValues(String fileRequested) {
        String[] parts = fileRequested.split("\\?");
        System.out.println(parts);
        String queryPart = parts[1];
        String[] queryPairs = queryPart.split("&");
        String [] result = new String[queryPairs.length + 1];
        result[0] = parts[0].substring(parts[0].lastIndexOf('/') + 1);
        for (int i=0; i < queryPairs.length; i++){
            String[] keyValues = queryPairs[i].split("=");
            if (keyValues.length > 1){
                result[i + 1] = keyValues[1];
            }
        }
        return result;
    }

    private static String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        return "text/plain";
    }

}
