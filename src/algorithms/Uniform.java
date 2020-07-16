/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package algorithms;

import visualiser.Algorithm;
import visualiser.Vertex;

public class Uniform extends Algorithm {

    public void setPseudocode(){
        pseudocode.addAll(
                "Frontier.add(Start)",
                "score[Start] := dist(Start)",
                "while Frontier not empty",
                "   Current := Frontier.remove()",
                "   if Current = Destination",
                "       trace path and finish",
                "   Visited.add(Current)",
                "   for each neighbour (N) of Current",
                "       if N not in Visited",
                "           if N not in Frontier",
                "               Frontier.add(N)",
                "           if dist(Current, N) < score[N]",
                "               parent[N] := Current",
                "               score[N] := dist(Current, N)"
        );
    }

    @Override
    public void solve(){
        initializeFrontierAs(Algorithm.PRIORITY_QUEUE);
        frontier.add(start);
        step(0);
        start.setScore(0);
        step(1);
        while(!frontier.isEmpty()){
            step(2);
            setCurrent(frontier.remove());
            step(3);
            step(4);
            if(getCurrent() == destination) {
                visited.add(getCurrent());
                reconstructPath(getCurrent());
                drawPath();
                step(5);
                break;
            }
            visited.add(getCurrent());
            step(6);
            step(7);
            for(Vertex tmp : getCurrent().getNeighbours()){
                setNeighbour(tmp);
                step(8);
                if(!visited.contains(getNeighbour())) {
                    step(9);
                    if(!frontier.contains(getNeighbour())) {
                        frontier.add(getNeighbour());
                        step(10);
                    }
                    step(11);
                    if(costF(getCurrent(), getNeighbour()) < getNeighbour().getScore()) {
                        getNeighbour().setParentNode(getCurrent());
                        step(12);
                        getNeighbour().setScore(costF(getCurrent(), getNeighbour()));
                        step(13);
                    }
                }
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

    public double costF(Vertex a, Vertex b){
        return a.getScore()
                + nodeDistance(a, b)
                + getWeight(a, b);
    }

    public double nodeDistance(Vertex a, Vertex b){
        return  Math.sqrt(
                Math.pow( b.getY() - a.getY(), 2 ) +
                        Math.pow( b.getX() - a.getX(), 2 )
        );
    }
}
