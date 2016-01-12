/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package https;

/**
 *
 * @author asus
 */
import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class Consumer {

    public static String getJSON(String urlToRead) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        StringBuilder result = new StringBuilder();
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e) {
            //e.printStackTrace();
            return "{}";
        }
        return result.toString();
    }

    public static String getXML(String urlToRead) {
        System.out.println(urlToRead);

        URL url;
        BufferedReader rd;
        String line;
        StringBuilder result = new StringBuilder();

        try {
            HttpsURLConnection con;
            url = new URL(urlToRead);
            //con = getConnection(true, "user", "pass", url);
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            con.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

        } catch (Exception e) {
            e.printStackTrace();
            return "<>";
        }

        return result.toString();
    }

}
