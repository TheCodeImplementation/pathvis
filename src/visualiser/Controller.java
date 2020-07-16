/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import algorithms.AStar;
import algorithms.Uniform;
import algorithms.BreadthFirst;
import algorithms.DepthFirst;
import javafx.collections.ListChangeListener;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import mycontrols.arrows.Edge;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.LogManager;

public class Controller {

    @FXML ListView listA;
    @FXML ListView listB;
    @FXML ListView listC;
    @FXML Button btnPlay;
    @FXML Button btnStep;
    @FXML Button btnReset;
    @FXML Button btnClear;
    @FXML AnchorPane graphSpace;
    @FXML ChoiceBox cbxAlgorithms;
    @FXML VBox vbLeft;
    @FXML ToggleButton chbShowWeight;
    @FXML ToggleButton chbShowDistance;
    @FXML TabPane tabPane;
    @FXML ScrollPane spNetwork;
    @FXML Accordion acrNetwork;
    @FXML AnchorPane spMoreInfo;
    @FXML TextArea txtUserAlgo;
    private VisNode node1;
    private VisNode node2;
    private VisNode nodeTmp;
    private Edge arrow;
    static ObjectProperty<VisNode> startNode = new SimpleObjectProperty<>(null);
    static SimpleObjectProperty<VisNode> destinationNode = new SimpleObjectProperty<>();
    static final int DEFAULT= 0, START=1, DESTINATION=2, VISITED=3, FRONTIER=4, PATH=5;
    private boolean deleteNode = false;
    static final PseudoClass ps_currentNode = PseudoClass.getPseudoClass("currentNode");
    static final PseudoClass ps_neighbourNode = PseudoClass.getPseudoClass("neighbourNode");
    private SimpleObjectProperty<Thread> solveThread = new SimpleObjectProperty<>();

