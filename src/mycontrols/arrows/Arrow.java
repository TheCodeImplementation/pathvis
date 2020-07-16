/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package mycontrols.arrows;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;

public class Arrow extends Group {
    private final Polyline        mainLine            = new Polyline();
    private final Polyline        headA               = new Polyline();
    private final Polyline        headB               = new Polyline();
    private final DoubleProperty  x1                  = new SimpleDoubleProperty();
    private final DoubleProperty  y1                  = new SimpleDoubleProperty();
    private final DoubleProperty  x2                  = new SimpleDoubleProperty();
    private final DoubleProperty  y2                  = new SimpleDoubleProperty();
    private double                arrowHeadAngle      = Math.toRadians(20); //angle of arrow head
    private final double          arrowHeadLength     = 10; //arrow length
    private final double          arrowLengthScaler   = 20; //the amount to scale the arrow in from the nodes.
    //CONSTRUCTORS
    public Arrow(){
        this(0,0,0,0);
    }
    public Arrow(double x1, double y1, double x2, double y2){
        //parameter assignment
        this.x1.set(x1);
        this.y1.set(y1);
        this.x2.set(x2);
        this.y2.set(y2);
        getChildren().addAll(mainLine, headA, headB);

        setUpStyleClassStructure();

        for(DoubleProperty dp : new DoubleProperty[]{this.x1,this.y1,this.x2,this.y2}){
            dp.addListener( (l,o,n) -> {
                update();
            });
        }
        update();
    }
    //METHODS
    public void setUpStyleClassStructure(){
        //whenever this class's styleclass is updated, update the children as well.
        getStyleClass().addListener((ListChangeListener<? super String>) c -> {
            c.next();
            for(Node n : new Polyline[]{mainLine,headA,headB}){
                n.getStyleClass().addAll(c.getAddedSubList());
                n.getStyleClass().removeAll(c.getRemoved());
            }
        });
        //apply a class for styling the entire arrow
        for(Polyline p : new Polyline[]{mainLine,headA,headB}){
            p.getStyleClass().setAll("arrow");
        }
        //apply a class for styling the arrow heads only
        for(Polyline p : new Polyline[]{headA,headB}){
            p.getStyleClass().add("arrowhead");
        }
    }
    private void update(){
        //scale the coordinates in a bit
        double[] start = scale(x2.get(),y2.get(), x1.get(),y1.get());
        double[] end   = scale(x1.get(),y1.get(), x2.get(),y2.get());
        //xy coordinates
        double x1 = start[0];
        double y1 = start[1];
        double x2 = end[0];
        double y2 = end[1];
        //main line
        mainLine.getPoints().setAll(x1,y1,x2,y2);
        //line slope
        double theta = Math.atan2( (y2-y1), (x2-x1) );
        //arrow head 1
        double x = x1 + Math.cos(theta + arrowHeadAngle) * arrowHeadLength;
        double y = y1 + Math.sin(theta + arrowHeadAngle) * arrowHeadLength;
        headA.getPoints().setAll(x,y, x1 , y1);
        x = x1 + Math.cos(theta - arrowHeadAngle) * arrowHeadLength;
        y = y1 + Math.sin(theta - arrowHeadAngle) * arrowHeadLength;
        headA.getPoints().addAll(x,y);
        //arrow head 2
        x = x2 - Math.cos(theta + arrowHeadAngle) * arrowHeadLength;
        y = y2 - Math.sin(theta + arrowHeadAngle) * arrowHeadLength;
        headB.getPoints().setAll(x,y, x2,y2);
        x = x2 - Math.cos(theta - arrowHeadAngle) * arrowHeadLength;
        y = y2 - Math.sin(theta - arrowHeadAngle) * arrowHeadLength;
        headB.getPoints().addAll(x,y);
    }
    private double[] scale(double x1, double y1, double x2, double y2){
        double theta = Math.atan2(y2-y1, x2-x1);
        return new double[]{
                x1 + Math.cos(theta) * arrowLengthScaler,
                y1 + Math.sin(theta) * arrowLengthScaler
        };
    }
    //GETTERS & SETTERS
    public double                   getX1() {
        return x1.get();
    }
    public DoubleProperty           x1Property() {
        return x1;
    }
    public void                     setX1(double x1) {
        this.x1.set(x1);
    }
    public double                   getY1() {
        return y1.get();
    }
    public DoubleProperty           y1Property() {
        return y1;
    }
    public void                     setY1(double y1) {
        this.y1.set(y1);
    }
    public double                   getX2() {
        return x2.get();
    }
    public DoubleProperty           x2Property() {
        return x2;
    }
    public void                     setX2(double x2) {
        this.x2.set(x2);
    }
    public double                   getY2() {
        return y2.get();
    }
    public DoubleProperty           y2Property() {
        return y2;
    }
    public void                     setY2(double y2) {
        this.y2.set(y2);
    }
    public DoubleProperty[]         getCoordinatePropertyList(){
        return new DoubleProperty[]{x1,y1,x2,y2};
    }
    public boolean                  isHeadAVisible() {
        return headA.isVisible();
    }
    public BooleanProperty          headAVisibleProperty() {
        return headA.visibleProperty();
    }
    public void                     setHeadAVisible(boolean headAVisible) {
        headA.setVisible(headAVisible);
    }
    public boolean                  isHeadBVisible() {
        return headB.isVisible();
    }
    public BooleanProperty          headBVisibleProperty() {
        return headB.visibleProperty();
    }
    public void                     setHeadBVisible(boolean headBVisible) {
        headB.setVisible(headBVisible);
    }
    public double                   getArrowHeadAngle() {
        return arrowHeadAngle;
    }
    public void                     setArrowHeadAngle(double arrowHeadAngle) {
        this.arrowHeadAngle = arrowHeadAngle;
    }
    public double                   getArrowHeadLength() {
        return arrowHeadLength;
    }
    public double                   getArrowLengthScaler() {
        return arrowLengthScaler;
    }
    public ObservableList<String>   getArrowHeadAStyleClass(){
        return headA.getStyleClass();
    }
    public ObservableList<String>   getArrowHeadBStyleClass(){
        return headB.getStyleClass();
    }
}
