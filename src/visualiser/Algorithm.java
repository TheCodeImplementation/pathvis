/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mycontrols.arrows.Edge;
import mycontrols.dialog.PopUp;

import java.util.ArrayList;

public abstract class Algorithm {

    private Controller                  controller;
    protected Vertex                    start;
    protected Vertex                    destination;
    private boolean                     stepDisabled = false;
    protected static final int          STACK=0, QUEUE=1, PRIORITY_QUEUE=2;
    static protected DataStructure      frontier;
    protected DataStructure             visited;
    private VisNode                     currentNode = null;
    private VisNode                     neighbourNode = null;
    private ArrayList<VisNode>          path = new ArrayList<>();
    static ArrayList<Edge>              projectedLines = new ArrayList<>();
    protected ObservableList<String>    pseudocode = FXCollections.observableArrayList();
    private static ChangeListener<String> changeListener = Algorithm::refreshFrontier;

    void                    run(Controller controller, boolean step) {
        //initiate
        this.controller = controller;
        visited = new Stack(controller, DataStructure.VISITED);
        start = Controller.startNode.get();
        destination = Controller.destinationNode.get();
        Platform.runLater( () -> {
            for(Node n : controller.graphSpace.getChildren()){
                if(n instanceof VisNode){
                    ((VisNode)n).getSimpleString().addListener(changeListener);
                }
            }

        });
        setStepDisable(step);
        setUpPseudocode();
        //run
        solve();
        //post run
        printResults();
    }
    public abstract void    solve();
    private void            printResults(){
        if(path.size() > 0) { //if a path has been found.
            Label visitedDetails = new Label("Visited: " + (controller.listB.getItems().size()) );
            //path details
            double distanceTotal = 0;
            double weightTotal = 0;
            for (int i = 0; i < path.size()-1; i++) {
                distanceTotal += nodeDistance(path.get(i), path.get(i+1));
                weightTotal += path.get(i).getConnection(path.get(i+1)).getWeight();
            }
            distanceTotal = Math.round(distanceTotal*100d)/100d;
            weightTotal   = Math.round(weightTotal*100d)/100d;
            double pathTotal = Math.round( (distanceTotal + weightTotal) * 100d) /100d;
            Label pathDistance = new Label("Path cost: " + pathTotal + " (Distance: " + distanceTotal + " / Weight: " + weightTotal);
            VBox content = new VBox(visitedDetails, pathDistance);
            Platform.runLater( () -> {
                PopUp.pop("Results", content);
            });
        }else{
            Platform.runLater( () -> {
                PopUp.pop("Results", new Label("No path found"));
            });
        }
    }
    private double          nodeDistance(Vertex a, Vertex b){
        return  Math.sqrt(
                Math.pow( b.getY() - a.getY(), 2 ) +
                        Math.pow( b.getX() - a.getX(), 2 )
        );
    }
    protected void          addToPath(Vertex node){
        path.add( ((VisNode)node) );
    }
    protected void          drawPath(){
        for (int i = 0; i < path.size()-1; i++) {
            VisNode n = path.get(i);
            Edge a = n.getConnection(path.get(i+1));
            Platform.runLater( () -> {
                controller.setNodeStyleClass(n, Controller.PATH);
                if (a != null)
                    a.getStyleClass().add("path");
            });
        }
        //redraw start and destination colours
        Platform.runLater( () -> {
            controller.setNodeStyleClass(((VisNode)start), Controller.START);
            controller.setNodeStyleClass(((VisNode)destination), Controller.DESTINATION);
        });
    }
    protected void          step(int stepNum){
        Platform.runLater( () -> {
            controller.listC.scrollTo(stepNum);
            controller.listC.getSelectionModel().select(stepNum);
        });
        if(!stepDisabled){
            Thread.currentThread().suspend();
        }else {
            try {
                Thread.sleep(5);
            } catch (Exception ignored) {}
        }
    }
    private void            setStepDisable(boolean stepThrough) {
        this.stepDisabled = stepThrough;
    }
    private void            setUpPseudocode(){
        setPseudocode();
        if(pseudocode != null){
            Platform.runLater( () -> {
                controller.listC.setItems(pseudocode);
            });
        }
    }
    protected void          setPseudocode(){
        pseudocode = null;
    }
    protected double        getWeight(Vertex a, Vertex b){
        for(Edge e : ((VisNode)a).edges){
            if(e.getNeighbour((Node)a)==b){
                return e.getWeight();
            }
        }
        return -1;
    }
    protected void          setCurrent(Vertex node){
//        Platform.runLater( () -> {
            if(currentNode != null) //set previous value to false first
                currentNode.pseudoClassStateChanged(Controller.ps_currentNode, false);
            if(node != null) //set new value to true
                ((VisNode)node).pseudoClassStateChanged(Controller.ps_currentNode, true);
//        });
        currentNode = (VisNode)node;
    }
    protected Vertex        getCurrent(){
        return currentNode;
    }
    protected void          setNeighbour(Vertex node){
//        Platform.runLater( () -> {
            if(neighbourNode != null) //set previous value to false first
                neighbourNode.pseudoClassStateChanged(Controller.ps_neighbourNode, false);
            if(node != null) //set new value to true
                ((VisNode)node).pseudoClassStateChanged(Controller.ps_neighbourNode, true);
//        });
        neighbourNode = (VisNode)node;
    }
    protected Vertex        getNeighbour(){
        return neighbourNode;
    }
    protected void          initializeFrontierAs(int type){
//        if(frontier != null){
//            throw new Error("Frontier can only be initialized once per run.");
//        }
        switch(type){
            case STACK:
                frontier = new Stack(controller,  DataStructure.FRONTIER);
                break;
            case QUEUE:
                frontier = new Queue(controller, DataStructure.FRONTIER);
                break;
            case PRIORITY_QUEUE:
                frontier = new PriorityQueue(controller, DataStructure.FRONTIER);
                break;
        }
    }
    protected void          projectLine(Vertex a, Vertex b){
        Edge projectedLine = new Edge((VisNode)a, (VisNode)b);
        projectedLine.x1Property().bind(((VisNode)a).layoutXProperty());
        projectedLine.y1Property().bind(((VisNode)a).layoutYProperty());
        projectedLine.x2Property().bind(((VisNode)b).layoutXProperty());
        projectedLine.y2Property().bind(((VisNode)b).layoutYProperty());

        projectedLine.getStyleClass().add("projected");
        projectedLine.setHeadAVisible(false);
        projectedLine.setHeadBVisible(false);
        projectedLine.setDistanceVisible(true);
        projectedLines.add(projectedLine);
        Platform.runLater( () -> {
            controller.graphSpace.getChildren().add(projectedLine);
        });
    }
    protected void          removeProjectedLine(){
        for(Edge a : projectedLines) {
            Platform.runLater(() -> {
                controller.graphSpace.getChildren().remove(a);
            });
        }
    }
    private static void     refreshFrontier(ObservableValue<? extends String> observableValue, String s, String s1) {
        frontier.refreshFrontier();
    }
    static void             removeNodeDataListener(VisNode v){
        v.getSimpleString().removeListener(changeListener);
    }
    public String           toString(){
        return this.getClass().getSimpleName();
    }
}
