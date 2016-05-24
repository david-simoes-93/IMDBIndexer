/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movieindexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author asus
 */
public class ImdbList extends Tab {

    ScrollPane scroller;
    FlowPane flow;
    TabPane parent;
    AddMenu am;
    String jsonName;
    
    public ImdbList(TabPane t, AddMenu am, String name) {
        super(name);
        this.parent = t;
        this.am = am;
        this.jsonName=name;

        this.flow = new FlowPane();
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setAlignment(Pos.TOP_CENTER);
        am.fp.add(flow);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        flow.setPrefWrapLength(bounds.getMaxX() - 10);

        JSONArray list;

        try (Scanner fin = new Scanner(new File(jsonName + ".json")).useDelimiter("\\Z")) {
            System.out.println("Reading "+name);
            String content = fin.next();
            //System.out.println("Reading "+name+": "+content);
            list = new JSONObject(content).getJSONArray("movies");
            System.out.println("Done.");
            for (int i = 0; i < list.length(); i++) {
                Text title = new Text(list.getJSONObject(i).getString("title") + " (" + list.getJSONObject(i).getString("year") + ")");
                title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                title.setWrappingWidth(200);
                title.textAlignmentProperty().set(TextAlignment.CENTER);

                Image img = new Image("file:" + list.getJSONObject(i).getString("id") + ".jpg");
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(200);
                imgView.setFitHeight(300);
                imgView.setPreserveRatio(true);

                Text idRef = new Text(list.getJSONObject(i).getString("id"));
                idRef.setVisible(false);

                flow.getChildren().add(am.configureVBox(title, imgView, idRef, am, name));
            }
        } catch (FileNotFoundException ex) {
            System.out.println(name + ".json file not found. Starting empty ImdbList");
            System.out.println("\t"+ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        scroller = new ScrollPane();
        scroller.setContent(flow);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        this.setContent(scroller);
        this.setText(name);
    }

    // returns {position at which movie was inserted, movie list size}
    public int[] addMovie(boolean local, String[] movieDetails) {
        JSONArray list = JsonManager.readJson(jsonName);

        JSONObject curr = new JSONObject();
        curr.put("title", movieDetails[0]);
        curr.put("year", movieDetails[1]);
        curr.put("director", movieDetails[2]);
        curr.put("genre", movieDetails[3]);
        curr.put("actors", movieDetails[4]);
        curr.put("score", movieDetails[5]);
        curr.put("orderTitle", movieDetails[6]);
        curr.put("id", movieDetails[7]);

        int i = 0;

        // Remove from JSON if local
        if (local) {
            while (i < list.length()) {
                if (((JSONObject) list.get(i)).getString("id").equals(movieDetails[7])) {
                    list.remove(i);
                    break;
                }
                i++;
            }
        }

        // Insert into JSON 
        i = 0;
        if (list.length() > 0) {
            while (i < list.length() && ((JSONObject) list.get(i)).getString("orderTitle").compareTo(movieDetails[6]) < 0) {
                i++;
            }

            for (int j = list.length(); j > i; j--) {
                list.put(j, list.getJSONObject(j - 1));
            }
        }
        list.put(i, curr);

        JsonManager.writeJson(jsonName, list);

        // Remove from pane (if local)
        if (local) {
            removeFromFlowpane(movieDetails[7]);
        }

        if (!local) {
            File photo = new File(movieDetails[7] + ".jpg");
            if (photo.exists()) {
                //photo.delete();
            } else {
                new File("temp.temp").renameTo(photo);
            }
        }

        // Add into pane
        insertInFlowpane(i, curr);

        return new int[]{i, list.length()};
    }

    public void removeMovie(String id) {
        JSONArray list = JsonManager.readJson(jsonName);

        for (int i = 0; i < list.length(); i++) {
            JSONObject curr = list.getJSONObject(i);
            if (curr.getString("id").equals(id)) {
                list.remove(i);
            }
        }

        JsonManager.writeJson(jsonName, list);

        removeFromFlowpane(id);

    }

    private void insertInFlowpane(int pos, JSONObject movie) {
        Text title2 = new Text(movie.getString("title") + " (" + movie.getString("year") + ")");
        title2.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title2.setWrappingWidth(200);
        title2.textAlignmentProperty().set(TextAlignment.CENTER);

        Image img = new Image("file:" + movie.getString("id") + ".jpg");
        ImageView imgView2 = new ImageView(img);
        imgView2.setFitWidth(200);
        imgView2.setFitHeight(300);
        imgView2.setPreserveRatio(true);

        Text idRef = new Text(movie.getString("id"));
        idRef.setVisible(false);

        flow.getChildren().add(pos, createMovieVBox(title2, imgView2, idRef, jsonName));
    }

    private VBox createMovieVBox(Text title, ImageView imgView, Text idRef, String name) {
        VBox vbox = new VBox();
        vbox.setPrefSize(200, 328);
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.getChildren().add(title);
        vbox.getChildren().add(imgView);
        vbox.getChildren().add(idRef);

        vbox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String id = ((Text) vbox.getChildren().get(2)).getText();
                event.consume();
                MovieIndexer.selectTab(parent, "Add");
                am.id = id;
                am.searchLocally(name);
                am.addButton.setVisible(true);
                am.addButton.setText("Update");
                am.remButton.setVisible(true);
                am.choicebox.getSelectionModel().select(am.cbOptions.indexOf(name));
            }
        });
        return vbox;
    }

    private void removeFromFlowpane(String id) {
        int max = flow.getChildren().size();
        for (int i = 0; i < max; i++) {
            if (((Text) ((VBox) flow.getChildren().get(i)).getChildren().get(2)).getText().equals(id)) {
                flow.getChildren().remove(i);
                break;
            }
        }
    }

}
