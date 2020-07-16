/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

class Stack extends Deque{
    public Stack(Controller controller, int listType) {
        super(controller, listType);
    }
    public void add(Vertex node){
        super.push(node);
    }
    public Vertex remove(){
        return super.pop();
    }
}
