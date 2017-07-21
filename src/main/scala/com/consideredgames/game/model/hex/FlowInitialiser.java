package com.consideredgames.game.model.hex;

import scala.collection.JavaConversions;
import scala.util.Random;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Sets up the flow on all the rivers which are assumed to be all connected. This is a use once then throw away class, as the class state is not
 * cleared down (no need to setup the flow twice!).
 *
 * @author matt
 */
// TODO convert to scala and use LazyLogging
public class FlowInitialiser {

    private List<Map<RiverSegment, Point>> onHold = new ArrayList<>();
    /**
     * Needs to be concurrent because modify it more than once in an iteration - with remove, and add to onHoldOrActive TODO fix..
     */
    private Map<RiverSegment, Point> active = new ConcurrentHashMap<>();
    private Map<RiverSegment, Point> onHoldSingle = new HashMap<>();

    /**
     * Sets up the flow for each segment of the provided the rivers. Done in such a way as to define flows that make sense, that is, not suddenly
     * changing direction for no reason.
     *
     * @param rivers The list of rivers which form the river network for the game.
     */
    public final void setup(List<RiverSegment> rivers, Random random) {
        // start with a segment

        List<RiverSegment> noFlowRivers = getRiversWithoutFlow(rivers);

        while (!noFlowRivers.isEmpty()) {

            // use one of the rivers without flow to start a new chain.
            RiverSegment river = noFlowRivers.get(0);
            river.setFlowUsingFrom(river.hexA().vertices().get(
                    (random.nextBoolean() ? river.sideA().clockwiseVertex() : river.sideA().anticlockwiseVertex())).get());

//            logger.debug("River set flow: " + river.toString() + " and fromPoint: " + river.hexA().vertices().get(river.sideA().clockwiseVertex()).get());

            addNeighboursToOnHoldOrActive(river);

            while (!onHold.isEmpty() || !active.isEmpty() || !onHoldSingle.isEmpty()) {
                if (!active.isEmpty()) {

                    Iterator<Map.Entry<RiverSegment, Point>> activeIterator = active.entrySet().iterator();
                    while (activeIterator.hasNext()) {
                        Map.Entry<RiverSegment, Point> entry = activeIterator.next();
                        RiverSegment tempRiver = entry.getKey();
                        Point fromPoint = entry.getValue();
                        tempRiver.setFlowUsingFrom(fromPoint);

//                        logger.debug("River set flow: " + tempRiver.toString() + " and fromPoint: " + fromPoint);

                        addNeighboursToOnHoldOrActive(tempRiver);
                        activeIterator.remove();
                    }

                } else if (!onHold.isEmpty()) {
                    boolean decisionMade, decisionMadeTemp;
                    decisionMade = false;

                    for (Iterator<Map<RiverSegment, Point>> entryIterator = onHold.iterator(); entryIterator.hasNext(); ) {
                        Map<RiverSegment, Point> entry = entryIterator.next();
                        decisionMadeTemp = reEvaluate(entry);
                        // switch decisionMade
                        if (decisionMadeTemp && !decisionMade) {
                            decisionMade = true;
                        }
                        if (entry.isEmpty()) {
                            entryIterator.remove();
                        }
                    }

                    if (!decisionMade) {
                        // reEvaluation had no effect, so need to make a
                        // decision
                        Map<RiverSegment, Point> riverPoints = onHold.remove(0);
                        for (Map.Entry<RiverSegment, Point> entry : riverPoints.entrySet()) {
                            if (!decisionMade) {
                                // the first entry will go forward
                                active.put(entry.getKey(), entry.getValue());
                                decisionMade = true;
                            } else {
                                // the second entry stays on hold for now
                                onHoldSingle.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }

                } else {
                    // onHoldSingle
                    // randomly choose a direction for the flow, then delegate
                    // to active.

                    Point point;
                    Map.Entry<RiverSegment, Point> entry = getFirstEntry(onHoldSingle.entrySet());
                    onHoldSingle.remove(entry.getKey());
                    river = entry.getKey();
                    if (random.nextInt(2) == 0) {
                        Point fromPoint = entry.getValue();
                        point = river.getOtherPoint(fromPoint).get();
                    } else {
                        point = entry.getValue();
                    }
                    active.put(river, point);
                }
            }

            noFlowRivers = getRiversWithoutFlow(rivers);
        }
    }

    /**
     * This could be improved - currently flawed in that it can leave one segment in if the flow has been set on the second one and not the first. But
     * this will get sorted out on the second pass.
     *
     * @param riverPointMap
     * @return
     */
    private boolean reEvaluate(Map<RiverSegment, Point> riverPointMap) {
        boolean decisionMade = false;

        for (Iterator<Map.Entry<RiverSegment, Point>> riverPointItr = riverPointMap.entrySet().iterator(); riverPointItr.hasNext(); ) {
            Map.Entry<RiverSegment, Point> riverPoint = riverPointItr.next();
            RiverSegment river = riverPoint.getKey();

            if (river.hasFlow()) {
                // flow has now been set so no longer onHold
                riverPointItr.remove();
            } else if (riverPointMap.size() == 1) {
                // if we've removed an entry but not this one, then this is the
                // only one (2 entries per map)
                // so its no longer a decision case and instead add to active.
                active.put(river, riverPoint.getValue());
                decisionMade = true;
                riverPointItr.remove();
            }
        }
        return decisionMade;
    }

    private void addToOnHoldOrActive(Map<RiverSegment, Point> neighbours, boolean inFlowDirection) {

        if (neighbours.size() == 1) {
            Map.Entry<RiverSegment, Point> entry = getFirstEntry(neighbours.entrySet());
            RiverSegment tempRiver = entry.getKey();

            Point point;
            if (inFlowDirection) {
                point = tempRiver.getOtherPoint(entry.getValue()).get();
            } else {
                point = entry.getValue();
            }

            // don't add if already added
            if (!active.keySet().contains(tempRiver)) {
                active.put(tempRiver, point);
            }

        } else if (neighbours.size() == 2) {
            onHold.add(neighbours);
//            logger.debug("Rivers added to onHold: " + neighbours.entrySet());
        }
    }

    private void addNeighboursToOnHoldOrActive(RiverSegment river) {

        Map<RiverSegment, Point> neighbours = JavaConversions.mapAsJavaMap(river.getNeighboursWithoutFlow(true));
        addToOnHoldOrActive(neighbours, true);
        neighbours = JavaConversions.mapAsJavaMap(river.getNeighboursWithoutFlow(false));
        addToOnHoldOrActive(neighbours, false);
    }

    private List<RiverSegment> getRiversWithoutFlow(List<RiverSegment> rivers) {
        List<RiverSegment> noFlowRivers = new ArrayList<>();
        for (RiverSegment river : rivers) {
            if (!river.hasFlow()) {
                noFlowRivers.add(river);
            }
        }
        return noFlowRivers;
    }

    private static Map.Entry<RiverSegment, Point> getFirstEntry(Set<Map.Entry<RiverSegment, Point>> entrySet) {
        Iterator<Map.Entry<RiverSegment, Point>> iterator = entrySet.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

}