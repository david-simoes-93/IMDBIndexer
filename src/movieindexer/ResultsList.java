package movieindexer;

import javafx.scene.control.TabPane;

public class ResultsList extends ImdbList{

    public ResultsList(TabPane t, AddMenu am, String name) {
        super(t, am, "Results");
        super.jsonName=name;
    }

}
