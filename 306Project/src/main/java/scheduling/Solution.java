package scheduling;

import java.util.ArrayList;
import java.util.List;

/**
 * This class shows a partial solution to the problem. It will be a node on the solution tree
 */
public class Solution {

    private List<Processor> processors = new ArrayList<Processor>();
    private int cost; //The cost for this solution
    private List<Solution> children = new ArrayList<Solution>(); //The children of this solution

    public Solution(List<Processor> processors) {
        this.processors = processors;

        //Calculating cost
        cost = 0;
        for (Processor p : this.processors) {
            if (p.getCost() > cost) {
                this.cost = p.getCost();
            }
        }
    }
}