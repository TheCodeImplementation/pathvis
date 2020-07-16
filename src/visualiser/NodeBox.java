/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import mycontrols.arrows.Edge;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mycontrols.input.DoubleScroller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class NodeBox extends VBox{
    private Controller controller;
    private VisNode node;
    private boolean initiated = false;
    @FXML HBox visNodeContainer;
    @FXML DoubleScroller doubleScrollerX;
    @FXML DoubleScroller doubleScrollerY;
    @FXML Button cmdStart;
    @FXML Button cmdDestination;
    @FXML VBox vbEdges;

    public NodeBox(Controller controller, VisNode node){
        this.controller = controller;
        this.node = node;
    }

    public void refresh(){
        if(!initiated) {
            getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NodeBox.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            visNodeContainer.getChildren().add(new VisNode(node));
            doubleScrollerX.setWidth(60);
            doubleScrollerY.setWidth(60);
            Bindings.bindBidirectional(doubleScrollerX.valueProperty(), node.layoutXProperty());
            Bindings.bindBidirectional(doubleScrollerY.valueProperty(), node.layoutYProperty());

            createNodeModifier(controller, node);
            initiated = true;
        }
        ///////////////////Only the following code will run on subsequent refresh() calls///////////////////////
        vbEdges.getChildren().clear();

        for(Edge a : node.edges){
            //the leftmost node on the graph should be left in the nodebox too.
            boolean lefty = node.getLayoutX() <= a.getNeighbour(node).getLayoutX();
            VisNode leftMost = lefty ? node : (VisNode)a.getNeighbour(node);
            VisNode rightMost = !lefty ? node : (VisNode)a.getNeighbour(node);
            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);

            HBox edges = new HBox();
            edges.getChildren().addAll(
                    new VisNode(leftMost),
                    createArrowModifier("<-", a, leftMost),
                    createWeightModifier(a),
                    createArrowModifier("->", a, rightMost),
                    new VisNode(rightMost),
                    space,
                    createArrowRemover(controller, a, (VisNode)a.getNeighbour(node))
            );
            edges.setOnMouseEntered(ev -> {
                a.getStyleClass().add("dragged");
            });
            edges.setOnMouseExited(ev -> {
                a.getStyleClass().remove("dragged");
            });
            edges.setAlignment(Pos.CENTER);
            edges.setPadding(new Insets(0,10,0,10));
            vbEdges.getChildren().add(edges);
        }
    }

    private void createNodeModifier(Controller controller, VisNode n){
        cmdStart.setOnAction(e -> {
            Controller.startNode.set(n);
        });
        cmdDestination.setOnAction(e -> {
            Controller.destinationNode.set(n);
        });
        //hotkeys
        Map<KeyCombination, Runnable> map = new HashMap<>();
        map.put(new KeyCodeCombination(KeyCode.S), () -> cmdStart.fire());
        map.put(new KeyCodeCombination(KeyCode.D), () -> cmdDestination.fire());
        Platform.runLater( () -> {
            controller.graphSpace.getScene().getAccelerators().putAll(map);
        });
    }

    private Button createArrowModifier(String text, Edge a, VisNode n){
        Button btn = new Button();
        btn.setPrefWidth(40);
        btn.setText(a.isHeadVisible(n) ? text : "-");
        btn.setOnMouseEntered( e -> {
            a.getArrowHeadStyleClass(n).add("arrowHeadHover");
        });
        btn.setOnMouseExited( e -> {
            a.getArrowHeadStyleClass(n).remove("arrowHeadHover");
        });
        btn.setOnAction(e -> {
            a.setHeadVisible(n, !a.isHeadVisible(n));
            btn.setText(a.isHeadVisible(n) ? text : "-");
        });
        return btn;
    }

    private DoubleScroller createWeightModifier(Edge a) {
        DoubleScroller ds = new DoubleScroller(0d, Double.POSITIVE_INFINITY, a.getWeight(), 1d);
        ds.setWidth(60);
        ds.valueProperty().addListener( (list, oldV, newV) -> {
                a.setWeight(newV.doubleValue());
        });
        return ds;
    }

    public Button createArrowRemover(Controller controller, Edge a, VisNode n){
        Button btn = new Button();
        btn.getStyleClass().add("rubbish_bin");
        btn.setMaxWidth(10);
        btn.setOnAction(e -> {
            n.edges.remove(a);
            ((VisNode)a.getNeighbour(n)).edges.remove(a);
            controller.graphSpace.getChildren().remove(a);
            refresh();
        });
        return btn;
    }
}