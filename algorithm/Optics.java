package algorithm;

import java.io.*;
import java.util.*;
import java.lang.reflect.Array;

import model.*;
import algorithm.*;

/**
 * Implementation of OPTICS.
 */
public class Optics extends Algorithm
{

    // DISTANCE_MANHATTAN, DISTANCE_EUCLIDIAN or DISTANCE_EUCLIDIAN_SQ
    // Euclidian Sq is the same as Euclidian, but does not take the square root,
    // but instead squares the epsilon parameter. Thus it is a lot faster.
    public final static int DISTANCE_METRIC = Calculations.DISTANCE_MANHATTAN;

    /**
     * Seeds queue.
     *
     * This is a min-PriorityQueue.
     */
    PriorityQueue<AlgorithmPoint> seeds;

    /**
     * Map of points.
     */
    ArrayList<AlgorithmPoint> points;

    /**
     * Parameters.
     */
    double epsilon;
    int minPts;

    /**
     * Cluster ID.
     */
    int clusterId;


    /**
     * Constructor.
     */
    public Optics()
    {
        super();
    }

    /**
     * Find the parameters
     *
     * @param ci Minimum number of clusters
     * @param cj Maximum number of clusters
     * @param n Number of points
     */
    public void findParameters(int ci, int cj, int n)
    {
        super.findParameters(ci, cj, n);

        this.epsilon = 10;
        this.minPts = 10;
        this.minClusterSize = 10;

        if (DISTANCE_METRIC == Calculations.DISTANCE_EUCLIDIAN_SQ) {
            epsilon = epsilon * epsilon;
        }
    }

    public void run()
    {
        points = new ArrayList<AlgorithmPoint>();

        reachabilityPlot = new ArrayList<AlgorithmPoint>(field.size());

        clusterId = 1;

        for (Point p : field.getAllPoints()) {
            AlgorithmPoint op = new AlgorithmPoint(p);
            points.add(op);
        }

        // just temporary
        Collections.shuffle(points);

        // initialize the Priority Queue
        seeds = new PriorityQueue<AlgorithmPoint>();

        for (AlgorithmPoint p : points) {
            if (p.isProcessed()) {
                continue;
            }
            p.process();

            expandClusterOrder(p);

            clusterId++;
        }

        clusterId--;

        cluster();
    }

    void expandClusterOrder(AlgorithmPoint p)
    {
        Pair<List<PrioPair<AlgorithmPoint,Double>>, List<PrioPair<AlgorithmPoint,Double>>> nn = getNeighbours(p);
        List<PrioPair<AlgorithmPoint,Double>> N = nn.getV();

        // set the cluster
        //p.setCluster(clusterId);

        write(p);

        if (coreDistance(N, p) != UNDEFINED) {
            update(N, p);

            AlgorithmPoint q;

            while ((q = seeds.poll()) != null) {
                Pair<List<PrioPair<AlgorithmPoint,Double>>, List<PrioPair<AlgorithmPoint,Double>>> nn_ = getNeighbours(p);
                List<PrioPair<AlgorithmPoint,Double>> N_ = nn_.getV();
                q.process();

                //q.setCluster(clusterId);

                write(q);

                if (coreDistance(N, q) != UNDEFINED) {
                    update(N_, q);
                }
            }
        }
    }

    double coreDistance(List<PrioPair<AlgorithmPoint,Double>> N, AlgorithmPoint p)
    {
        if (N.size() >= minPts) {
            return N.get(0).getV().doubleValue();
        }
        return UNDEFINED;
    }

    /**
     * O(n) finding of the neighbour.
     *
     * @return A pair of lists, the first one contains the epsilon distances and the
     *         second one all the points on minPts distance
     */
    Pair<List<PrioPair<AlgorithmPoint,Double>>, List<PrioPair<AlgorithmPoint,Double>>> getNeighbours(AlgorithmPoint p)
    {
        List<PrioPair<AlgorithmPoint,Double>> list = new ArrayList<PrioPair<AlgorithmPoint,Double>>();
        List<PrioPair<AlgorithmPoint,Double>> nextNeighbours = new ArrayList<PrioPair<AlgorithmPoint,Double>>();

        PriorityQueue<PrioPair<AlgorithmPoint,Double>> pq = new PriorityQueue<PrioPair<AlgorithmPoint,Double>>();

        for (AlgorithmPoint q : points) {
            if (q == p) {
                continue;
            }

            double dist = Calculations.distance(p.getPoint(), q.getPoint(), DISTANCE_METRIC);

            PrioPair<AlgorithmPoint,Double> pair = new PrioPair<AlgorithmPoint,Double>(q, dist);

            if (dist <= epsilon) {
                list.add(pair);
            }

            // add the pair
            if (pq.size() < minPts) {
                pq.add(pair);
            } else {
                if (dist < ((Double) pq.peek().getV())) {
                    // remove the highest element
                    pq.poll();
                    pq.add(pair);
                }
            }
        }

        for (PrioPair<AlgorithmPoint,Double> pair : pq) {
            nextNeighbours.add(pair);
        }

        return new Pair<List<PrioPair<AlgorithmPoint,Double>>, List<PrioPair<AlgorithmPoint,Double>>>(list, nextNeighbours);
    }

    /**
     * Update method
     */
    void update(List<PrioPair<AlgorithmPoint,Double>> N, AlgorithmPoint p)
    {
        double coredist = coreDistance(N, p);

        for (PrioPair<AlgorithmPoint,Double> pair : N) {
            AlgorithmPoint o = pair.getT();
            if (!pair.getT().isProcessed()) {
                double newReachabilityDistance = Math.max(coredist, Calculations.distance(o.getPoint(), p.getPoint(), DISTANCE_METRIC));
                //System.out.println("rea: " + newReachabilityDistance);

                if (o.getReachabilityDistance() == UNDEFINED) {
                    o.setReachabilityDistance(newReachabilityDistance);
                    seeds.add(o);
                } else {
                    if (newReachabilityDistance < o.getReachabilityDistance()) {
                        seeds.remove(o);
                        seeds.add(o);
                    }
                }
            }
        }
    }

    /**
     * Write.
     */
    void write(AlgorithmPoint op)
    {
        if (op.getReachabilityDistance() != UNDEFINED) {
            op.setX(reachabilityPlot.size());
            reachabilityPlot.add(op);
        }
    }

    class Pair<T,V>
    {
        T t;
        V v;

        public Pair(T t, V v)
        {
            this.t = t;
            this.v = v;
        }

        public T getT()
        {
            return t;
        }

        public V getV()
        {
            return v;
        }
    }

    class PrioPair<T,V extends Comparable<V>> implements Comparable<PrioPair<T,V>>
    {
        T t;
        V v;

        public PrioPair(T t, V v)
        {
            this.t = t;
            this.v = v;
        }

        public T getT()
        {
            return t;
        }

        public V getV()
        {
            return v;
        }

        public int compareTo(PrioPair<T,V> c)
        {
            // to reverse the priority queue
            return v.compareTo(c.getV()) * -1;
        }
    }
}
