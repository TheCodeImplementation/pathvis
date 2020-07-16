/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import mycontrols.arrows.Edge;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;

import java.util.ArrayList;

public class VisNode extends Button implements Vertex {

    static int                              count = 0;
    private SimpleObjectProperty<Vertex>    parent = new SimpleObjectProperty<>(null);
    private DoubleProperty                  score =  new SimpleDoubleProperty(Double.POSITIVE_INFINITY);
    private DoubleProperty                  gScore =  new SimpleDoubleProperty(Double.POSITIVE_INFINITY);
    private double                          hScore = 0;
    public ObservableList<Edge>             edges = FXCollections.observableArrayList();
    private static BooleanProperty          showScores = new SimpleBooleanProperty(false);
    private static BooleanProperty          showScore = new SimpleBooleanProperty(false);
    SimpleStringProperty                    info = new SimpleStringProperty(){ @Override public String toString(){
        StringBuilder sb = new StringBuilder();
            sb.append(
                    "ID: " + getId() +
                    " | Parent: " + parent.getValue()
            );
            if(showScores.getValue()) {
                sb.append(
                        " | gScore: " + (gScore.getValue() != Double.POSITIVE_INFINITY ? Math.round(gScore.get()*100d)/100d : gScore.get())
                );
            }
            if(showScores.getValue() || showScore.getValue()){
                sb.append(
                        " | Score: "  + (score.getValue() != Double.POSITIVE_INFINITY ? Math.round(score.get()*100d)/100d : score.get())
                );
            }
        return sb.toString();
    }};

    public                      VisNode(){
        getStyleClass().setAll("visNode");
    }
    public                      VisNode(VisNode ref){
        textProperty().bind(ref.textProperty());
        getStyleClass().setAll(ref.getStyleClass());
        ref.getStyleClass().addListener((ListChangeListener<String>) c -> {
            getStyleClass().setAll(ref.getStyleClass());
        });
        setOnAction(e -> {
            ref.fire();
        });
    }
    public                      VisNode(double x, double y){
        setId(count++ + "");
        textProperty().bind(idProperty());
        translateXProperty().bind(widthProperty().divide(-2));
        translateYProperty().bind(heightProperty().divide(-2));
        setLayoutX(x);
        setLayoutY(y);
        getStyleClass().setAll("visNode");
        info.bind(Bindings.concat(parent, gScore, score));
    }

    public ArrayList<Vertex>    getNeighbours(){
        ArrayList<Vertex> neighbours = new ArrayList<>();
        for(Edge a : edges){
            if(a.isHeadVisible(a.getNeighbour(this))){
                neighbours.add((Vertex)a.getNeighbour(this));
            }
        }
        return neighbours;
    }
    public Edge                 getConnection(VisNode neighbour){
        for(Edge a : edges){
            if(a.getNeighbour(this) == neighbour){
                return a;
            }
        }
        return null;
    }
    public SimpleStringProperty getSimpleString(){
        /*
         *The simple string property will be used in listViews. This way the listview will be updated
         * when the node's properties change.
         */
        return info;
    }

    public void                 setParentNode(Vertex node){
        this.parent.set(node);
    }
    public Vertex               getParentNode(){
        return parent.getValue();
    }
    public void                 setScore(double score){
        showScore.setValue(true);
        this.score.set(score);
    }
    public double               getScore(){
        showScore.setValue(true);
        return score.getValue();
    }
    public void                 setGScore(double score) {
        showScores.setValue(true);
        gScore.set(score);
    }
    public double               getGScore() {
        showScores.setValue(true);
        return gScore.get();
    }
    public double               getX() {
        return getLayoutX();
    }
    public double               getY() {
        return getLayoutY();
    }
    public static void          setShowScores(boolean show){
        showScores.setValue(show);
    }
    public static void          setShowScore(boolean show){
        showScore.setValue(show);
    }
    public String               toString(){
        return getId();
    }
}