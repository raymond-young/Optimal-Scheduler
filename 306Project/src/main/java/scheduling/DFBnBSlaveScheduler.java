package scheduling;

import graph.TaskGraph;

import java.util.Map;

/**
 * Slave version of DFbnBScheduler to support starting from a partial schedule and notifying master of updates.
 * Written by Kevin.
 */
public class DFBnBSlaveScheduler extends DFBnBScheduler {
    private DFBnBMasterScheduler master;
    private boolean done;

    public DFBnBSlaveScheduler(TaskGraph graph, int processors, Schedule schedule, int upperBound, DFBnBMasterScheduler master, Map<String, Boolean> combinations) {
        super(graph, processors);
        this.master = master;
        this.schedule = schedule;
        this.schedulableNodes = schedule.getSchedulableNodes();
        this.minDepth = schedule.getScheduledNodes().size() - 1;
        this.depth = minDepth;
        this.upperBound = upperBound;
        this.combinations = combinations;
        this.done = false;
    }

    /**
     * Called by master to notify schedule of a new upper bound found from another thread.
     * @param upperBound the new upper bound.
     */

    public void updateUpperBound(int upperBound) {
        if (upperBound < this.upperBound) {
            this.upperBound = upperBound;
        }
    }

    /**
     * Sends notification to master of a potential new optimal schedule.
     */
    @Override
    public void updateSchedule () {
        if (optimalSchedule != null) {
            master.updateSchedule(optimalSchedule, false);
        }
    }

    /**
     * Sends notification to master to update number of branches pruned.
     */
    @Override
    public void updateBranchesPruned (boolean forced) {
        master.updateBranchesPruned(branchesPruned, forced);
    }

    /**
     * Sends notification to master to update number of paths.
     */
    @Override
    public void updateNumPaths (boolean forced) {
        master.updateNumPaths(numPaths);
    }

    /**
     * Sends notification to master to signal that this slave has finished running.
     */
    @Override
    public void finish () {
        this.done = true;
        master.finish();
    }

    public boolean isFinished() {
        return done;
    }


}
