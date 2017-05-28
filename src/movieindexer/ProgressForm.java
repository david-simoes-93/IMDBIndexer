package movieindexer;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by david on 28-05-2017.
 */
public class ProgressForm {
    private final Stage dialogStage;
    private final ProgressBar pb = new ProgressBar();
    private final ProgressIndicator pin = new ProgressIndicator();
    Label label;
    boolean [] gotCancelled;

    public ProgressForm(boolean [] gotCancelled) {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        this.gotCancelled = gotCancelled;

        // PROGRESS BAR
        label = new Label();
        label.setText("Starting...\n0 errors.");

        pb.setProgress(-1F);
        pin.setProgress(-1F);

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(pb, pin);

        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(label, hb);

        dialogStage.setOnCloseRequest(event -> {
            gotCancelled[0]=true;
            // Save file
        });

        Scene scene = new Scene(vb);
        dialogStage.setScene(scene);
    }

    public void activateProgressBar(final Task<?> task)  {
        pb.progressProperty().bind(task.progressProperty());
        pin.progressProperty().bind(task.progressProperty());
        dialogStage.show();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }
}

