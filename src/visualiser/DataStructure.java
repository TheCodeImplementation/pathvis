/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class DataStructure {

    ObservableList<SimpleStringProperty> list = FXCollections.observableArrayList();
    Controller controller;
    public static final int FRONTIER=0, VISITED=1;
    private int listType;

    public DataStructure(Controller controller, int listType){
        Platform.runLater( () -> {
            this.controller = controller;
            this.listType = listType;
            if(listType == FRONTIER)
                controller.listA.setItems(list);
            else if(listType == VISITED)
                controller.listB.setItems(list);
        });
    }

    public abstract void  add(Vertex node);
    public abstract Vertex remove();
    void visualiseAdd(Vertex node){
        Platform.runLater( () ->{
            list.addAll(((VisNode)node).getSimpleString());
            if(listType == FRONTIER)
                controller.setNodeStyleClass((VisNode)node, Controller.FRONTIER);
            else if(listType == VISITED) {
                controller.setNodeStyleClass((VisNode)node, Controller.VISITED);
            }
        });
    }
    void visualiseRemove(Vertex node){
        Platform.runLater( () -> {
            list.remove(((VisNode)node).getSimpleString());
            controller.setNodeStyleClass((VisNode)node, Controller.DEFAULT);
        });
    }
    public void refreshFrontier(){
        controller.listA.refresh();
    }
    public abstract boolean contains(Vertex node);
    public abstract boolean isEmpty();
}
