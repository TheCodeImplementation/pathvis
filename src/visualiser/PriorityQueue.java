/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.util.Comparator;

class PriorityQueue extends DataStructure {
    private java.util.PriorityQueue<Vertex> frontier = new java.util.PriorityQueue<>(Comparator.comparingDouble(Vertex::getScore));

    PriorityQueue(Controller controller, int listType) {
        super(controller, listType);
    }
    public void add(Vertex node){
        frontier.add(node);
        super.visualiseAdd(node);
    }
    public Vertex remove(){
        Vertex node = frontier.remove();
        super.visualiseRemove(node);
        return node;
    }
    public boolean contains(Vertex node){
        return frontier.contains(node);
    }
    public boolean isEmpty(){
        return frontier.isEmpty();
    }
    @Override
    public void refreshFrontier(){
        super.refreshFrontier();
        frontierListViewSort();
        frontierUpdate();
    }
    private void frontierListViewSort() {
        Platform.runLater(() -> {
            FXCollections.sort(list, (a, b) -> {
                String[] aa = a.toString().split(" Score: ");
                String[] bb = b.toString().split(" Score: ");
                double aScore = Double.parseDouble(aa[1]);
                double bScore = Double.parseDouble(bb[1]);
                return Double.compare(aScore, bScore);
            });
        });
    }
    private void frontierUpdate(){
        //priority queues only order elements on insert. if an entry's value changes, you have to re-do the insert to re-order.
        java.util.PriorityQueue<Vertex> frontierCopy = new java.util.PriorityQueue<>(Comparator.comparingDouble(Vertex::getScore));
        for(Vertex v: frontier){
            frontierCopy.add(v);
        }
        frontier = frontierCopy;
    }
}
