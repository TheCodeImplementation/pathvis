/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import java.util.ArrayDeque;

abstract class Deque extends DataStructure {
    private ArrayDeque<Vertex> frontier = new ArrayDeque<>();

    Deque(Controller controller, int listType) {
        super(controller, listType);
    }
    void push(Vertex node){
        frontier.push(node);
        super.visualiseAdd(node);
    }
    Vertex pop(){
        Vertex node = frontier.pop();
        super.visualiseRemove(node);
        return node;
    }
    void enqueue(Vertex node){
        frontier.add(node);
        super.visualiseAdd(node);
    }
    Vertex dequeue(){
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
}
