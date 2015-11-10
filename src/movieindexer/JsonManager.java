/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movieindexer;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author BlueMoon
 */
public class JsonManager {

    public static boolean createEmptyJson(String t) {
        JSONArray list = new JSONArray();

        JSONObject mainObj = new JSONObject();
        mainObj.put("movies", list);

        try {
            if (!new File(t + ".json").createNewFile()) {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        // Commit to file
        try (PrintWriter fout = new PrintWriter(new File(t + ".json"))) {
            mainObj.write(fout);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static void removeJson(String t) {
        JSONArray list = new JSONArray();

        try (Scanner fin = new Scanner(new File(t + ".json")).useDelimiter("\\Z")) {
            String content = fin.next();
            list = new JSONObject(content).getJSONArray("movies");
        } catch (Exception ex) {
            System.out.println(".json file not found.");
            list = new JSONArray();
        }

        for (int i = 0; i < list.length(); i++) {
            JSONObject curr = list.getJSONObject(i);
            File photo = new File(curr.getString("id") + ".jpg");
            if (photo.exists()) {
                photo.delete();
            }
        }
        new File(t + ".json").delete();

    }

    public static JSONArray readJson(String t) {
        JSONArray list;

        try (Scanner fin = new Scanner(new File(t + ".json")).useDelimiter("\\Z")) {
            String content = fin.next();
            list = new JSONObject(content).getJSONArray("movies");
        } catch (Exception ex) {
            System.out.println(".json file not found.");
            list = new JSONArray();
        }
        return list;
    }

    public static void writeJson(String t, JSONArray j) {
        JSONObject mainObj = new JSONObject();
        mainObj.put("movies", j);

        try (PrintWriter fout = new PrintWriter(new File(t + ".json"))) {
            mainObj.write(fout);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