    public void initialize(){
        setUpHotKeys();
        readInUserAlgorithms();
        for(Algorithm a : new Algorithm[]{ new AStar(), new Uniform(), new BreadthFirst(), new DepthFirst() }){
            cbxAlgorithms.getItems().add(a);
        }
        cbxAlgorithms.getSelectionModel().selectFirst();
        startNode.addListener( (a,oldV,newV) -> {
            if(oldV != null && newV != null)
                setNodeStyleClass(oldV, 0);
            if(newV != null)
                setNodeStyleClass(newV, START);
            if(newV == destinationNode.getValue())
                destinationNode.setValue(null);
        });
        destinationNode.addListener( (a,oldV,newV) -> {
            if(oldV != null && newV != null)
                setNodeStyleClass(oldV,0);
            if(newV != null)
                setNodeStyleClass(newV, DESTINATION);
            if(newV == startNode.getValue()){
                startNode.setValue(null);
            }
        });
        btnPlay.disableProperty().bind(startNode.isNull().or(destinationNode.isNull().or(solveThread.isNotNull())));
        btnStep.disableProperty().bind(startNode.isNull().or(destinationNode.isNull()));
        // Suppress warnings caused by scrollTo(), when application run in java 1.8
        LogManager.getLogManager().reset();
        //add logo to "more info" box
        Image img = new Image("visualiser/resources/TCI_logo.jpg",100,100,true,true);
        ImageView iv = new ImageView(img);
        iv.setLayoutX(260);
        iv.setLayoutY(90);
        spMoreInfo.getChildren().add(iv);
        //this text, with "\n" cannot be set in fxml so must be done here instead, unfortunately.
        txtUserAlgo.setText(
                        "Users can test themselves by writing their own versions of the path-finding algorithms.\n" +
                        "To do so, do the following:\n\n" +
                        "Create a new Java project and add a package titled \"algorithms\"\n" +
                        "In \"algorithms\", create a new class, which extends \"algorithm\"\n" +
                        "It is recommended that you read the source code for the provided algorithms to learn the " +
                                "commands provided by visualiser.PathVis.\n" +
                        "Your new class will have methods and variables for solving and visualising the problem\n" +
                        "You must override the method solve(), where you will implement your solution\n" +
                        "You have two lists included: frontier and visited. Before frontier can be used, it must be " +
                                "initialized as a stack, queue or priority queue using initializeFrontierAs(type).\n" +
                        "The method projectLine(Vertex a, Vertex b) is purely a visual function which draws a line " +
                                "between vertices. removeProjectedLines() must be called to remove them.\n" +
                        "getWeight(Vertex a, Vertex b) returns the weight of the edge connecting the two vertices\n" +
                        "Once a path has been found, add all vertices to the path list by calling addToPath(Vertex) " +
                                "then call drawPath() to visualise it.\n" +
                        "The step(stepNum) method acts as a break point in the algorithm where execution will pause " +
                                "until the user clicks the step button. The string at index stepNum in pseudocode " +
                                "(if initialized) will be highlighted.\n" +
                        "Each node in the network is of type Vertex. Vertex contains the variables, parent, used for " +
                                "storing the previous Vertex in the path, Score, the variable priority queue sorts " +
                                "by, gScore, useful in A* implementations, x and y coordinates and a getNeighbours() " +
                                "method which returns a list of connected vertices.\n" +
                        "Optionally, you may override setPseudocode() where you can add your pseudcode to the variable " +
                                "\"pseudocode\"\n" +
                        "Include this JAR in your project and withing your main, call \"visualiser.PathVis.launch()\"");
    }
    //node-space events
    @FXML private void graphSpaceMousePressed(MouseEvent e){
        if(e.isPrimaryButtonDown()){
            node1 = createAndAddNode(e.getX(), e.getY());
        }
    }
    @FXML private void graphSpaceDragDetected(MouseEvent e) {
        if(e.isPrimaryButtonDown()) {
            graphSpace.startFullDrag();
            node2 = createAndAddNode(e.getX(), e.getY());
            node2.toBack();
            arrow = createArrow(node1, node2);
            node2.getStyleClass().add("dragged");
            arrow.getStyleClass().add("dragged");
        }
    }
    @FXML private void graphSpaceMouseDragged(MouseEvent e) {
        if(node2 != null){
            node2.setLayoutX(e.getX());
            node2.setLayoutY(e.getY());
        }
    }
    @FXML private void graphSpaceMouseReleased(MouseEvent e) {
        //if mouse was released while over a pre-exisiting node
        if(nodeTmp != null){
            node1.edges.remove(arrow);
            graphSpace.getChildren().removeAll(node2, arrow);
            removeFromAccordian(node2.getId());
            //since node was discarded, lower count
            VisNode.count--;
            node2 = nodeTmp;
            arrow = createArrow(node1, node2);
        }
        if(node2 != null) {
            node2.getStyleClass().remove("dragged");
            arrow.getStyleClass().remove("dragged");
            node2 = null;
        }
    }
    //import/export
    @FXML private void graphSpaceDragOver(DragEvent e){
        if(e.getDragboard().hasFiles()){
            e.acceptTransferModes(TransferMode.ANY);
        }
    }
    @FXML private void graphSpaceDragDropped(DragEvent e) {
        try {
            if(e.getDragboard().hasFiles()) {
                List<File> files = e.getDragboard().getFiles();
                Scanner scan = new Scanner(files.get(0));
                scan.useDelimiter(",");
                HashMap<Integer, Double[]> coordinates = new HashMap<>();
                int num = scan.nextInt(); //total number of nodes in graph
                for(int i = 0; i <  num; i++){
                    coordinates.put(scan.nextInt(), new Double[]{scan.nextDouble(), scan.nextDouble()});
                }
                HashMap<Integer, VisNode> nodes = new HashMap<>();
                coordinates.forEach( (k,v) -> {
                    nodes.put(k, createAndAddNode(v[0], v[1]));
                });


                num = scan.nextInt();
                if(num != -1)
                    startNode.set(nodes.get(num));
                num = scan.nextInt();
                if(num != -1)
                    destinationNode.set(nodes.get(num));
                while(scan.hasNextInt()){
                    VisNode n1 = nodes.get(scan.nextInt());
                    VisNode n2 = nodes.get(scan.nextInt());
                    Edge arrow = createArrow(n1, n2);
                    arrow.setHeadVisible(n1, scan.nextBoolean());
                    arrow.setHeadVisible(n2, scan.nextBoolean());
                    arrow.setWeight(scan.nextDouble());
                }
            }
        }catch(Exception o){
            System.out.println("File not found.");
        }
    }
    @FXML private void exportNetwork(){
        Window mainStage = graphSpace.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save network");
        File file = fileChooser.showSaveDialog(mainStage);
        if (file != null) {
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(networkToString());
                out.close();
            }catch(Exception e){
                System.out.println("File Export issue");
                e.printStackTrace();
            }
        }
    }
    private String     networkToString(){
        ArrayList<VisNode> nodes = new ArrayList<>();
        StringBuilder data = new StringBuilder();
        //add the coordinates for every node on the graph
        for(Node n : graphSpace.getChildren()){
            if(n instanceof VisNode){
                nodes.add((VisNode)n);
                data.append(  n.getId() + "," + n.getLayoutX() + "," + n.getLayoutY() + ",");
            }
        }
        data.insert(0, nodes.size() + ","); //prefix the total number of nodes
        ///////////////////////////////
        if(startNode.get() == null)
            data.append(-1 + ",");
        else
            data.append(startNode.get().getId() + ",");
        if(destinationNode.get() == null)
            data.append(-1 + ",");
        else
            data.append(destinationNode.get().getId() + ",");
        ///////////////////////////////
        Edge arrow;
        for(Node a : graphSpace.getChildren()){
            if(a instanceof Edge){
                arrow = ((Edge)a);
                data.append(
                        (arrow.getNodeA().getId()) + "," +
                                (arrow.getNodeB().getId()) + "," +
                                arrow.isHeadVisible(arrow.getNodeA()) + "," +
                                arrow.isHeadVisible(arrow.getNodeB()) + "," +
                                arrow.getWeight() + "," );
            }
        }
        return data.toString();
    }
    //GUI button functions
    @FXML private void buildManhattan(){
        ArrayList<VisNode> tmp = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 5; j++) {
                tmp.add(createAndAddNode(j*75+100, i*75+50));
            }
        }

        for(VisNode a : tmp){
            for(VisNode b : tmp){
                int x = Integer.parseInt(a.getId());
                int y = Integer.parseInt(b.getId());
                if(x < y && Math.sqrt(Math.pow(a.getLayoutY() - b.getLayoutY(), 2) + (Math.pow(a.getLayoutX() - b.getLayoutX(), 2))) <= 75 ) {
                    createArrow(a, b);
                }
            }
        }

        startNode.set(tmp.get(0));
        destinationNode.set(tmp.get(tmp.size()-1));
    }
    @FXML private void buildTree(){
        startNode.set( treeBuilder(330,50, 170) );
    }
    public VisNode     treeBuilder(double x, double y, int dist){
        VisNode node = createAndAddNode(x,y);

        if(y < 400){
            createArrow(node, treeBuilder(x-dist, y+100, dist/2));
            createArrow(node, treeBuilder(x+dist, y+100, dist/2));
        }else{
            //one of the leaf nodes will be destination
            destinationNode.set(node);
        }
        return node;
    }
    @FXML public void  clickClear(ActionEvent actionEvent) {
        btnReset.setDisable(true);
        graphSpace.getChildren().clear();
        VisNode.count = 0;
        listA.getItems().clear();
        listB.getItems().clear();
        listC.getItems().clear();
        startNode.setValue(null);
        destinationNode.setValue(null);
        acrNetwork.getPanes().clear();
        solveThread.set(null);
    }
    @FXML public void  clickPlay(ActionEvent actionEvent) {
        boolean play = actionEvent.getSource() == btnPlay;
        btnReset.setDisable(false);

        if(solveThread.get() == null || play){
            tabPane.getSelectionModel().select(0);
            solveThread.set(new Thread( () -> {
                Algorithm a = null;
                try {
                    a = ((Algorithm)cbxAlgorithms.getValue()).getClass().getDeclaredConstructor().newInstance();
                } catch (Exception ignore) {}
                resetNetwork(null);
                a.run(this, play);
                btnReset.setDisable(false);
                solveThread.set(null);
            }));
            solveThread.get().setDaemon(true);
            solveThread.get().start();
        }else{
            solveThread.get().resume();
        }
    }
    @FXML public void  resetNetwork(ActionEvent actionEvent) {
        for(Node n : graphSpace.getChildren()) {
            if (n instanceof VisNode) {
                Algorithm.removeNodeDataListener((VisNode) n);
                ((VisNode) n).setParentNode(null);
                ((VisNode) n).setScore(Double.POSITIVE_INFINITY);
                ((VisNode) n).setGScore(Double.POSITIVE_INFINITY);
                VisNode.setShowScores(false);
                VisNode.setShowScore(false);
            }
        }
        Platform.runLater( () -> {
            if(actionEvent != null) //i.e. called from btnReset
                btnReset.setDisable(true);
            for(Node n : graphSpace.getChildren()){
                if(n instanceof VisNode){
                    ((VisNode)n).pseudoClassStateChanged(Controller.ps_currentNode, false);
                    ((VisNode)n).pseudoClassStateChanged(Controller.ps_neighbourNode, false);
                    setNodeStyleClass((VisNode) n, Controller.DEFAULT);
                }
                if(n instanceof Edge){
                    ((Edge) n).getStyleClass().removeAll("path");
                }
            }
            listA.getItems().clear();
            listB.getItems().clear();
            listC.getItems().clear();
            for(Edge a : Algorithm.projectedLines){
                graphSpace.getChildren().remove(a);
            }
            setNodeStyleClass(startNode.get(), Controller.START);
            setNodeStyleClass(destinationNode.get(), Controller.DESTINATION);
        });
        if(actionEvent != null)
            solveThread.set(null);
    }
    //node events
    private void nodeMousePressed(MouseEvent e, VisNode v) {
        if(e.isSecondaryButtonDown()){
            deleteNode = true;
        }
    }
    private void nodeDragDetected(MouseEvent e, VisNode n) {
        if(e.isPrimaryButtonDown()){
            n.startFullDrag();
            node1 = n;
            node2 = createAndAddNode(n.getLayoutX() + e.getX() + n.getTranslateX(), n.getLayoutY() + e.getY() + n.getTranslateY());
            arrow = createArrow(node1, node2);
            node2.getStyleClass().add("dragged");
            arrow.getStyleClass().add("dragged");
            node2.toBack();
        }
        if(e.isSecondaryButtonDown()){
            deleteNode = false;
            n.startFullDrag();
            n.getStyleClass().add("dragged");
            for(Edge a : n.edges){
                a.getStyleClass().add("dragged");
                a.toFront();
            }
            n.toFront();//stop this node mergeing with others.
            node2 = n;
        }
    }
    private void nodeMouseDragged(MouseEvent e, VisNode n) {
        if(node2 != null){
            //drag node with mouse
            node2.setLayoutX(n.getLayoutX() + e.getX() + n.getTranslateX());
            node2.setLayoutY(n.getLayoutY() + e.getY() + n.getTranslateY());
        }
    }
    private void nodeMouseDragEntered(MouseDragEvent e, VisNode n) {
        //if the "dragEntered" node isn't either of the two just created
        if(n != node2 && n != node1){
            n.getStyleClass().add("dragged"); //highlight it with "dragged" colours
            nodeTmp = n;
            node2.setVisible(false);
        }
    }
    private void nodeMouseDragExited(MouseDragEvent e, VisNode n) {
        if(node2 != null){
            node2.setVisible(true);
        }
        if(nodeTmp != null){
            n.getStyleClass().remove("dragged");
            nodeTmp = null;
        }
    }
    private void nodeMouseReleased(MouseEvent e, VisNode n) {
        //if mouse was released while over a pre-exisiting node
        if(nodeTmp != null){
            node1.edges.remove(arrow);
            graphSpace.getChildren().removeAll(node2, arrow);
            removeFromAccordian(node2.getId());
            //since node was discarded, lower count
            VisNode.count--;
            node2 = nodeTmp;
            if(!node1.getNeighbours().contains(node2)) {
                arrow = createArrow(node1, node2);
            }
        }
        if(node2 != null) {
            node2.getStyleClass().remove("dragged");
        }
        for(Edge a : n.edges){
            a.getStyleClass().remove("dragged");
        }
        if(deleteNode){
            deleteNode(n);
            //if the node was start or destination, play button should disable.
            if(n == startNode.get() ) {
                startNode.set(null);
            }else if(n == destinationNode.get()){
                destinationNode.set(null);
            }
            deleteNode = false;
            removeFromAccordian(n.getId());
        }
        clearVariables();
    }
    //helper methods
    private VisNode createAndAddNode(double x, double y) {
        VisNode vNode = new VisNode(x,y);
        graphSpace.getChildren().add(vNode);
        addToAccordian(vNode);
        vNode.setOnMousePressed(z -> nodeMousePressed(z, vNode));
        vNode.setOnDragDetected(z -> nodeDragDetected(z, vNode));
        vNode.setOnMouseDragged(z -> nodeMouseDragged(z, vNode));
        vNode.setOnMouseReleased(z -> nodeMouseReleased(z, vNode));
        vNode.setOnMouseDragEntered(z -> nodeMouseDragEntered(z, vNode));
        vNode.setOnMouseDragExited(z -> nodeMouseDragExited(z, vNode));
        return vNode;
    }
    private void    deleteNode(VisNode v){
        //remove all arrows connected to node
        for(Edge a : v.edges){
            ((VisNode)a.getNeighbour(v)).edges.remove(a);
            graphSpace.getChildren().remove(a);
        }
        //remove node itself
        graphSpace.getChildren().remove(v);
    }
    Edge            createArrow(VisNode n1, VisNode n2){
        Edge tmp = new Edge(n1, n2);
        tmp.distanceVisibleProperty().bind(chbShowDistance.selectedProperty());
        tmp.weightVisibleProperty().bind(chbShowWeight.selectedProperty());

        n1.edges.add(tmp);
        n2.edges.add(tmp);
        graphSpace.getChildren().add(tmp);
        return tmp;
    }
    void            setNodeStyleClass(VisNode node, int style){
        node.getStyleClass().removeAll("start", "destination", "visited", "frontier", "path");
        switch(style){
            case START:
                node.getStyleClass().add("start");
                break;
            case DESTINATION:
                node.getStyleClass().add("destination");
                break;
            case VISITED:
                node.getStyleClass().add("visited");
                break;
            case FRONTIER:
                node.getStyleClass().add("frontier");
                break;
            case PATH:
                node.getStyleClass().add("path");
                break;
        }
    }
    private void    clearVariables(){
        if(node1 != null){
            node1 = null;
        }
        if(node2 != null){
            node2 = null;
        }
        if(nodeTmp != null){
            nodeTmp = null;
        }
        if(arrow != null){
            arrow = null;
        }
    }
    private void    readInUserAlgorithms(){
        //Thanks to Stack Overflow user Ahmed Ashour for his help with this code: https://stackoverflow.com/a/520344
        try{
            String path = "algorithms";
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            assert classLoader != null;
            Enumeration<URL> resources = classLoader.getResources(path);
            File dir = new File(resources.nextElement().getFile());
            ArrayList<Class> classes = new ArrayList<>();

            //if a directory name contains a space, the above code will replaces it with "%20". The following changes it back.
            dir = new File(dir.toString().replace("%20", " "));

            if(dir.listFiles() != null) {
                for (File file : dir.listFiles()) {
                    if (file != null && file.getName().endsWith(".class")) {
                        classes.add(Class.forName(path + '.' + file.getName().replace(".class", "")));
                    }
                }
                for (Class c : classes) {
                    Object a = c.getDeclaredConstructor().newInstance();
                    if (a instanceof Algorithm) {
                        cbxAlgorithms.getItems().add(a);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void    addToAccordian(VisNode n) {
        NodeBox nb = new NodeBox(this, n);
        TitledPane tp = new TitledPane(n.getId(), nb);
        tp.setPrefWidth(spNetwork.getWidth()-5);
        acrNetwork.getPanes().add(tp);
        //refresh title pane whenever it is opened
        tp.expandedProperty().addListener((javafx.beans.value.ChangeListener<? super Boolean>) (l,ov,nv) -> {
            if(nv){
                nb.refresh();
            }
        });
        n.edges.addListener( (ListChangeListener<? super Edge>) c -> {
            if(tp.isExpanded()){
                ((NodeBox)tp.getContent()).refresh();
            }
        });
        n.setOnAction( e -> {
            tp.setExpanded(true);
            //switch tab
            tabPane.getSelectionModel().select(1);
            //scroll to selection
            spNetwork.setVvalue(tp.getBoundsInParent().getMinY()/acrNetwork.getHeight());
        });
    }
    private void    removeFromAccordian(String id) {
        //and remove from nodeBox
        for(TitledPane tp : acrNetwork.getPanes()){
            if(tp.getText().equals(id)){
                acrNetwork.getPanes().remove(tp);
                break;
            }
        }
    }
    private void    setUpHotKeys() {
        Map<KeyCombination, Runnable> map = new HashMap<>();
        map.put(new KeyCodeCombination(KeyCode.P), () -> btnPlay.fire());
        map.put(new KeyCodeCombination(KeyCode.O), () -> btnStep.fire());
        map.put(new KeyCodeCombination(KeyCode.W), () -> chbShowWeight.fire());
        map.put(new KeyCodeCombination(KeyCode.E), () -> chbShowDistance.fire());
        map.put(new KeyCodeCombination(KeyCode.C), () -> btnClear.fire());
        map.put(new KeyCodeCombination(KeyCode.I), () -> btnReset.fire());
        map.put(new KeyCodeCombination(KeyCode.M), () -> buildManhattan());
        map.put(new KeyCodeCombination(KeyCode.T), () -> buildTree());
        map.put(new KeyCodeCombination(KeyCode.DIGIT1), () -> cbxAlgorithms.getSelectionModel().select(0) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT2), () -> cbxAlgorithms.getSelectionModel().select(1) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT3), () -> cbxAlgorithms.getSelectionModel().select(2) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT4), () -> cbxAlgorithms.getSelectionModel().select(3) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT5), () -> cbxAlgorithms.getSelectionModel().select(4) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT6), () -> cbxAlgorithms.getSelectionModel().select(5) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT7), () -> cbxAlgorithms.getSelectionModel().select(6) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT8), () -> cbxAlgorithms.getSelectionModel().select(7) );
        map.put(new KeyCodeCombination(KeyCode.DIGIT9), () -> cbxAlgorithms.getSelectionModel().select(8) );

        Platform.runLater( () -> {
            graphSpace.getScene().getAccelerators().putAll(map);
        });
    }
}