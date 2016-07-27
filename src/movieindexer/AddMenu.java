/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movieindexer;

import https.Consumer;
import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;

public class AddMenu extends VBox {

    String id;
    boolean local;

    TextField title, orderTitle, idField, listField;
    Text year, director, score, plot;
    ImageView imgView;
    TextArea genre, actors;

    Button searchButton, addButton, remButton, cancelButton;
    Button addButton_list, remButton_list;
    Button imdbLink, torrentLink;
    String magnetLink = "";

    WebView webview;

    TabPane tabs;
    ChoiceBox choicebox;
    ObservableList<String> cbOptions;
    ArrayList<FlowPane> fp;

    public AddMenu(TabPane t) {
        super();
        this.tabs = t;
        this.fp = new ArrayList();

        this.setPadding(new Insets(20, 20, 20, 20));
        GridPane mainGrid = new GridPane();
        mainGrid.add(createSearchGrid(), 0, 0);
        mainGrid.add(createInfoGrid(), 0, 1);
        mainGrid.add(getTrailerTorrentTab(), 1, 1);
        this.getChildren().add(mainGrid);
        local = false;

        configureSearchButton();
        configureAddButton();
        configureRemButton();
        configureCancelButton();
        configureListButtons(this);
        configureImdbTorrentButtons();
    }

    private void configureImdbTorrentButtons() {
        imdbLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI("http://www.imdb.com/title/" + id + "/"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        torrentLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI(magnetLink));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private GridPane getTrailerTorrentTab() {
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 10, 10, 10));

        imdbLink = new Button("IMDB");
        imdbLink.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        imdbLink.setVisible(false);
        infoGrid.add(imdbLink, 0, 0);

        webview = new WebView();
        webview.setPrefSize(512, 312);
        infoGrid.add(webview, 0, 1);
        webview.setVisible(false);

        torrentLink = new Button("Torrent");
        torrentLink.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        torrentLink.setVisible(false);
        infoGrid.add(torrentLink, 0, 2);

