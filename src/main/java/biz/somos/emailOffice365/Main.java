/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.somos.emailOffice365;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author SomoS
 */
public class Main {
    private static boolean testIMAP = true;
    private static boolean testSTMP;
    
    private static int port;
    private static String host;
    
    private static String email_user;
    private static String password;
    
    private static boolean isSocketFactory = false;
    
    private static boolean isImaps = false;
    private static boolean isStmps = false;
    
    private static String protocol = "";
    
    private static boolean isSSL = false;
    private static boolean isStartTLS = false;
    
    private static String mechanismsAuth = "";
    
    private static boolean isAuth = false;
    
    private static String emailTo;
    
    private static String trustHost;
    private static boolean getToken = false;
    private static String tenantID;
    private static String clientID;
    private static String grantType;
    private static String scope;
    private static String clientSecret;
    private static boolean library = false;
    private static boolean ROPC = false;
    private static boolean clientCred = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        if (args == null){
            System.out.println("Especifique el protocolo");
            return;            
        }
        for (String arg : args){
            String[] values = arg.split("=");            
            switch (values[0]){
                case "-I":
                    testIMAP = true;
                    testSTMP = false;
                    break;
                case "-S":
                    testIMAP = false;
                    testSTMP = true;
                    break;
                case "-port":
                    port = Integer.valueOf(values[1]);
                    break;
                case "-host":                    
                    host = values[1];
                    break;
                case "-user":
                    email_user = values[1];
                    break;
                case "-password":
                    password = values[1];
                    break;
                case "-socketFactory":
                    isSocketFactory = true;
                    break;
                case "-imaps":
                    isImaps = true;
                    break;
                case "-smtps":
                    isStmps = true;
                    break;
                case "-ssl":
                    isSSL = true;
                    break;
                case "-sTLS":
                    isStartTLS = true;
                    break;
                case "-mechAuth":
                    mechanismsAuth = values[1];
                    break;
                case "-auth":
                    isAuth = true;
                    break;
                case "-to":
                    emailTo = values[1];
                    break;
                case "-trusthost":
                    trustHost = values[1];
                    break;
                case "-tenant":
                    tenantID = values[1];
                    break;
                case "-clientID":
                    clientID = values[1];
                    break;
                case "-grantType":
                    grantType = values[1];
                    break;
                case "-scope":
                    scope = values[1];
                    break;
                case "-lib":
                    library = true;
                    break;
                case "-token":
                    getToken = true;
                    break;
                case "-ROPC":
                    ROPC = true;                    
                    break;
                case "-clientCred":
                    clientCred = true;
                    break;
            }                                                
                
        }
        
