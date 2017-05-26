/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movieindexer;

import https.Consumer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Optional;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.json.JSONArray;
import org.json.JSONObject;

public class AddMenu extends VBox {

    String currentMovieID;
    boolean isCurrentMovieLocal;
    TextField movieTitleField, movieOrderTitleField, movieYearField, movieDirectorField;
    ImageView movieImgView;
    TextArea movieGenreArea, movieActorsArea, movieScoreArea, moviePlotArea;
    Button movieAddButton, movieRemButton, movieCancelButton;

    TextField searchIDField;
    Button searchButton;

    TextField listNameField;
    ChoiceBox listChoiceBox;
    ObservableList<String> listChoiceBoxOptions;
    Button listAddButton, listRemButton;
    //Button imdbLink, torrentLink;
    //String magnetLink = "";
    //WebView webview;

    TabPane tabPane;

    public AddMenu(TabPane t) {
        super();
        this.tabPane = t;

        this.setPadding(new Insets(20, 20, 20, 20));
        GridPane mainGrid = new GridPane();
        mainGrid.add(createSearchGrid(), 0, 0);
        mainGrid.add(createInfoGrid(), 0, 1);
        mainGrid.add(createListGrid(), 0, 2);
        //mainGrid.add(getTrailerTorrentTab(), 1, 1);
        this.getChildren().add(mainGrid);
        isCurrentMovieLocal = false;

        configureSearchButton();
        configureAddButton();
        configureRemButton();
        configureCancelButton();
        configureListButtons(this);
        //configureImdbTorrentButtons();
    }

    /*
     // configure the IMDB and Torrent buttons
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
     */
    // adds new tab <am> named <t> either to 'start' or to 'before last' positions
    private void addNewTab(String t, AddMenu am, boolean startOfList) {
        if (!JsonManager.createEmptyJson(t)) {
            return;
        }

        listChoiceBoxOptions.add(t);

        ImdbList ml = new ImdbList(tabPane, am, t);
        if (startOfList || tabPane.getTabs().size() == 0) {
            tabPane.getTabs().add(ml);
        } else {
            tabPane.getTabs().add(tabPane.getTabs().size() - 1, ml);
        }
    }