        return infoGrid;
    }

    private void addNewTab(String t, AddMenu am, boolean startOfList) {
        if (!JsonManager.createEmptyJson(t)) {
            return;
        }

        cbOptions.add(t);

        ImdbList ml = new ImdbList(tabs, am, t);
        if (startOfList || tabs.getTabs().size() == 0) {
            tabs.getTabs().add(ml);
        } else {
            tabs.getTabs().add(tabs.getTabs().size() - 1, ml);
        }
    }

    public void updateList() {
        for (Tab tab : tabs.getTabs()) {
            if (tab instanceof ImdbList) {
                cbOptions.add(tab.getText());
            }
        }

        if (cbOptions.size() == 0) {
            addNewTab("Movies", this, true);
        }

        choicebox.getSelectionModel().select(0);

        if (cbOptions.size() == 1) {
            remButton_list.setDisable(true);
        }
    }

    public void insertInFlowpane(int pos, String title, String year, String id1, String listName, FlowPane fp) {
        Text title2 = new Text(title + " (" + year + ")");
        title2.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title2.setWrappingWidth(200);
        title2.textAlignmentProperty().set(TextAlignment.CENTER);

        Image img = new Image("file:" + id + ".jpg");
        ImageView imgView2 = new ImageView(img);
        imgView2.setFitWidth(200);
        imgView2.setFitHeight(300);
        imgView2.setPreserveRatio(true);

        Text idRef = new Text(id1);
        idRef.setVisible(false);

        fp.getChildren().add(pos, configureVBox(title2, imgView2, idRef, this, listName));
    }

    public VBox configureVBox(Text title, ImageView imgView, Text idRef, AddMenu am, String name) {
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
                MovieIndexer.selectTab(tabs, "Add");

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

    public boolean searchLocally(String fName) {
        /*JSONArray list;

         try (Scanner fin = new Scanner(new File(fName + ".json")).useDelimiter("\\Z")) {
         String content = fin.next();
         list = new JSONObject(content).getJSONArray("movies");
         } catch (Exception ex) {
         System.out.println(".json file not found.");
         list = new JSONArray();
         }*/
        JSONArray list = JsonManager.readJson(fName);

        for (int i = 0; i < list.length(); i++) {
            JSONObject curr = list.getJSONObject(i);
            if (curr.getString("id").equals(id)) {
                title.setText(curr.getString("title"));
                year.setText(curr.getString("year"));
                director.setText(curr.getString("director"));
                genre.setText(curr.getString("genre"));
                actors.setText(curr.getString("actors"));
                score.setText(curr.getString("score"));
                orderTitle.setText(curr.getString("orderTitle"));
                plot.setText("ID: " + id);
                imgView.setImage(new Image("file:" + id + ".jpg"));
                local = true;
                return true;
            }
        }
        local = false;
        return false;
    }

    public final GridPane createInfoGrid() {
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 10, 10, 10));

        title = new TextField();
        infoGrid.add(title, 0, 1);
        year = new Text();
        year.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        infoGrid.add(year, 1, 1);
        director = new Text();
        director.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        infoGrid.add(director, 2, 1);

        genre = new TextArea();
        genre.setPrefSize(300, 100);
        infoGrid.add(genre, 0, 2);
        actors = new TextArea();
        actors.setPrefSize(200, 100);
        infoGrid.add(actors, 1, 2);
        score = new Text();
        score.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        infoGrid.add(score, 2, 2);

        plot = new Text();
        plot.setWrappingWidth(300);

        infoGrid.add(plot, 0, 3);

        imgView = new ImageView();
        imgView.setFitWidth(200);
        imgView.setFitHeight(300);
        imgView.setPreserveRatio(true);
        infoGrid.add(imgView, 1, 3);
        orderTitle = new TextField();
        infoGrid.add(orderTitle, 2, 3);

        addButton = new Button("Add");
        addButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        addButton.setVisible(false);
        infoGrid.add(addButton, 0, 4);

        remButton = new Button("Remove");
        remButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        remButton.setVisible(false);
        infoGrid.add(remButton, 0, 5);

        cancelButton = new Button("Cancel");
        cancelButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cancelButton.setVisible(true);
        infoGrid.add(cancelButton, 0, 6);

        cbOptions = FXCollections.observableArrayList();
        choicebox = new ChoiceBox(cbOptions);
        infoGrid.add(choicebox, 2, 4);

        remButton_list = new Button("Remove List");
        remButton_list.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        remButton_list.setVisible(true);
        infoGrid.add(remButton_list, 2, 5);

        listField = new TextField();
        infoGrid.add(listField, 1, 4);

        addButton_list = new Button("Add List");
        addButton_list.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        addButton_list.setVisible(true);
        infoGrid.add(addButton_list, 1, 5);

        return infoGrid;
    }

    public final GridPane createSearchGrid() {
        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);
        searchGrid.setPadding(new Insets(10, 10, 10, 10));

        // Title in column 1, row 1
        Text category = new Text("ID:");
        category.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        searchGrid.add(category, 0, 0);

        // Title in column 2, row 1
        idField = new TextField();
        searchGrid.add(idField, 1, 0);

        searchButton = new Button("Search");
        searchGrid.add(searchButton, 2, 0);

        return searchGrid;
    }

    private Thread searchThread() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                Platform.runLater(() -> {
                    searchButton.setDisable(true);
                    searchButton.setText("Searching...");
                });
                id = idField.getText();
                if (searchLocally(choicebox.getSelectionModel().getSelectedItem().toString())) {
                    Platform.runLater(() -> {
                        addButton.setVisible(true);
                        addButton.setText("Update");
                        remButton.setVisible(true);
                        local = true;

                        searchButton.setDisable(false);
                        searchButton.setText("Search");
                    });
                    return -1;
                }
                local = false;
                String html = Consumer.getJSON("http://www.omdbapi.com/?i=" + idField.getText() + "&plot=short&r=json");
                JSONObject obj = new JSONObject(html);

                if (!html.equals("{}") && "True".equals(obj.getString("Response"))) {
                    id = obj.getString("imdbID");
                    Platform.runLater(() -> {
                        title.setText(obj.getString("Title"));
                        if (obj.getString("Title").toLowerCase().startsWith("the ")) {
                            orderTitle.setText(obj.getString("Title").substring(4).toLowerCase());
                        } else {
                            orderTitle.setText(obj.getString("Title").toLowerCase());
                        }
                        String tempYear = obj.getString("Year").replaceAll("\\D+", "");;
                        if (tempYear.length() > 4) {
                            tempYear = tempYear.substring(0, 4) + "-" + tempYear.substring(tempYear.length() - 4, tempYear.length());
                        }
                        year.setText(tempYear);
                        if (obj.getString("imdbRating").equals("N/A") || obj.getString("Metascore").equals("N/A")) {
                            score.setText("N/A\nN/A");
                        } else {
                            score.setText(obj.getString("imdbRating") + "/10\n" + obj.getString("Metascore") + "/100");
                        }
                        genre.setText(obj.getString("Genre").replaceAll(", ", "\n"));
                        actors.setText(obj.getString("Actors").replaceAll(", ", "\n"));
                        plot.setText(obj.getString("Plot"));
                        director.setText(obj.getString("Director"));
                    });
                    if (obj.getString("Poster").equals("N/A") || !Consumer.getImage(obj.getString("Poster"))) {
                        try {
                            Files.copy(Paths.get(new File("").getAbsolutePath() + File.separator + "notFound.jpg"),
                                    Paths.get(new File("").getAbsolutePath() + File.separator + "temp.temp"), REPLACE_EXISTING);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    Platform.runLater(() -> {
                        imgView.setImage(new Image("file:temp.temp"));

                        addButton.setText("Add");
                        addButton.setVisible(true);
                        remButton.setVisible(false);

                        imdbLink.setVisible(true);
                        torrentLink.setVisible(true);
                        webview.setVisible(true);
                        //webview.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                        webview.getEngine().load("https://www.youtube.com/embed/"
                                + Consumer.getYoutubeID(title.getText(), year.getText()));
                        magnetLink = Consumer.getMagnetLink(title.getText(), year.getText());
                        if (magnetLink == null) {
                            torrentLink.setVisible(false);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        imdbLink.setVisible(false);
                        webview.setVisible(false);
                        torrentLink.setVisible(false);
                        addButton.setVisible(false);
                        remButton.setVisible(false);
                    });
                    id = null;
                }
                Platform.runLater(() -> {
                    searchButton.setDisable(false);
                    searchButton.setText("Search");
                });

                return 0;
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);

        return th;
    }

    public final void configureSearchButton() {
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                searchThread().start();
            }
        });
    }

    public final void configureAddButton() {
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String listName = choicebox.getSelectionModel().getSelectedItem().toString();
                ImdbList tab = ((ImdbList) MovieIndexer.getTabByName(tabs, listName));
                int[] indexes = tab.addMovie(local, new String[]{
                    title.getText().replaceAll("\"", ""), year.getText(), director.getText(), genre.getText().replaceAll("\"", ""),
                    actors.getText().replaceAll("\"", ""), score.getText(), orderTitle.getText(), id});

                // Reset GUI
                clearMenu();

                tabs.getSelectionModel().select(tab);
                int moviesPerRow = (int) (tabs.getWidth() - 24) / (200 + 4);
                ((ImdbList) tabs.getSelectionModel().getSelectedItem()).scroller.setVvalue(
                        ((indexes[0] / moviesPerRow)) / ((Math.ceil(indexes[1] * 1.0 / moviesPerRow)) - 1));

            }
        });
    }

    public final void configureRemButton() {
        remButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String listName = choicebox.getSelectionModel().getSelectedItem().toString();
                ImdbList tab = ((ImdbList) MovieIndexer.getTabByName(tabs, listName));
                tab.removeMovie(id);

                // only delete image if its not used anywhere else
                int counter = 0;
                for (String tabName : cbOptions) {
                    if (searchLocally(tabName)) {
                        counter++;
                        break;
                    }
                }
                if (counter == 0) {
                    File photo = new File(id + ".jpg");
                    if (photo.exists()) {
                        photo.delete();
                    }
                }

                clearMenu();
                tabs.getSelectionModel().select(tab);
            }
        });
    }

    public void clearMenu() {
        title.setText("");
        year.setText("");
        director.setText("");
        genre.setText("");
        actors.setText("");
        score.setText("");
        orderTitle.setText("");
        plot.setText("");

        id = null;
        imgView.setImage(null);
        addButton.setText("Add");
        addButton.setVisible(false);
        remButton.setVisible(false);

        imdbLink.setVisible(false);
        webview.setVisible(false);
    }

    public final void configureCancelButton() {
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clearMenu();
                MovieIndexer.selectTab(tabs, choicebox.getSelectionModel().getSelectedItem().toString());
            }
        });
    }

    public final void configureListButtons(AddMenu am) {
        addButton_list.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addNewTab(listField.getText(), am, false);
                listField.clear();
            }
        });

        remButton_list.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText("Deleting a list is permanent!");
                alert.setContentText("Are you sure?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    // ... user chose OK
                    JsonManager.removeJson(choicebox.getSelectionModel().getSelectedItem().toString());

                    String listName = choicebox.getSelectionModel().getSelectedItem().toString();
                    tabs.getTabs().remove(MovieIndexer.getTabByName(tabs, listName));
                    cbOptions.remove(listName);
                    choicebox.getSelectionModel().select(0);

                    if (cbOptions.size() == 1) {
                        remButton_list.setDisable(true);
                    }
                } else {
                    // ... user chose CANCEL or closed the dialog
                    // do nothing
                }

            }
        });

        choicebox.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number val, Number newVal) {
                        if (newVal.intValue() < 0) {
                            return;
                        }
                        if (addButton.visibleProperty().get()) {
                            if (searchLocally(cbOptions.get(newVal.intValue()))) {
                                addButton.setVisible(true);
                                addButton.setText("Update");
                                remButton.setVisible(true);
                                local = true;
                            } else {
                                addButton.setVisible(true);
                                addButton.setText("Add");
                                remButton.setVisible(false);
                                local = false;
                            }
                        }
                    }
                });
    }

}
