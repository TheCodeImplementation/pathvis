/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package algorithms;

import visualiser.Algorithm;
import visualiser.Vertex;

public class BreadthFirst extends Algorithm {

    public void setPseudocode() {
        pseudocode.addAll(
                "Q.enqueue(Start)",
                "while Q not empty",
                "   Current := Q.dequeue()",
                "   if Current = Destination",
                "      trace path and finish",
                "   Visited.add(Current)",
                "   for all neighbours (N) of Current",
                "      if N not in Visited or Q",
                "         Q.enqueue(N)",
                "         parent[N] := Current"
        );
    }

    @Override
    public void solve(){
        initializeFrontierAs(Algorithm.QUEUE);
        frontier.add(start);
        step(0);
        while(!frontier.isEmpty()){
            step(1);
            setCurrent(frontier.remove());
            step(2);
            step(3);
            if(getCurrent() == destination){
                visited.add(getCurrent());
                reconstructPath(getCurrent());
                drawPath();
                step(4);
                break;
            }
            visited.add(getCurrent());
            step(5);
            step(6);
            for(Vertex tmp : getCurrent().getNeighbours()){
                setNeighbour(tmp);
                step(7);
                if(!visited.contains(getNeighbour()) && !frontier.contains(getNeighbour())) {
                    frontier.add(getNeighbour());
                    step(8);
                    getNeighbour().setParentNode(getCurrent());
                    step(9);
                }
                setNeighbour(null);
            }
        }
    }
    private void reconstructPath(Vertex current) {
        addToPath(current);
        Vertex tmp = getCurrent();
        while(tmp.getParentNode() != null){
            tmp = tmp.getParentNode();
            addToPath(tmp);
        }
    }
}