    public void updateList() {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof ImdbList) {
                listChoiceBoxOptions.add(tab.getText());
            }
        }

        /*if (cbOptions.size() == 0) {
         addNewTab("Movies", this, true);
         }*/
        listChoiceBox.getSelectionModel().select(0);

        if (listChoiceBoxOptions.size() == 1) {
            listRemButton.setDisable(true);
        }
    }

    public void insertInFlowpane(int pos, String title, String year, String id1, String listName, FlowPane fp) {
        Text title2 = new Text(title + " (" + year + ")");
        title2.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title2.setWrappingWidth(200);
        title2.textAlignmentProperty().set(TextAlignment.CENTER);

        Image img = new Image("file:" + currentMovieID + ".jpg");
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
                MovieIndexer.selectTab(tabPane, "Add");

                am.currentMovieID = id;
                am.searchLocally(name);
                am.movieAddButton.setVisible(true);
                am.movieAddButton.setText("Update");
                am.movieRemButton.setVisible(true);
                am.listChoiceBox.getSelectionModel().select(am.listChoiceBoxOptions.indexOf(name));
            }
        });
        return vbox;
    }

    public boolean searchLocally(String fName) {
        JSONArray list = JsonManager.readJson(fName);

        for (int i = 0; i < list.length(); i++) {
            JSONObject curr = list.getJSONObject(i);
            if (curr.getString("id").equals(currentMovieID)) {
                movieTitleField.setText(curr.getString("title"));
                movieYearField.setText(curr.getString("year"));
                movieDirectorField.setText(curr.getString("director"));
                movieGenreArea.setText(curr.getString("genre"));
                movieActorsArea.setText(curr.getString("actors"));
                movieScoreArea.setText(curr.getString("score"));
                movieOrderTitleField.setText(curr.getString("orderTitle"));
                moviePlotArea.setText("ID: " + currentMovieID);
                moviePlotArea.setEditable(false);
                setInfoGridVisible(true);
                movieImgView.setImage(new Image("file:" + currentMovieID + ".jpg"));
                isCurrentMovieLocal = true;
                return true;
            }
        }
        isCurrentMovieLocal = false;
        return false;
    }

    public final GridPane createInfoGrid() {
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.getColumnConstraints().add(new ColumnConstraints(200)); // column 1 is 100 wide
        infoGrid.getColumnConstraints().add(new ColumnConstraints(200));
        infoGrid.getColumnConstraints().add(new ColumnConstraints(200));
        infoGrid.setPadding(new Insets(10, 10, 10, 10));

        movieTitleField = new TextField();
        infoGrid.add(movieTitleField, 0, 1);
        movieYearField = new TextField();
        infoGrid.add(movieYearField, 1, 1);
        movieDirectorField = new TextField();
        infoGrid.add(movieDirectorField, 2, 1);

        movieOrderTitleField = new TextField();
        infoGrid.add(movieOrderTitleField, 0, 2);
        movieGenreArea = new TextArea();
        movieGenreArea.setPrefSize(300, 100);
        movieGenreArea.setWrapText(true);
        infoGrid.add(movieGenreArea, 1, 2);
        movieScoreArea = new TextArea();
        infoGrid.add(movieScoreArea, 2, 2);

        moviePlotArea = new TextArea();
        moviePlotArea.setWrapText(true);
        moviePlotArea.setEditable(false);
        infoGrid.add(moviePlotArea, 0, 3);

        movieImgView = new ImageView();
        movieImgView.setFitWidth(200);
        movieImgView.setFitHeight(300);
        movieImgView.setPreserveRatio(true);
        infoGrid.add(movieImgView, 1, 3);
        movieActorsArea = new TextArea();
        movieActorsArea.setPrefSize(200, 100);
        movieActorsArea.setWrapText(true);
        infoGrid.add(movieActorsArea, 2, 3);

        setInfoGridVisible(false);

        movieAddButton = new Button("Add");
        movieAddButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        movieAddButton.setVisible(false);
        movieAddButton.setMaxWidth(Double.MAX_VALUE);
        infoGrid.add(movieAddButton, 0, 4);

        movieRemButton = new Button("Remove");
        movieRemButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        movieRemButton.setVisible(false);
        movieRemButton.setMaxWidth(Double.MAX_VALUE);
        infoGrid.add(movieRemButton, 1, 4);

        movieCancelButton = new Button("Cancel");
        movieCancelButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        movieCancelButton.setVisible(true);
        movieCancelButton.setMaxWidth(Double.MAX_VALUE);
        infoGrid.add(movieCancelButton, 2, 4);

        return infoGrid;
    }

    public final GridPane createListGrid() {
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        ColumnConstraints column0 = new ColumnConstraints(290);
        ColumnConstraints column1 = new ColumnConstraints(20);
        ColumnConstraints column2 = new ColumnConstraints(290);
        infoGrid.getColumnConstraints().addAll(column0, column1, column2);
        infoGrid.setPadding(new Insets(10, 10, 10, 10));

        listChoiceBoxOptions = FXCollections.observableArrayList();
        listChoiceBox = new ChoiceBox(listChoiceBoxOptions);
        listChoiceBox.setMaxWidth(Double.MAX_VALUE);
        infoGrid.add(listChoiceBox, 2, 0);

        listRemButton = new Button("Remove List");
        listRemButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        listRemButton.setVisible(true);
        listRemButton.setMaxWidth(Double.MAX_VALUE);
        infoGrid.add(listRemButton, 2, 1);

        listNameField = new TextField();
        listNameField.setMaxWidth(Double.MAX_VALUE);
        infoGrid.add(listNameField, 0, 0);

        listAddButton = new Button("Add List");
        listAddButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        listAddButton.setVisible(true);
        listAddButton.setMaxWidth(Double.MAX_VALUE);
        infoGrid.add(listAddButton, 0, 1);

        return infoGrid;
    }

    public final GridPane createSearchGrid() {
        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);
        ColumnConstraints column0 = new ColumnConstraints(50);
        ColumnConstraints column1 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints(100);
        searchGrid.getColumnConstraints().addAll(column0, column1, column2); // first column gets any extra width
        searchGrid.setPadding(new Insets(10, 10, 10, 10));

        // Title in column 1, row 1
        Text category = new Text("ID:");
        category.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        searchGrid.add(category, 0, 0);

        // Title in column 2, row 1
        searchIDField = new TextField();
        searchGrid.add(searchIDField, 1, 0);

        searchButton = new Button("Search");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchGrid.add(searchButton, 2, 0);

        return searchGrid;
    }

    private Thread searchUserRatingThread() {

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                // disable search button
                Platform.runLater(() -> {
                    searchButton.setDisable(true);
                    searchButton.setText("Searching...");
                });
                
                String html = Consumer.getJSON("https://api.import.io/store/connector/b11a1cf2-3efa-4cff-971f-52b939d7cba7/_query?input=webpage/url:"
                        + "http%3A%2F%2Fwww.imdb.com%2Fuser%2F" + searchIDField.getText() + "%2Fratings&&_apikey=4911226d82c84f2f9e06e04"
                        + "bffad812e3ee3dd322ae4d7309f3751ce6913e2da924f27aabd67c1a8d8aee488dc392b80c9de4885cd3d16c6d630151818526d3b"
                        + "5b50b3d57bcf816bfa015eeeb4989a1f");
                JSONObject obj = new JSONObject(html);
                
                System.out.println(html);
                // if found on IMDB, fill fields
                if (!html.equals("{}")) {
                    System.out.println("asd2");
                    JSONArray results = obj.getJSONArray("results");
                    String listName = listChoiceBox.getSelectionModel().getSelectedItem().toString();
                    ImdbList tab = ((ImdbList) MovieIndexer.getTabByName(tabPane, listName));

                    for (Object o : results) {
                        System.out.println("asd4");
                        JSONObject jsonO = (JSONObject) o;
                        String[] fields = jsonO.getString("info_link").split("/");
                        System.out.println(fields[fields.length - 1]);
                        searchIDField.setText(fields[fields.length - 1]);
                        searchMovieIdThread().start();
                        searchMovieIdThread().join();
                        System.out.println("asd5");
                        tab.addMovie(isCurrentMovieLocal, new String[]{
                            movieTitleField.getText().replaceAll("\"", ""), movieYearField.getText(), movieDirectorField.getText(), movieGenreArea.getText().replaceAll("\"", ""),
                            movieActorsArea.getText().replaceAll("\"", ""), movieScoreArea.getText(), movieOrderTitleField.getText(), currentMovieID});
                    }

                    // Reset GUI
                    clearMenu();

                    tabPane.getSelectionModel().select(tab);
                    ((ImdbList) tabPane.getSelectionModel().getSelectedItem()).scroller.setVvalue(0);
                } else {
                    System.out.println("asd3");
                    Platform.runLater(() -> {
                        movieAddButton.setVisible(false);
                        movieRemButton.setVisible(false);
                    });
                    currentMovieID = null;
                }
                System.out.println("asd4");
                // enable search button again
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

    private Thread searchMovieIdThread() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                // disable search button
                Platform.runLater(() -> {
                    searchButton.setDisable(true);
                    searchButton.setText("Searching...");
                });

                // search locally
                currentMovieID = searchIDField.getText();
                if (searchLocally(listChoiceBox.getSelectionModel().getSelectedItem().toString())) {
                    Platform.runLater(() -> {
                        movieAddButton.setVisible(true);
                        movieAddButton.setText("Update");
                        movieRemButton.setVisible(true);
                        isCurrentMovieLocal = true;

                        searchButton.setDisable(false);
                        searchButton.setText("Search");
                    });
                    return -1;
                }

                // if not local, then search on imdb
                isCurrentMovieLocal = false;


                //first -> http://api.themoviedb.org/3/find/tt0944947?api_key=81ca906cd9b89813a6365ca35ca87664&external_source=imdb_id
                //then  -> https://api.themoviedb.org/3/tv/1399?api_key=81ca906cd9b89813a6365ca35ca87664

                String get_type = Consumer.getJSON("http://api.themoviedb.org/3/find/"+ searchIDField.getText() +"?api_key=81ca906cd9b89813a6365ca35ca87664&external_source=imdb_id");
                JSONObject obj_type = new JSONObject(get_type);
                int moviedb_id = 0;
                final boolean movie;
                final boolean tv_show;
                if(obj_type.getJSONArray("movie_results").length()!=0){
                    movie = true;
                    tv_show = false;
                    moviedb_id = obj_type.getJSONArray("movie_results").getJSONObject(0).getInt("id");
                }else if(obj_type.getJSONArray("tv_results").length()!=0){
                    movie = false;
                    tv_show = true;
                    moviedb_id = obj_type.getJSONArray("tv_results").getJSONObject(0).getInt("id");
                }else{
                    movie = false;
                    tv_show = false;
                }

                if (movie || tv_show) {
                    //String html = Consumer.getJSON("http://www.omdbapi.com/?i=" + searchIDField.getText() + "&plot=short&r=json");
                    String html = "";
                    if(movie)
                        html = Consumer.getJSON("https://api.themoviedb.org/3/movie/"+ moviedb_id +"?api_key=81ca906cd9b89813a6365ca35ca87664&append_to_response=credits");
                    else if(tv_show)
                        html = Consumer.getJSON("https://api.themoviedb.org/3/tv/"+ moviedb_id +"?api_key=81ca906cd9b89813a6365ca35ca87664&append_to_response=credits");

                    JSONObject obj = new JSONObject(html);
                    System.out.println(html+"\n"+obj.toString());

                    currentMovieID = searchIDField.getText();
                    Platform.runLater(() -> {
                        String title_field;
                        if(movie)
                            title_field = "title";
                        else
                            title_field = "name";
                        movieTitleField.setText(obj.getString(title_field));

                        if (obj.getString(title_field).toLowerCase().startsWith("the ")) {
                            movieOrderTitleField.setText(obj.getString(title_field).substring(4).toLowerCase().replaceAll("[^a-z0-9 ]", ""));
                        } else {
                            movieOrderTitleField.setText(obj.getString(title_field).toLowerCase().replaceAll("[^a-z0-9 ]", ""));
                        }

                        if(movie)
                            movieYearField.setText(obj.getString("release_date").replaceAll("\\D+", "").substring(0, 4));
                        else{
                            movieYearField.setText(obj.getString("first_air_date").replaceAll("\\D+", "").substring(0, 4)
                                    +"-"+
                                    obj.getString("last_air_date").replaceAll("\\D+", "").substring(0, 4));
                        }

                        //crashes if field doest exist
                        if (obj.getDouble("vote_average")==0) {
                            movieScoreArea.setText("N/A\n");
                        } else {
                            movieScoreArea.setText(obj.getDouble("vote_average") + "/10\n");
                        }

                        String genre_temp = "";
                        for(Object genre : obj.getJSONArray("genres")){
                            genre_temp += ((JSONObject)genre).getString("name") + "\n";
                        }
                        movieGenreArea.setText(genre_temp);
                        moviePlotArea.setText(obj.getString("overview"));
                        JSONObject people_in_the_movie = obj.getJSONObject("credits");
                        JSONArray cast = people_in_the_movie.getJSONArray("cast");

                        String actors_temp = "";
                        for(int i=0; i<10 && i<cast.length(); i++){
                            actors_temp += ((JSONObject)cast.get(i)).getString("name")+"\n";
                        }
                        movieActorsArea.setText(actors_temp);

                        String directors_temp = "";
                        if(movie) {
                            JSONArray crew = people_in_the_movie.getJSONArray("crew");
                            for (int i = 0; i < crew.length(); i++) {
                                if ("Directing".equals(((JSONObject) crew.get(i)).getString("department")) &&
                                        "Director".equals(((JSONObject) crew.get(i)).getString("job"))) {
                                    if (directors_temp.length() != 0)
                                        directors_temp += ", ";
                                    directors_temp += ((JSONObject) crew.get(i)).getString("name");
                                }
                                //System.out.println(directors_temp);
                            }
                        }else{
                            JSONArray crew = obj.getJSONArray("created_by");
                            for (int i = 0; i < crew.length(); i++) {
                                if(directors_temp.length()!=0)
                                    directors_temp+=", ";
                                directors_temp += ((JSONObject) crew.get(i)).getString("name") + ", ";
                            }
                        }
                        movieDirectorField.setText(directors_temp);

                        setInfoGridVisible(true);
                    });
                    if (obj.getString("poster_path").equals("N/A") || !Consumer.getImage("http://image.tmdb.org/t/p/w342"+obj.getString("poster_path"))) {
                        try {
                            Files.copy(Paths.get(new File("").getAbsolutePath() + File.separator + "notFound.jpg"),
                                    Paths.get(new File("").getAbsolutePath() + File.separator + "temp.temp"), REPLACE_EXISTING);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    Platform.runLater(() -> {
                        movieImgView.setImage(new Image("file:temp.temp"));

                        movieAddButton.setText("Add");
                        movieAddButton.setVisible(true);
                        movieRemButton.setVisible(false);
                    });
                }
                /*if (!html.equals("{}") && "True".equals(obj.getString("Response"))) {
                    currentMovieID = obj.getString("imdbID");
                    Platform.runLater(() -> {
                        movieTitleField.setText(obj.getString("Title"));

                        if (obj.getString("Title").toLowerCase().startsWith("the ")) {
                            movieOrderTitleField.setText(obj.getString("Title").substring(4).toLowerCase());
                        } else {
                            movieOrderTitleField.setText(obj.getString("Title").toLowerCase());
                        }
                        String tempYear = obj.getString("Year").replaceAll("\\D+", "");;
                        if (tempYear.length() > 4) {
                            tempYear = tempYear.substring(0, 4) + "-" + tempYear.substring(tempYear.length() - 4, tempYear.length());
                        }
                        movieYearField.setText(tempYear);
                        if (obj.getString("imdbRating").equals("N/A") || obj.getString("Metascore").equals("N/A")) {
                            movieScoreArea.setText("N/A\nN/A");
                        } else {
                            movieScoreArea.setText(obj.getString("imdbRating") + "/10\n" + obj.getString("Metascore") + "/100");
                        }
                        movieGenreArea.setText(obj.getString("Genre").replaceAll(", ", "\n"));
                        movieActorsArea.setText(obj.getString("Actors").replaceAll(", ", "\n"));
                        moviePlotArea.setText(obj.getString("Plot"));
                        movieDirectorField.setText(obj.getString("Director"));

                        setInfoGridVisible(true);
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
                        movieImgView.setImage(new Image("file:temp.temp"));

                        movieAddButton.setText("Add");
                        movieAddButton.setVisible(true);
                        movieRemButton.setVisible(false);

                        /* imdbLink.setVisible(true);
                         torrentLink.setVisible(true);
                         webview.setVisible(true);
                         //webview.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                         webview.getEngine().load("https://www.youtube.com/embed/"
                         + Consumer.getYoutubeID(title.getText(), year.getText()));
                         magnetLink = Consumer.getMagnetLink(title.getText(), year.getText());
                         if (magnetLink == null) {
                         torrentLink.setVisible(false);
                         }*
                    });
                }*/
                else {
                    Platform.runLater(() -> {
                        //imdbLink.setVisible(false);
                        //webview.setVisible(false);
                        //torrentLink.setVisible(false);
                        movieAddButton.setVisible(false);
                        movieRemButton.setVisible(false);
                    });
                    currentMovieID = null;
                }

                // enable search button again
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

    public final void setInfoGridVisible(boolean vis) {
        movieTitleField.setVisible(vis);
        movieOrderTitleField.setVisible(vis);
        movieYearField.setVisible(vis);
        movieDirectorField.setVisible(vis);
        movieGenreArea.setVisible(vis);
        movieActorsArea.setVisible(vis);
        movieScoreArea.setVisible(vis);
        moviePlotArea.setVisible(vis);

    }

    public final void configureSearchButton() {
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchIDField.getText().startsWith("ur")) {
                    searchUserRatingThread().start();
                } else {
                    searchMovieIdThread().start();
                }
            }
        });
    }

    public final void configureAddButton() {
        movieAddButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String listName = listChoiceBox.getSelectionModel().getSelectedItem().toString();
                ImdbList tab = ((ImdbList) MovieIndexer.getTabByName(tabPane, listName));
                int[] indexes = tab.addMovie(isCurrentMovieLocal, new String[]{
                    movieTitleField.getText().replaceAll("\"", ""), movieYearField.getText(), movieDirectorField.getText(), movieGenreArea.getText().replaceAll("\"", ""),
                    movieActorsArea.getText().replaceAll("\"", ""), movieScoreArea.getText(), movieOrderTitleField.getText(), currentMovieID});

                // Reset GUI
                clearMenu();

                tabPane.getSelectionModel().select(tab);
                int moviesPerRow = (int) (tabPane.getWidth() - 24) / (200 + 4);
                ((ImdbList) tabPane.getSelectionModel().getSelectedItem()).scroller.setVvalue(
                        ((indexes[0] / moviesPerRow)) / ((Math.ceil(indexes[1] * 1.0 / moviesPerRow)) - 1));

            }
        });
    }

    public final void configureRemButton() {
        movieRemButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String listName = listChoiceBox.getSelectionModel().getSelectedItem().toString();
                ImdbList tab = ((ImdbList) MovieIndexer.getTabByName(tabPane, listName));
                tab.removeMovie(currentMovieID);

                // only delete image if its not used anywhere else
                int counter = 0;
                for (String tabName : listChoiceBoxOptions) {
                    if (searchLocally(tabName)) {
                        counter++;
                        break;
                    }
                }
                if (counter == 0) {
                    File photo = new File(currentMovieID + ".jpg");
                    if (photo.exists()) {
                        photo.delete();
                    }
                }

                clearMenu();
                tabPane.getSelectionModel().select(tab);
            }
        });
    }

    public void clearMenu() {
        movieTitleField.setText("");
        movieYearField.setText("");
        movieDirectorField.setText("");
        movieGenreArea.setText("");
        movieActorsArea.setText("");
        movieScoreArea.setText("");
        movieOrderTitleField.setText("");
        moviePlotArea.setText("");

        currentMovieID = null;
        movieImgView.setImage(null);
        movieAddButton.setText("Add");
        movieAddButton.setVisible(false);
        movieRemButton.setVisible(false);

        setInfoGridVisible(false);
        //imdbLink.setVisible(false);
        //webview.setVisible(false);
    }

    public final void configureCancelButton() {
        movieCancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clearMenu();
                MovieIndexer.selectTab(tabPane, listChoiceBox.getSelectionModel().getSelectedItem().toString());
            }
        });
    }

    public final void configureListButtons(AddMenu am) {
        listAddButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addNewTab(listNameField.getText(), am, false);
                listNameField.clear();
            }
        });

        listRemButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText("Deleting a list is permanent!");
                alert.setContentText("Are you sure?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    // ... user chose OK
                    JsonManager.removeJson(listChoiceBox.getSelectionModel().getSelectedItem().toString());

                    String listName = listChoiceBox.getSelectionModel().getSelectedItem().toString();
                    tabPane.getTabs().remove(MovieIndexer.getTabByName(tabPane, listName));
                    listChoiceBoxOptions.remove(listName);
                    listChoiceBox.getSelectionModel().select(0);

                    if (listChoiceBoxOptions.size() == 1) {
                        listRemButton.setDisable(true);
                    }
                } else {
                    // ... user chose CANCEL or closed the dialog
                    // do nothing
                }

            }
        });

        listChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number val, Number newVal) {
                if (newVal.intValue() < 0) {
                    return;
                }
                if (movieAddButton.visibleProperty().get()) {
                    if (searchLocally(listChoiceBoxOptions.get(newVal.intValue()))) {
                        movieAddButton.setVisible(true);
                        movieAddButton.setText("Update");
                        movieRemButton.setVisible(true);
                        isCurrentMovieLocal = true;
                    } else {
                        movieAddButton.setVisible(true);
                        movieAddButton.setText("Add");
                        movieRemButton.setVisible(false);
                        isCurrentMovieLocal = false;
                    }
                }
            }
        });
    }

}
