package movieindexer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MovieIndexer extends Application {

    // List of tabs
    private TabPane tabs;

    // Another tab
    private AddMenu am;

    // something for maximizing.. ignore
    //private int retardedCounter = 0;
    TextField searchField;

    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        BooleanProperty readingFiles = new SimpleBooleanProperty(false);

        tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setFocusTraversable(false);

        // Add tab
        Tab addTab = new Tab("Add");
        am = new AddMenu(tabs);
        addTab.setContent(am);

        Task<ArrayList<Tab>> task = new Task<ArrayList<Tab>>() {
            @Override
            protected ArrayList<Tab> call() throws Exception {

                File[] listOfFiles = new File(".").listFiles();
                ArrayList<Tab> jsons = new ArrayList();
                double index = 0, maxIndex = 0;

                // get amount of files to read
                for (File f : listOfFiles) {
                    if (f.getName().endsWith(".json")) {
                        maxIndex++;
                    }
                }

                if (maxIndex == 0) {
                    // if no JSONs available, create one
                    if (!JsonManager.createEmptyJson("Movies")) {
                        return null;
                    }

                    ImdbList ml = new ImdbList(tabs, am, "Movies");
                    tabs.getTabs().add(ml);
                } else {
                    // read each json and add to tab
                    for (File f : listOfFiles) {
                        String fileName = f.getName();
                        if (fileName.endsWith(".json")) {

                            String realName = fileName.substring(0, fileName.length() - 5);
                            FirstPreloader.labelText = "Loading " + realName;
                            notifyPreloader(new Preloader.ProgressNotification(index / maxIndex));

                            ImdbList ml = new ImdbList(tabs, am, realName);
                            jsons.add(ml);
                            tabs.getTabs().add(ml);

                            index++;
                        }
                    }
                    notifyPreloader(new Preloader.ProgressNotification(index / maxIndex));
                }
                readingFiles.setValue(Boolean.TRUE);
                return jsons;
            }
        };
        new Thread(task).start();

        // Bottom buttons
        HBox hbButtons = new HBox();
        hbButtons.setPadding(new Insets(2));

        Button scrapeBtn = new Button();
        //scrapeBtn.setDisable(true);
        scrapeBtn.setText("Update All");
        scrapeBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                scrapeBtn.setDisable(true);



                final boolean [] gotCancelled = {false};
                ProgressForm pForm = new ProgressForm(gotCancelled);

                Task<Void> task = new Task<Void>() {


                    @Override
                    public Void call() throws InterruptedException {
                        ImdbList tab = (ImdbList) tabs.getSelectionModel().getSelectedItem();
                        String fName = tab.jsonName;
                        JSONArray list = JsonManager.readJson(fName);

                        String[] errors = {""};
                        int[] error_count = {0};
                        int[] i = {0};
                        for (i[0]=0; i[0] < list.length() && !gotCancelled[0]; i[0]++) {
                            try {
                                String[] movieInfo = am.getInfoFromMovieDB(list.getJSONObject(i[0]).getString("id"), false);
                                if (movieInfo[7] == null) throw new Exception("Got null reading!");
                                movieInfo[6] = list.getJSONObject(i[0]).getString("orderTitle").toLowerCase().replaceAll("[^a-z0-9 ]", "");
                                System.out.println("Updating " + movieInfo[0]);
                                File photo = new File(movieInfo[7] + ".jpg");
                                File newPhoto = new File("temp.temp");
                                if (newPhoto.exists()) {
                                    if (photo.exists())
                                        photo.delete();
                                    newPhoto.renameTo(photo);
                                }
                                Platform.runLater(() -> {
                                    tab.addMovie(true, movieInfo);
                                });
                            } catch (Exception ex) {
                                errors[0] += list.getJSONObject(i[0]).getString("id") + " " + list.getJSONObject(i[0]).getString("title") + " " + ex.getMessage() + '\n';
                                error_count[0]++;
                                ex.printStackTrace();
                            }
                            Platform.runLater(() -> {
                                pForm.label.setText("Updated " + i[0] + "/" + list.length() + "\n"+error_count[0]+" errors.");
                                updateProgress(i[0], list.length());
                            });
                        }
                        Platform.runLater(() -> {
                            updateProgress(list.length(), list.length());
                        });

                        System.out.println("Updated " + i[0] + "/" + list.length() + "\n"+error_count[0]+" errors on:\n" + errors[0]);
                        return null;
                    }
                };

                // binds progress of progress bars to progress of task:
                pForm.activateProgressBar(task);

                // in real life this method would get the result of the task
                // and update the UI based on its value:


                task.setOnSucceeded(event2 -> {
                    pForm.getDialogStage().close();
                    scrapeBtn.setDisable(false);
                });

                scrapeBtn.setDisable(true);
                pForm.getDialogStage().show();

                Thread thread = new Thread(task);
                thread.start();
                //--

                //System.out.println("Updated " + (list.length() - error_count) + "/" + list.length() + "\nErrors on:\n" + errors);
            }
        });
        hbButtons.getChildren().add(scrapeBtn);

        // bottom search field
        searchField = new TextField();
        searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                                        @Override
                                        public void handle(KeyEvent keyEvent) {
                                            if (keyEvent.getCode() != KeyCode.ENTER) {
                                                return;
                                            }
                                            filterButton();
                                        }

                                    }
        );
        hbButtons.getChildren().add(searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // root
        BorderPane root = new BorderPane();
        root.setCenter(tabs);
        root.setBottom(hbButtons);
        Scene scene = new Scene(root);

        // Final setup
        primaryStage.setScene(scene);
        primaryStage.setTitle("MovieIndexer");
        primaryStage.setMaximized(true);

        // Resize listener
        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                for (Tab tab : tabs.getTabs()) {
                    if (tab instanceof ImdbList) {
                        ((ImdbList) tab).flow.setPrefWrapLength(t1.intValue() - 24);
                    }
                }
            }
        });

        // Maximization listener
        primaryStage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (!ov.getValue()) {
                    primaryStage.setWidth(856);
                    primaryStage.setHeight(600);
                }
            }
        });

        // show scene when all's done
        readingFiles.addListener(new ChangeListener<Boolean>() {
            public void changed(
                    ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (Boolean.TRUE.equals(t1)) {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            // Tabs
                            am.updateList();
                            tabs.getTabs().add(addTab);
                            primaryStage.show();
                            notifyPreloader(new Preloader.StateChangeNotification(Type.BEFORE_START));

                        }
                    });
                }
            }
        });
        ;

    }

    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);

        launch(args);
    }

    // Search function
    public void filterButton() {
        String text = searchField.getText();
        String fName = ((ImdbList) tabs.getSelectionModel().getSelectedItem()).jsonName;
        JSONArray list = JsonManager.readJson(fName);


        ResultsList results = new ResultsList(tabs, am, fName);
        results.flow.setPrefWidth(tabs.getWidth() - 24);

        for (Object curr : list) {
            JSONObject curr2 = (JSONObject) curr;
            boolean add = true;
            for (String t2 : text.split(" ")) {
                String t = t2.toLowerCase();
                if (!curr2.getString("id").toLowerCase().contains(t)
                        && !curr2.getString("title").toLowerCase().contains(t)
                        && !curr2.getString("year").toLowerCase().contains(t)
                        && !curr2.getString("director").toLowerCase().contains(t)
                        && !curr2.getString("genre").toLowerCase().contains(t)
                        && !curr2.getString("actors").toLowerCase().contains(t)
                        && !curr2.getString("orderTitle").toLowerCase().contains(t)) {
                    add = false;
                    break;
                }
            }

            if (!add) {
                continue;
            }

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

            results.flow.getChildren().add(results.flow.getChildren().size(), am.configureVBox(title2, imgView2, idRef, am, fName));
        }

        // remove previous results tab
        Tab lastTab = tabs.getTabs().get(tabs.getTabs().size() - 1);
        if (tabs.getTabs().get(tabs.getTabs().size() - 1) instanceof ResultsList) {
            tabs.getTabs().remove(lastTab);
        }

        // add new results tab
        tabs.getTabs().add(results);
        tabs.getSelectionModel().selectLast();
    }

    public static int getIndexByName(TabPane tabs, String name) {
        ObservableList<Tab> l = tabs.getTabs();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getText().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static Tab getTabByName(TabPane tabs, String name) {
        ObservableList<Tab> l = tabs.getTabs();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getText().equals(name)) {
                return l.get(i);
            }
        }
        return null;
    }

    public static void selectTab(TabPane tabs, String name) {
        Tab tab = getTabByName(tabs, name);
        tabs.getSelectionModel().select(tab);
    }
}
