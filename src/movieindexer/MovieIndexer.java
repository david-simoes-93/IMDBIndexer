package movieindexer;

import java.io.File;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MovieIndexer extends Application {
    
    private TabPane tabs;
    private ArrayList<ImdbList> mll;
    private AddMenu am;
    private int retardedCounter = 0; 

    @Override
    public void start(Stage primaryStage) {
        
        tabs = new TabPane();
        mll=new ArrayList();

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
        Scene scene = new Scene(tabs);

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
