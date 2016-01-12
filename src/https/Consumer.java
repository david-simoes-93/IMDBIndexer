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
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.image.Image;

import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public static String getYoutubeID(String title, String year) {
        String[] fields = title.split("[^a-zA-Z\\d\\s:]");

        String importIO = "https://api.import.io/store/connector/404864f4-0b74-471a-ac9b-b7df70913624/_query?input=webpage/url:https%3A%2F%2Fwww.youtube.com%2Fresults%3Fsearch_query%3Dtrailer%2B" + year;
        for (String str : fields) {
            try {
                importIO += URLEncoder.encode(" " + str, "UTF-8");
            } catch (Exception ex) {
                ex.printStackTrace();
                return "";
            }
        }
        importIO += "&&_apikey=4911226d82c84f2f9e06e04bffad812e3ee3dd322ae4d7309f3751ce6913e2da924f27aabd67c1a8d8aee488dc392b80c9de4885cd3d16c6d630151818526d3b5b50b3d57bcf816bfa015eeeb4989a1f";
        System.out.println(importIO);

        String results = Consumer.getJSON(importIO);

        String retVal = "";
        JSONArray mainObj = new JSONObject(results).getJSONArray("results");
        if (mainObj.length() > 0) {
            String[] link = mainObj.getJSONObject(0).getString("uixtile_link").split("v=");
            retVal = link[link.length - 1];
        }

        return retVal;
    }

    public static String getMagnetLink(String title, String year) {
        String magnetLink = null;
        String magnet = "https://kickass.unblocked.li/usearch/" + year;
        String[] fields = title.split("[^a-zA-Z\\d\\s:]");
        for (String str : fields) {
            try {
                magnet += URLEncoder.encode(" " + str, "UTF-8");
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        magnet += "/?rss=1";
        magnet = magnet.replaceAll("\\+", "%20");

        String results = Consumer.getXML(magnet);
        if (results.equals("<>")) {
            //torrentLink.setVisible(false);
            return null;
        }

        Pattern p = Pattern.compile("<torrent:magnetURI>.*?</torrent:magnetURI>", Pattern.DOTALL);
        Matcher m = p.matcher(results);
        if (m.find()) {
            magnetLink = m.group().replace("<torrent:magnetURI><![CDATA[", "").replace("]]></torrent:magnetURI>", "");
            System.out.println(magnetLink);
        } else {
            //torrentLink.setVisible(false);
            return null;
        }

        return magnetLink;
    }

    public static boolean getImage(String url) {
        try {
            URL imageUrl = new URL(url);
            try (InputStream imageReader = new BufferedInputStream(
                    imageUrl.openStream());
                    OutputStream imageWriter = new BufferedOutputStream(
                            new FileOutputStream(new File("").getAbsolutePath() + File.separator + "temp.temp"));) {
                int readByte;
                while ((readByte = imageReader.read()) != -1) {
                    imageWriter.write(readByte);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}
