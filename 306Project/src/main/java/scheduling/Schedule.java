package scheduling;

import graph.TaskEdge;
import graph.TaskGraph;
import graph.TaskNode;

import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class shows a partial Schedule to the problem. It will be a node on the Schedule tree
 */
public class Schedule {

    private List<Processor> processors = new ArrayList<Processor>();
    private int cost; //The cost for this Schedule.
    private List<Schedule> children = new ArrayList<Schedule>(); //The children of this Schedule
    private HashSet<TaskNode> schedulableTasks = new HashSet<TaskNode>(); // The tasks that can be scheduled.

    /**
     * Constructor
     *
     * @param processors
     */
    public Schedule(List<Processor> processors) {
        this.processors = processors;

        //Calculating cost
        this.cost = 0;
        for (Processor p : this.processors) {
            if (p.getCost() > cost) {
                this.cost = p.getCost();
            }
        }
    }

    /**
     * Initialises the 'schedulable nodes' list. (i.e. the entry nodes)
     * In the beginning, the only schedulable nodes will be the entry nodes.
     *
     * @return schedulable: the list of schedulable nodes.
     */
    public void initializeSchedulableNodes(TaskGraph tg) {
        HashSet<TaskNode> initialNodes = new HashSet<TaskNode>();
        HashSet<TaskNode> nodes = tg.getNodes();

        for (TaskNode n : nodes) {
            if (n.getIncomingEdges().size() == 0) {
            	initialNodes.add(n);
            }
        }
        this.schedulableTasks = initialNodes;
    }


    public List<Processor> getProcessors() {	
        return processors;
    }
    
    
    /**
     * Updates the schedulable nodes (for after a node get scheduled).
     *
     * @param tn is the node that has just been scheduled.
     */
    public void updateSchedulableNodes(TaskNode tn) {
        schedulableTasks.remove(tn);

        for (TaskEdge e : tn.getOutgoingEdges()) {
            if (e.getEndNode().isSchedulable()) {
                schedulableTasks.add(e.getEndNode());
            }
        }
    }

    /**
     * Return schedulable Nodes
     */
    public HashSet<TaskNode> getSchedulableNodes() {
        return this.schedulableTasks;
    }

    /**
     * Returns all the children of a given partial schedule
     *
     * @param availableNodes
     * @return
     */
    public List<Schedule> createChildren(List<TaskNode> availableNodes) {
        return this.children;
    }

}
