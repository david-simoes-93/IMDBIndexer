package movieindexer;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FirstPreloader extends Preloader {
    static String labelText="";
    
    Label label;
    ProgressBar bar;
    Stage stage;
    boolean noLoadingProgress = true;
 
    private Scene createPreloaderScene() {
        bar = new ProgressBar(0);
        label = new Label("Loading...");
        BorderPane p = new BorderPane();
        p.setBottom(bar);
        p.setCenter(label);
        bar.setMinWidth(300);

        return new Scene(p, 300, 150);
    }
 
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setScene(createPreloaderScene());
        stage.show();

    }
 
    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        //ignore
    }
 
    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        //ignore, hide after application signals it is ready
    }
 
    @Override
    public void handleApplicationNotification(PreloaderNotification pn) {
        if (pn instanceof ProgressNotification) {
           //expect application to send us progress notifications with progress ranging from 0 to 1.0
           double v = ((ProgressNotification) pn).getProgress();
           bar.setProgress(v);  
           label.setText(labelText);
        } else if (pn instanceof StateChangeNotification) {
            //hide after get any state update from application
            stage.hide();
        }
    }  
}
