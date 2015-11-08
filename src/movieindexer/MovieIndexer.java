package movieindexer;

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
    private MovieList ml;
    private AddMenu am;
    private int retardedCounter = 0;

    @Override
    public void start(Stage primaryStage) {
        tabs = new TabPane();
        
        // Add tab
        Tab tabAlign = new Tab();
        tabAlign.setText("Add");
        am = new AddMenu(tabs);
        tabAlign.setContent(am);
        
        // Movies tab
        Tab tabMovies = new Tab();
        tabMovies.setText("List");
        ml = new MovieList(tabs, am);
        tabMovies.setContent(ml);

        // Tabs
        tabs.getTabs().addAll(tabMovies, tabAlign);
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
                    ml.flow.setPrefWrapLength(tabs.getWidth() - 24);
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
