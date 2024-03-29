package com.mhall119;

import org.apache.commons.cli.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.crypto.SecretKey;

public class NexmoApp {
    private static final String balanceEndpoint = "https://rest.nexmo.com/account/get-balance?api_key=%1s&api_secret=%2s";
    private static final String messageEndpoint = "https://api.nexmo.com/v0.1/messages";
    private static final String messageBody = "{ \"from\": { \"type\": \"sms\", \"number\": \"%1s\" }, \"to\": { \"type\": \"sms\", \"number\": \"%2s\" }, \"message\": { \"content\": { \"type\": \"text\", \"text\": \"%3s\" } } }";
    private String NEXMO_API_KEY = System.getenv("NEXMO_API_KEY");
    private String NEXMO_API_SECRET = System.getenv("NEXMO_API_SECRET");
    private String NEXMO_APP_ID = System.getenv("NEXMO_APP_ID");
    private String NEXMO_PRIVATE_KEY = System.getenv("NEXMO_PRIVATE_KEY");

    public NexmoApp(CommandLine cmd) {
        if (cmd.hasOption("key")) {
            this.NEXMO_API_KEY = cmd.getOptionValue("key");
        }
        if (cmd.hasOption("secret")) {
            this.NEXMO_API_SECRET = cmd.getOptionValue("secret");
        }
        if (cmd.hasOption("appid")) {
            this.NEXMO_APP_ID = cmd.getOptionValue("appid");
        }
        if (cmd.hasOption("private-key")) {
            this.NEXMO_PRIVATE_KEY = cmd.getOptionValue("private-key");
        }

    }

    public static void main( String[] args ) {
        Options options = new Options();
        options.addOption("k", "key", true, "Nemo API key");
        options.addOption("s", "secret", true, "Nexmo secret key");
        options.addOption("a", "appid", true, "Nexmo Application ID");
        options.addOption("p", "private-key", true, "Nexmo private key file");
        options.addOption("h", "help", false, "Print this message");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            NexmoApp app = new NexmoApp(line);

            if (line.hasOption("help")) {
                printHelp(options);
                System.exit(0);
            }

            List<String> extraArgs = line.getArgList();
            if (extraArgs.size() > 1) {
                System.err.println("Too many commands. Select either 'balance' or 'sms' command.");
                printHelp(options);
                System.exit(1);
            }

            String command = "help";
            if (extraArgs.size() == 1) {
                command = extraArgs.get(0);
            }

            if (command.equalsIgnoreCase("balance")) {
                app.checkBalance();
            } else if (command.equalsIgnoreCase("sms")) {
                app.sendSMS();
            } else if (command.equalsIgnoreCase("help")) {
                printHelp(options);
            } else {
                System.err.println("Unknown command: "+command);
            }

        }
        catch( ParseException exp ) {
            // Command line options parsing failed
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "nexmo <balance|sms> [OPTION]", options );

    }

    private void checkBalance() {
        String requestPath = String.format(balanceEndpoint, this.NEXMO_API_KEY, this.NEXMO_API_SECRET);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(requestPath);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try {
                        System.out.println(EntityUtils.toString(entity));
                    } catch (IOException ex) {
                        System.err.println("Error reading HTTP response");
                    }
                }
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            System.err.println("Error making HTTP connection");
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                System.err.println("Error closing HTTP connection");
            }
        }
    }

    private void sendSMS() {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("What is your phone number?");
        String FROM_NUMBER = scanner.nextLine();

        System.out.println("What number do you want to send to?");
        String TO_NUMBER = scanner.nextLine();

        System.out.println("Type your message:");
        String MSG = scanner.nextLine();

        scanner.close();

        String requestPath = messageEndpoint;
        String requestBody = String.format(messageBody, FROM_NUMBER, TO_NUMBER, MSG);
        HttpEntity requestEntity = new StringEntity(requestBody, StandardCharsets.UTF_8);

        HttpPost httppost = new HttpPost(requestPath);
        httppost.setEntity(requestEntity);
        httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httppost.addHeader("Accept", "application/json");
        this.addAuth(httppost);
  

        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
                
        try {
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    try {
                        System.out.println(EntityUtils.toString(responseEntity));
                    } catch (IOException ex) {
                        System.err.println("Error reading HTTP response");
                    }
                }
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            System.err.println("Error making HTTP connection");
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                System.err.println("Error closing HTTP connection");
            }
        }

    }

    private void addAuth(HttpRequestBase httpRequest) {
        if (this.NEXMO_APP_ID != null && this.NEXMO_PRIVATE_KEY != null) {
            try {
                Path keyFilePath = Paths.get(this.NEXMO_PRIVATE_KEY);
                String keyContent = new String(Files.readAllBytes(keyFilePath))
                .replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

                try {
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(keyContent)));
                    String jws = Jwts.builder()
                    .setHeaderParam("type", "JWT")
                    .setSubject("mhall119")
                    .claim("application_id", this.NEXMO_APP_ID)
                    .claim("iat", Instant.now().getEpochSecond())
                    .claim("jti", UUID.randomUUID().toString())
                    .signWith(key)
                    .compact();

                    String authHeader = "Bearer " + jws;
                    httpRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                } catch (Exception ex) {
                    System.err.println("Error processing private key");
                    ex.printStackTrace();
                    System.exit(1);
                }
            } catch (Exception ex) {
                System.err.println("Error reading private key");
                ex.printStackTrace();
                System.exit(1);
            }

        } else if (this.NEXMO_API_KEY != null && this.NEXMO_API_SECRET != null) {
            String authToken = this.NEXMO_API_KEY + ":" + this.NEXMO_API_SECRET;
            byte[] encodedAuth = Base64.encodeBase64(authToken.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            httpRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }

    }
}