        if (getToken){
            getToken();
            return;
        }
        if(testIMAP){
            if (isImaps)
                protocol = "imaps";
            else
                protocol = "imap";
            System.out.println("------------TEST IMAP--------------");
            System.out.println("PROTOCOL: " + protocol);
            readIMAP();
            
            return;
        }
        if (testSTMP){
            if (isStmps)
                protocol = "smtps";
            else
                protocol = "smtp";
            System.out.println("------------TEST SMTP--------------");
            System.out.println("PROTOCOL: " + protocol);
            sendSTMP();
        }        
    }
    
    public static void readIMAP(){
        System.out.println("Inside MailReader()...");        
        Properties props = new Properties();
        // Set manual Properties        
        System.out.println("host: " + host);
        props.put("mail."+protocol+".port", String.valueOf(port));        
        props.put("mail."+protocol+".host", host);
        
        setPropetries(props);
        try {
            /* Create the session and get the store for read the mail. */

            Session session = Session.getInstance(props);
            session.setDebug(true);
            
            Store store = session.getStore(protocol);

            store.connect(host, port, email_user, password);                        

        } catch (MessagingException e) {
            System.out.println("Exception while connecting to server: " + e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(2);
        }    
    }
    
    private static void setPropetries(Properties props){
        if (isSocketFactory){
            final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
            props.setProperty("mail."+protocol+".socketFactory.class", SSL_FACTORY);
            props.setProperty("mail."+protocol+".socketFactory.fallback", "false");
            props.setProperty("mail."+protocol+".socketFactory.port", String.valueOf(port));
        }
        if(isSSL){
           props.setProperty("mail."+protocol+".ssl.enable", "true"); 
        }
        if(isStartTLS){           
           props.setProperty("mail."+protocol+".starttls.enable", "true"); 
        }
        if(isAuth)
            props.setProperty("mail."+protocol+".auth", "true"); 
        if (trustHost != null)
            props.setProperty("mail."+protocol+".ssl.trust", trustHost);
        if (!mechanismsAuth.isEmpty()){
            props.setProperty("mail."+protocol+".auth.mechanisms", mechanismsAuth);
            props.put("mail."+protocol+".auth.login.disable", "true");
            props.put("mail."+protocol+".auth.plain.disable", "true");
            //props.put("mail."+protocol+".ssl.enable", "true");
        }            
        
        props.put("mail.debug", "true");
        props.put("mail.debug.auth", "true");
        
        System.out.println("PROPERTIES:\n " + props.toString() +"\n\n");
    }
    
    private static void sendSTMP(){
        Properties props = new Properties();
        
        props.put("mail."+protocol+".port", String.valueOf(port));
        props.put("mail."+protocol+".host", host);        
        setPropetries(props);
                
        Session session = Session.getInstance(props, new Authenticator() {          
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email_user,
                        password);          
            }       
        });
        session.setDebug(true);
        
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(email_user);

            msg.setRecipients(Message.RecipientType.TO,
                    emailTo);
            msg.setSubject("Testing SMTP using [" + email_user + "]");
            msg.setSentDate(new Date());
            msg.setText("Hey, this is a test from [" + email_user + "], Sending via Java Mail API");

            Transport.send(msg);
            System.out.println("Sent Ok");
        } catch (MessagingException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
    
    private static void getToken() throws Exception{       
        if (library && ROPC){
            System.out.println("----- Libreria msal4j con flujo ROPC -----");
            try{
                System.out.print("Ingrese authority: ");                
                String authority = System.console().readLine();
                System.out.print("Ingrese clientID: ");                
                clientID = System.console().readLine();                
                //String authority = "https://login.microsoftonline.com/" + tenantID;
                System.out.print("Ingrese scope: ");                
                scope = System.console().readLine();                
                System.out.print("Ingrese email: ");                
                email_user = System.console().readLine();
                System.out.println("Email user: " + email_user);                
                PublicClientApplication app = PublicClientApplication.builder(clientID).authority(authority).build();                
                System.out.print("Ingrese contraseña: ");            
                char[] passChar = System.console().readPassword();
                UserNamePasswordParameters parameters = UserNamePasswordParameters.builder(Collections.singleton(scope), email_user, passChar).build();
                String token = app.acquireToken(parameters).get().accessToken();
                System.out.println("Token obtenido: " + token);
                return;
            }catch(Exception ex){
                System.out.println("Hay una excepción de tipo - " + ex.getClass());
                System.out.println("Exception message - " + ex.getMessage());
                throw ex;
            }
        } 
        if (library && clientCred){
            System.out.println("----- Libreria msal4j con flujo de concesión de credenciales de cliente -----");
            try{
                System.out.print("Ingrese authority: ");                
                String authority = System.console().readLine();
                System.out.print("Ingrese clientID: ");                
                clientID = System.console().readLine();                
                //String authority = "https://login.microsoftonline.com/" + tenantID;
                System.out.print("Ingrese scope: ");                
                scope = System.console().readLine();
                System.out.print("Ingrese client secret: ");                
                clientSecret = System.console().readLine();
                
                ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                        clientID,
                        ClientCredentialFactory.createFromSecret(clientSecret))
                        .authority(authority)                        
                        .build();
                ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
                        Collections.singleton(scope))
                        .build();
                String token = app.acquireToken(clientCredentialParam).get().accessToken();
                System.out.println("Token obtenido: " + token);
            }catch (Exception ex){
                System.out.println("Hay una excepción de tipo - " + ex.getClass());
                System.out.println("Exception message - " + ex.getMessage());
                throw ex;
            }
        }
        if(!library && ROPC){
            System.out.println("----- Sin uso de la librería con flujo ROPC -----");
            try{
                System.out.print("Ingrese authority: ");                
                String authority = System.console().readLine();
                System.out.print("Ingrese clientID: ");                
                clientID = System.console().readLine();                
                //String authority = "https://login.microsoftonline.com/" + tenantID;
                System.out.print("Ingrese scope: ");                
                scope = System.console().readLine();                
                System.out.print("Ingrese email: ");                
                email_user = System.console().readLine();
                System.out.print("Ingrese client secret: ");                
                clientSecret = Arrays.toString(System.console().readPassword());
                
                //URL url = new URL("https://login.microsoftonline.com/" + tenantID + "/oauth2/v2.0/token");
                System.out.print("Please input the password for: ");            
                char[] passChar = System.console().readPassword();
                password = Arrays.toString(passChar);

                Map<String,String> arguments = new HashMap<>();
                arguments.put("client_id", clientID);
                //arguments.put("scope", "user.read openid profile offline_access");
                arguments.put("scope", scope);
                if(!clientSecret.isEmpty() || clientSecret != null)
                    arguments.put("client_secret", clientSecret);
                arguments.put("username", email_user);
                arguments.put("password", password);
                arguments.put("grant_type", "password");
                
                String url_str = authority + "/oauth2/v2.0/token";
                sendRequest(url_str, arguments);
            }catch(Exception e){
                System.out.println("Error: " + e);
            }      
        }
        if (!library && clientCred){
            System.out.println("----- Sin uso de la librería con flujo de concesión de credenciales de cliente -----");
            try{                
                System.out.print("Ingrese authority: ");                
                String authority = System.console().readLine();
                System.out.print("Ingrese clientID: ");                
                clientID = System.console().readLine();                
                //String authority = "https://login.microsoftonline.com/" + tenantID;
                System.out.print("Ingrese scope: ");                
                scope = System.console().readLine();
                System.out.print("Ingrese client secret: ");                
                clientSecret = System.console().readLine();
                Map<String,String> arguments = new HashMap<>();
                arguments.put("client_id", clientID);
                arguments.put("scope", scope);
                arguments.put("client_secret", clientSecret);
                arguments.put("grant_type", "client_credentials");
                
                String url_str = authority + "/oauth2/v2.0/token";
                sendRequest(url_str, arguments);
            }catch(Exception e){
                System.out.println("Error: " + e);
            }
            
        }
    }
    
    private static void sendRequest(String url_str, Map<String,String> arguments) throws MalformedURLException, IOException{
        URL url = new URL(url_str);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);        

        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String,String> entry : arguments.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
                 + URLEncoder.encode(entry.getValue(), "UTF-8"));
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        try(BufferedReader br = new BufferedReader(
            new InputStreamReader(http.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }catch(Exception e){
            try(BufferedReader br = new BufferedReader(
            new InputStreamReader(http.getErrorStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            System.out.println("Error: " + response.toString());
            }
        }
    }
}
