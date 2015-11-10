/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movieindexer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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

    TabPane tabs;
    ChoiceBox choicebox;
    ObservableList<String> cbOptions;
    ArrayList<FlowPane> fp;

    public AddMenu(TabPane t) {
        super();
        this.tabs = t;
        this.fp = new ArrayList();

        this.setPadding(new Insets(20, 20, 20, 20));
        this.getChildren().add(createSearchGrid());
        this.getChildren().add(createInfoGrid());
        local = false;

        configureSearchButton();
        configureAddButton();
        configureRemButton();
        configureCancelButton();
        configureListButtons(this);
    }

    private void addNewTab(String t, AddMenu am) {
        if (!JsonManager.createEmptyJson(t)) {
            return;
        }

        cbOptions.add(t);

        Tab newTab = new Tab();
        newTab.setText(t);
        ImdbList ml = new ImdbList(tabs, am, t);
        newTab.setContent(ml);
        //mll.add(ml);
        tabs.getTabs().add(newTab);
    }

    public void updateList() {
        //tabNames = new ArrayList();
        for (Tab tab : tabs.getTabs()) {
            cbOptions.add(tab.getText());
        }

        if (cbOptions.size() == 0) {
            addNewTab("Movies", this);
        }
        //System.out.println("lol "+tabNames.size()+"   "+tabs);
        //choicebox = new ChoiceBox(FXCollections.observableArrayList(tabNames));

        choicebox.getSelectionModel().select(0);

        if (cbOptions.size() == 1) {
            remButton_list.setDisable(true);
        }
    }

    private void removeFromFlowpane(String id, FlowPane fp) {
        int max = fp.getChildren().size();
        for (int i = 0; i < max; i++) {
            if (((Text) ((VBox) fp.getChildren().get(i)).getChildren().get(2)).getText().equals(id)) {
                fp.getChildren().remove(i);
                break;
            }
        }
    }

    private void insertInFlowpane(int pos, String title, String year, String id1, String listName, FlowPane fp) {
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
                tabs.getSelectionModel().select(am.getIndexByName("Add"));
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
        JSONArray list;

        try (Scanner fin = new Scanner(new File(fName + ".json")).useDelimiter("\\Z")) {
            String content = fin.next();
            list = new JSONObject(content).getJSONArray("movies");
        } catch (Exception ex) {
            System.out.println(".json file not found.");
            list = new JSONArray();
        }

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

    public int getIndexByName(String n) {
        ObservableList<Tab> l = tabs.getTabs();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getText().equals(n)) {
                return i;
            }
        }
        return l.size() - 1;

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

    public final void configureSearchButton() {
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                id = idField.getText();
                if (searchLocally(choicebox.getSelectionModel().getSelectedItem().toString())) {
                    addButton.setVisible(true);
                    addButton.setText("Update");
                    remButton.setVisible(true);
                    local = true;
                    return;
                }
                local = false;
                String html = Consumer.getHTML("http://www.omdbapi.com/?i=" + idField.getText() + "&plot=short&r=json");
                JSONObject obj = new JSONObject(html);

                if (obj.getString("Response").equals("True")) {
                    id = obj.getString("imdbID");
                    title.setText(obj.getString("Title"));
                    if (obj.getString("Title").toLowerCase().startsWith("the ")) {
                        orderTitle.setText(obj.getString("Title").substring(4).toLowerCase());
                    } else {
                        orderTitle.setText(obj.getString("Title").toLowerCase());
                    }
                    String tempYear = obj.getString("Year").replaceAll("\\D+","");;
                    if(tempYear.length()>4)
                        tempYear=tempYear.substring(0, 4)+"-"+tempYear.substring(tempYear.length()-4, tempYear.length());
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

                    if (!obj.getString("Poster").equals("N/A")) {
                        try {
                            URL imageUrl = new URL(obj.getString("Poster"));
                            try (InputStream imageReader = new BufferedInputStream(
                                    imageUrl.openStream());
                                    OutputStream imageWriter = new BufferedOutputStream(
                                            new FileOutputStream(new File("").getAbsolutePath() + File.separator + "temp.temp"));) {
                                int readByte;
                                while ((readByte = imageReader.read()) != -1) {
                                    imageWriter.write(readByte);
                                }
                            }

                            Image img = new Image("file:temp.temp");
                            imgView.setImage(img);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        try {
                            Files.copy(Paths.get(new File("").getAbsolutePath() + File.separator + "notFound.jpg"),
                                    Paths.get(new File("").getAbsolutePath() + File.separator + "temp.temp"), REPLACE_EXISTING);
                            Image img = new Image("file:temp.temp");
                            imgView.setImage(img);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                    addButton.setText("Add");
                    addButton.setVisible(true);
                    remButton.setVisible(false);
                } else {
                    id = null;
                    addButton.setVisible(false);
                    remButton.setVisible(false);
                }
            }
        });
    }

    public final void configureAddButton() {
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                JSONArray list = JsonManager.readJson(choicebox.getSelectionModel().getSelectedItem().toString());

                JSONObject curr = new JSONObject();
                curr.put("title", title.getText().replaceAll("\"", ""));
                curr.put("year", year.getText());
                curr.put("director", director.getText());
                curr.put("genre", genre.getText().replaceAll("\"", ""));
                curr.put("actors", actors.getText().replaceAll("\"", ""));
                curr.put("score", score.getText());
                curr.put("orderTitle", orderTitle.getText());
                curr.put("id", id);

                int i = 0;

                // Remove from JSON if local
                if (local) {
                    while (i < list.length()) {
                        if (((JSONObject) list.get(i)).getString("id").equals(id)) {
                            list.remove(i);
                            break;
                        }
                        i++;
                    }
                }

                // Insert into JSON 
                i = 0;
                if (list.length() > 0) {
                    while (i < list.length() && ((JSONObject) list.get(i)).getString("orderTitle").compareTo(orderTitle.getText()) < 0) {
                        i++;
                    }

                    for (int j = list.length(); j > i; j--) {
                        list.put(j, list.getJSONObject(j - 1));
                    }
                }
                list.put(i, curr);

                JsonManager.writeJson(choicebox.getSelectionModel().getSelectedItem().toString(), list);

                // Remove from pane (if local)
                if (local) {
                    removeFromFlowpane(id, fp.get(choicebox.getSelectionModel().getSelectedIndex()));
                }

                if (!local) {
                    File photo = new File(id + ".jpg");
                    if (photo.exists()) {
                        //photo.delete();
                    } else {
                        new File("temp.temp").renameTo(photo);
                    }
                }

                // Add into pane
                insertInFlowpane(i, title.getText(), year.getText(), id, choicebox.getSelectionModel().getSelectedItem().toString(), fp.get(choicebox.getSelectionModel().getSelectedIndex()));

                // Reset GUI
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
                tabs.getSelectionModel().select(getIndexByName(choicebox.getSelectionModel().getSelectedItem().toString()));
            }
        });
    }

    public final void configureRemButton() {
        remButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                JSONArray list = JsonManager.readJson(choicebox.getSelectionModel().getSelectedItem().toString());

                for (int i = 0; i < list.length(); i++) {
                    JSONObject curr = list.getJSONObject(i);
                    if (curr.getString("id").equals(id)) {
                        list.remove(i);
                    }
                }

                JsonManager.writeJson(choicebox.getSelectionModel().getSelectedItem().toString(), list);

                title.setText("");
                year.setText("");
                director.setText("");
                genre.setText("");
                actors.setText("");
                score.setText("");
                orderTitle.setText("");
                plot.setText("");

                int counter = 0;
                for (String tab : cbOptions) {
                    if (searchLocally(tab)) {
                        counter++;
                    }
                }
                if (counter == 0) {
                    File photo = new File(id + ".jpg");
                    if (photo.exists()) {
                        photo.delete();
                    }
                }

                imgView.setImage(null);
                addButton.setText("Add");
                addButton.setVisible(false);
                remButton.setVisible(false);

                removeFromFlowpane(id, fp.get(choicebox.getSelectionModel().getSelectedIndex()));

                id = null;
                tabs.getSelectionModel().select(getIndexByName(choicebox.getSelectionModel().getSelectedItem().toString()));
            }
        });
    }

    public final void configureCancelButton() {
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                title.setText("");
                year.setText("");
                director.setText("");
                genre.setText("");
                actors.setText("");
                score.setText("");
                orderTitle.setText("");
                plot.setText("");

                imgView.setImage(null);
                addButton.setText("Add");
                addButton.setVisible(false);
                remButton.setVisible(false);

                id = null;
                tabs.getSelectionModel().select(getIndexByName(choicebox.getSelectionModel().getSelectedItem().toString()));
            }
        });
    }

    public final void configureListButtons(AddMenu am) {
        addButton_list.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addNewTab(listField.getText(), am);
            }
        });

        remButton_list.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                JsonManager.removeJson(choicebox.getSelectionModel().getSelectedItem().toString());

                tabs.getTabs().remove(getIndexByName(choicebox.getSelectionModel().getSelectedItem().toString()));
                cbOptions.remove(choicebox.getSelectionModel().getSelectedItem().toString());
                choicebox.getSelectionModel().select(0);

                if (cbOptions.size() == 1) {
                    remButton_list.setDisable(true);
                }
            }
        });

        choicebox.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number val, Number newVal) {
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
