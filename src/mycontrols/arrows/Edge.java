/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package mycontrols.arrows;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class Edge extends ContentArrow {
    private Node                    nodeA;
    private Node                    nodeB;
    private final DoubleProperty    weight = new SimpleDoubleProperty();
    private final BooleanProperty   weightVisible = new SimpleBooleanProperty(false);
    private final Label             lblDistance = new Label();
    private final Label             lblWeight = new Label();
    //CONSTRUCTORS
    public Edge(){
        this(0,0,0,0);
    }
    public Edge(Node nodeA, Node nodeB){
        this(nodeA.getLayoutX(), nodeA.getLayoutY(), nodeB.getLayoutX(), nodeB.getLayoutY());
        x1Property().bind(nodeA.layoutXProperty());
        y1Property().bind(nodeA.layoutYProperty());
        x2Property().bind(nodeB.layoutXProperty());
        y2Property().bind(nodeB.layoutYProperty());
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }
    public Edge(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
        //distance label
        lblDistance.getStyleClass().add("arrowContent");
        IntegerProperty distance = new SimpleIntegerProperty();
        lblDistance.textProperty().bind(Bindings.concat("d:", distance.asString()));
        distance.bind(Bindings.createDoubleBinding( () ->
                        Math.sqrt(
                                Math.pow(getY2() - getY1(), 2) +
                                Math.pow(getX2() - getX1(), 2)
                        ),
                y2Property(), y1Property(), x2Property(), x1Property()
        ));
        lblDistance.managedProperty().bind(lblDistance.visibleProperty());
        //weight label
        lblWeight.getStyleClass().add("arrowContent");
        weight.addListener( (l,o,n) -> {
            if(n.doubleValue() != 0){
                getStyleClass().add("weight");
            }else{
                getStyleClass().remove("weight");
            }
        });
        lblWeight.visibleProperty().bind(weight.isNotEqualTo(0).and(weightVisible));
        lblWeight.textProperty().bind(Bindings.concat("w:", weight.asString()));
        lblWeight.managedProperty().bind(lblWeight.visibleProperty());
        addContent(new HBox(5, lblWeight, lblDistance));
    }
    //METHODS
    public Node                     getNeighbour(Node me){
        return me == nodeA ?  nodeB :  nodeA;
    }
    public boolean                  isHeadVisible(Node n){
        return n == nodeA ? isHeadAVisible() : isHeadBVisible();
    }
    public void                     setHeadVisible(Node me, Boolean bool){
        if(me == nodeA)
            setHeadAVisible(bool);
        else
            setHeadBVisible(bool);
    }
    public ObservableList<String>   getArrowHeadStyleClass(Node me){
        return me == nodeA ? getArrowHeadAStyleClass() : getArrowHeadBStyleClass();
    }
    //GETTERS & SETTERS
    public double           getWeight() {
        return weight.get();
    }
    public DoubleProperty   weightProperty() {
        return weight;
    }
    public void             setWeight(double weight) {
        this.weight.set(weight);
    }
    public boolean          isWeightVisible() {
        return weightVisible.get();
    }
    public BooleanProperty  weightVisibleProperty() {
        return weightVisible;
    }
    public void             setWeightVisible(boolean weightVisible) {
        this.weightVisible.set(weightVisible);
    }
    public boolean          isDistanceVisible() {
        return lblDistance.isVisible();
    }
    public BooleanProperty  distanceVisibleProperty() {
        return lblDistance.visibleProperty();
    }
    public void             setDistanceVisible(boolean distanceVisible) {
        lblDistance.setVisible(distanceVisible);
    }
    public Node             getNodeA() {
        return nodeA;
    }
    public void             setNodeA(Node nodeA) {
        this.nodeA = nodeA;
    }
    public Node             getNodeB() {
        return nodeB;
    }
    public void             setNodeB(Node nodeB) {
        this.nodeB = nodeB;

    }
}
