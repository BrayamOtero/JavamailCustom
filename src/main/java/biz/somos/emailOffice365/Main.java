/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.somos.emailOffice365;

import java.util.Date;
import java.util.Properties;
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
    
    private static String mechanismsAuth = "PLAIN";
    
    private static boolean isAuth = false;
    
    private static String emailTo;
    
    private static String trustHost;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
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
            }                                                
                
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

            Session session = Session.getInstance(System.getProperties());
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
    
}
