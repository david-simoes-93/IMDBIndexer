package movieindexer;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FirstPreloader extends Preloader {
    ProgressBar bar;
    Stage stage;
    ProgressIndicator pi;
    Label label;
 
    private Scene createPreloaderScene() {
        bar = new ProgressBar();
        pi = new ProgressIndicator(0);
        label = new Label("@");
        
        HBox  p = new HBox ();
        
        p.getChildren().addAll(bar, pi, label);
        return new Scene(p, 300, 150);        
    }
    
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setScene(createPreloaderScene());        
        stage.show();
    }
    
    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        System.out.println("% "+pn.getProgress());
        bar.setProgress(pn.getProgress());
    }
 
    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }    
}
