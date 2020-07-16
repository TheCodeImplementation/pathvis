/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package algorithms;

import visualiser.Algorithm;
import visualiser.Vertex;

public class AStar extends Algorithm {

    public void setPseudocode(){
        pseudocode.addAll(
                "Frontier.add(Start)",
                "Score[Start] := gScore[Start] + hScore[Start]", //Score == fScore
                "while Frontier not empty",
                "   Current := lowest f-score node in Frontier",
                "   if Current = Destination",
                "       trace path and finish",
                "   Visited.add(Current)",
                "   for each Neighbour (N) of Current",
                "       if N not in Visited",
                "           if N not in Frontier",
                "               Frontier.add(N)",
                "           if calcGScore(Current, N) < gScore[N]",
                "               parent[N] := Current",
                "               gScore[N] := calGScore(Current, N)",
                "               Score[N] := gScore[N] + hScore[N]"
        );
    }
    @Override
    public void solve(){
        initializeFrontierAs(Algorithm.PRIORITY_QUEUE);
        frontier.add(start);
        step(0);
        start.setGScore(0);
        start.setScore(nodeDistance(start, destination)); //set fScore
        projectLine(start, destination);
        step(1);
        removeProjectedLine();
        while(!frontier.isEmpty()){
            step(2);
            setCurrent(frontier.remove());
            step(3);
            step(4);
            if(getCurrent() == destination){
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
                    if(!frontier.contains(getNeighbour())){
                        frontier.add(getNeighbour());
                        step(10);
                    }
                    step(11);
                    if(calcGScore(getCurrent(), getNeighbour()) < getNeighbour().getGScore()){
                        getNeighbour().setParentNode(getCurrent());
                        step(12);
                        getNeighbour().setGScore(calcGScore(getCurrent(), getNeighbour()));
                        step(13);
                        getNeighbour().setScore(getNeighbour().getGScore() + nodeDistance(getNeighbour(), destination));
                        projectLine(getNeighbour(), destination);
                        step(14);
                        removeProjectedLine();
                    }
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

    public double calcGScore(Vertex a, Vertex b){
        return a.getGScore()
                + nodeDistance(a, b)
                + getWeight(a, b);
    }

    public double nodeDistance(Vertex a, Vertex b){
        return  Math.sqrt(
                    Math.pow( b.getY() - a.getY(), 2 ) +
                    Math.pow( b.getX() - a.getX(), 2 )
        );
    }

    public String toString(){
        return "A*";
    }
}
