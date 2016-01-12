package movieindexer;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

public class MovieIndexer extends Application {

    // List of tabs
    private TabPane tabs;

    // Each particular tab
    private ArrayList<ImdbList> mll;

    // Another tab
    private AddMenu am;

    // something for maximizing.. ignore
    private int retardedCounter = 0;

    @Override
    public void start(Stage primaryStage) {

        tabs = new TabPane();
        mll = new ArrayList();

        // Add tab
        Tab tabAlign = new Tab();
        tabAlign.setText("Add");
        am = new AddMenu(tabs);
        tabAlign.setContent(am);

        File[] listOfFiles = new File(".").listFiles();
        ArrayList<Tab> jsons = new ArrayList();
        for (File f : listOfFiles) {
            String fileName = f.getName();
            if (fileName.endsWith(".json")) {
                Tab tabMovies = new Tab();
                String realName = fileName.substring(0, fileName.length() - 5);
                tabMovies.setText(realName);
                ImdbList ml = new ImdbList(tabs, am, realName);
                tabMovies.setContent(ml);
                jsons.add(tabMovies);
                mll.add(ml);
            }
        }

        // Tabs
        tabs.getTabs().addAll(jsons);
        am.updateList();
        tabs.getTabs().add(tabAlign);

        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // bottom respectively "button area"
        HBox hbButtons = new HBox();
        hbButtons.setPadding(new Insets(2));

        Button scrapeBtn = new Button();
        scrapeBtn.setText("Update All");
        scrapeBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Scrape button pressed.");
            }
        });
        hbButtons.getChildren().add(scrapeBtn);

        TextField searchField = new TextField();
        searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String text = searchField.getText();
                    String fName = mll.get(tabs.getSelectionModel().getSelectedIndex()).name;

                    JSONArray list;
                    try (Scanner fin = new Scanner(new File(fName + ".json")).useDelimiter("\\Z")) {
                        String content = fin.next();
                        list = new JSONObject(content).getJSONArray("movies");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println(fName+".json file not found.");
                        return;
                    }

                    ImdbList results = new ImdbList(tabs, am, "Results");

                    for (Object curr : list) {
                        JSONObject curr2 = (JSONObject) curr;
                        for (String t2 : text.split(" ")) {
                            String t=t2.toLowerCase();
                            if (curr2.getString("id").toLowerCase().contains(t)
                                    || curr2.getString("title").toLowerCase().contains(t)
                                    || curr2.getString("year").toLowerCase().contains(t)
                                    || curr2.getString("director").toLowerCase().contains(t)
                                    || curr2.getString("genre").toLowerCase().contains(t)
                                    || curr2.getString("actors").toLowerCase().contains(t)
                                    || curr2.getString("orderTitle").toLowerCase().contains(t)) {

                                Text title2 = new Text(curr2.getString("title") + " (" + curr2.getString("year") + ")");
                                title2.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                                title2.setWrappingWidth(200);
                                title2.textAlignmentProperty().set(TextAlignment.CENTER);

                                Image img = new Image("file:" + curr2.getString("id") + ".jpg");
                                ImageView imgView2 = new ImageView(img);
                                imgView2.setFitWidth(200);
                                imgView2.setFitHeight(300);
                                imgView2.setPreserveRatio(true);

                                Text idRef = new Text(curr2.getString("id"));
                                idRef.setVisible(false);

                                results.flow.getChildren().add(results.flow.getChildren().size(), am.configureVBox(title2, imgView2, idRef, am, ""));
                            }
                        }
                    }

                    Tab resultsTab = new Tab();
                    resultsTab.setText(fName);
                    resultsTab.setContent(results);
                    tabs.getTabs().add(resultsTab);
                }
            }
        }
        );
        hbButtons.getChildren().add(searchField);

        // root
        BorderPane root = new BorderPane();
        root.setCenter(tabs);
        root.setBottom(hbButtons);

        Scene scene = new Scene(root);

        // Final setup
        primaryStage.setScene(scene);
        primaryStage.setTitle("MovieIndexer");
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Resize listener
        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                if (retardedCounter != 1 && retardedCounter != 2) {
                    for (ImdbList ml : mll) {
                        ml.flow.setPrefWrapLength(tabs.getWidth() - 24);
                    }
                }
                retardedCounter++;
            }
        });

        // Maximize listener
        primaryStage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (!t1) {
                    tabs.resize(856, 600);
                    primaryStage.setWidth(856);
                    primaryStage.setHeight(600);
                    primaryStage.setX(10);
                    primaryStage.setY(10);
                } else {
                    Screen screen = Screen.getPrimary();
                    Rectangle2D bounds = screen.getVisualBounds();
                    tabs.resize(bounds.getMaxX(), bounds.getMaxY());
                }
            }
        });

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
