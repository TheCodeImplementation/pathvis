/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package visualiser;

import java.util.ArrayList;

public interface Vertex {
    String getId();
    void setParentNode(Vertex node);
    Vertex getParentNode();
    void setScore(double score);
    double getScore();
    void setGScore(double score);
    double getGScore();
    ArrayList<Vertex> getNeighbours();
    double getX();
    double getY();
}
