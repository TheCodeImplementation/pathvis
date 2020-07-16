/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package mycontrols.arrows;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class ContentArrow extends Arrow {
    private final Pane content  = new Pane();
    //CONSTRUCTORS
    public ContentArrow(){
        this(0,0,0,0);
    }
    public ContentArrow(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);

        getChildren().addAll(content);

        //node coordinates = the arrow's mid-point minus 1/2 the width/height, so the content is bang in the centre
        content.layoutXProperty().bind(x2Property().add(x1Property()).divide(2).subtract(content.widthProperty().divide(2)));
        content.layoutYProperty().bind(y2Property().add(y1Property()).divide(2).subtract(content.heightProperty().divide(2)));
    }
    //GETTERS & SETTERS
    public void            addContent(Node content){
        this.content.getChildren().setAll(content);
    }
    public Node            getContent() {
        return content.getChildren().get(0);
    }
    public boolean         isContentVisible() {
        return content.isVisible();
    }
    public BooleanProperty contentVisibleProperty() {
        return content.visibleProperty();
    }
    public void            setcontentVisible(boolean showContent) {
        content.setVisible(showContent);
    }
}
