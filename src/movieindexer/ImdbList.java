/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package movieindexer;

import java.io.File;
import java.util.Scanner;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ScrollPane;
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
public class ImdbList extends ScrollPane{
    FlowPane flow;
    TabPane tabs;
    //AddMenu am;
    String name;
    
    public ImdbList(TabPane t, AddMenu am, String name){
        super();
        this.tabs=t;
        //this.am=am;
        this.name=name;
        
        this.flow = new FlowPane();
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setAlignment(Pos.TOP_CENTER);
        am.fp.add(flow);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        flow.setPrefWrapLength(bounds.getMaxX() - 10);

        JSONArray list;

        try (Scanner fin = new Scanner(new File(name+".json")).useDelimiter("\\Z")) {
            String content = fin.next();
            list = new JSONObject(content).getJSONArray("movies");

            for (int i = 0; i < list.length(); i++) {
                Text title = new Text(list.getJSONObject(i).getString("title")+ " ("+list.getJSONObject(i).getString("year")+")");
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
                
                /*VBox vbox = new VBox();
                vbox.setPrefSize(200, 328);
                vbox.setAlignment(Pos.BOTTOM_CENTER);
                vbox.getChildren().add(title);
                vbox.getChildren().add(imgView);
                vbox.getChildren().add(idRef);
                
                vbox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        String id = ((Text)vbox.getChildren().get(2)).getText();
                        event.consume();
                        tabs.getSelectionModel().select(am.getIndexByName("Add"));
                        am.id=id;
                        am.searchLocally(name);
                        am.addButton.setVisible(true);
                        am.addButton.setText("Update");
                        am.remButton.setVisible(true);
                        am.choicebox.getSelectionModel().select(am.cbOptions.indexOf(name));
                    }
                });*/

                flow.getChildren().add(am.configureVBox(title, imgView, idRef, am, name));
            }
        } catch (Exception ex) {
            System.out.println(".json file not found.");
        }

        
        this.setContent(flow);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    }
}
