import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     *
     * @param g       The graph to use.
     * @param stlon   The longitude of the start location.
     * @param stlat   The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */

    private static GraphDB.Node start;
    private static GraphDB.Node destination;
    private static class routerNode implements Comparable<routerNode>{
        public long id;
        public routerNode pre;
        public double priority;
        public double distFromStart;
        public routerNode(Long id, routerNode pre, double distFromStart){
            this.id = id;
            this.pre = pre;
            this.distFromStart = distFromStart;
            this.priority = distFromStart + distToDest(id);
        }
        @Override
        public int compareTo(routerNode v){
//            return (int) (this.priority - v.priority);
            if (this.priority < v.priority) {
                return -1;
            }
            if (this.priority > v.priority) {
                return 1;
            }
            return 0;
        }
    }
    private static double distToDest(long id){
        return GraphDB.distance(GraphDB.lon(id), GraphDB.lat(id), destination.lon, destination.lat);
    }
    private static boolean isGoal(routerNode v){
        return distToDest(v.id) == 0;
    }

    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {

        start = g.nodes.get(g.closest(stlon, stlat));
        destination = g.nodes.get(g.closest(destlon, destlat));
        PriorityQueue<routerNode> pq = new PriorityQueue<>();
        Map<Long, Boolean> visited = new HashMap<>();
        pq.add(new routerNode(start.id, null, 0));
        while (!isGoal(pq.peek())){
            routerNode minNode = pq.poll();
            visited.put(minNode.id, true);
            for (long neighborId : g.adjacent(minNode.id)){
                if ((!visited.containsKey(neighborId))){
                    pq.add(new routerNode(neighborId, minNode, minNode.distFromStart + GraphDB.distance(neighborId, minNode.id)));
                }
            }
        }
        Stack<routerNode> solution = new Stack<>();
        List<Long> solutions = new LinkedList<>();
        routerNode Node = pq.peek();
        while (Node != null){
            solution.push(Node);
            Node = Node.pre;
        }
        while (!solution.isEmpty()){
            solutions.add(solution.pop().id);
        }
        return solutions;
    }

        /**
         * Create the list of directions corresponding to a route on the graph.
         * @param g The graph to use.
         * @param route The route to translate into directions. Each element
         *              corresponds to a node from the graph in the route.
         * @return A list of NavigatiionDirection objects corresponding to the input
         * route.
         */
        public static List<NavigationDirection> routeDirections (GraphDB g, List < Long > route){
            double preBearing = 0;
            double distance = 0;
            List<NavigationDirection> directions = new LinkedList<>();
            String currWayName = CurrWayName(g, route.get(0), route.get(1));
            int currDirection = NavigationDirection.START;
            for (int i = 1; i < route.size(); i++) {
                long preNode = route.get(i - 1);
                long currNode = route.get(i);
                double currBearing = g.bearing(preNode, currNode);
                double relativeBearing = currBearing - preBearing;
                if ((g.getWayNames(currNode).contains(currWayName)) && (i != route.size() - 1)){
                    distance += GraphDB.distance(preNode, currNode);
                    preBearing = currBearing;
                    continue;
                }
                if (i != route.size() - 1){
                    distance += GraphDB.distance(preNode, currNode);
                }
                NavigationDirection nd = new NavigationDirection();
                nd.direction = currDirection;
                nd.way = currWayName;
                nd.distance = distance;
                directions.add(nd);
                currWayName = CurrWayName(g, preNode, currNode);
                currDirection = getDirection(relativeBearing);
                distance = GraphDB.distance(preNode, currNode);
            }
            return directions;
        }

        private static String CurrWayName(GraphDB g, long v, long w) {
            for (String a : g.getWayNames(v)) {
                for (String b : g.getWayNames(w)) {
                    if (a.equals(b)) {
                        return a;
                    }
                }
            }
            return "";
        }

    private static int getDirection(double relativeBearing) {
        double absBearing = Math.abs(relativeBearing);
        if (absBearing > 180) {
            absBearing = 360 - absBearing;
            relativeBearing *= -1;
        }
        if (absBearing <= 15) {
            return NavigationDirection.STRAIGHT;
        }
        if (absBearing <= 30) {
            return relativeBearing < 0 ? NavigationDirection.SLIGHT_LEFT : NavigationDirection.SLIGHT_RIGHT;
        }
        if (absBearing <= 100) {
            return relativeBearing < 0 ? NavigationDirection.LEFT : NavigationDirection.RIGHT;
        }
        else {
            return relativeBearing < 0 ? NavigationDirection.SHARP_LEFT : NavigationDirection.SHARP_RIGHT;
        }
    }



    /**
         * Class to represent a navigation direction, which consists of 3 attributes:
         * a direction to go, a way, and the distance to travel for.
         */
        public static class NavigationDirection {

            /** Integer constants representing directions. */
            public static final int START = 0;
            public static final int STRAIGHT = 1;
            public static final int SLIGHT_LEFT = 2;
            public static final int SLIGHT_RIGHT = 3;
            public static final int RIGHT = 4;
            public static final int LEFT = 5;
            public static final int SHARP_LEFT = 6;
            public static final int SHARP_RIGHT = 7;

            /** Number of directions supported. */
            public static final int NUM_DIRECTIONS = 8;

            /** A mapping of integer values to directions.*/
            public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

            /** Default name for an unknown way. */
            public static final String UNKNOWN_ROAD = "unknown road";

            /** Static initializer. */
            static {
                DIRECTIONS[START] = "Start";
                DIRECTIONS[STRAIGHT] = "Go straight";
                DIRECTIONS[SLIGHT_LEFT] = "Slight left";
                DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
                DIRECTIONS[LEFT] = "Turn left";
                DIRECTIONS[RIGHT] = "Turn right";
                DIRECTIONS[SHARP_LEFT] = "Sharp left";
                DIRECTIONS[SHARP_RIGHT] = "Sharp right";
            }

            /** The direction a given NavigationDirection represents.*/
            int direction;
            /** The name of the way I represent. */
            String way;
            /** The distance along this way I represent. */
            double distance;

            /**
             * Create a default, anonymous NavigationDirection.
             */
            public NavigationDirection() {
                this.direction = STRAIGHT;
                this.way = UNKNOWN_ROAD;
                this.distance = 0.0;
            }

            public String toString() {
                return String.format("%s on %s and continue for %.3f miles.",
                        DIRECTIONS[direction], way, distance);
            }

            /**
             * Takes the string representation of a navigation direction and converts it into
             * a Navigation Direction object.
             * @param dirAsString The string representation of the NavigationDirection.
             * @return A NavigationDirection object representing the input string.
             */
            public static NavigationDirection fromString(String dirAsString) {
                String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(dirAsString);
                NavigationDirection nd = new NavigationDirection();
                if (m.matches()) {
                    String direction = m.group(1);
                    if (direction.equals("Start")) {
                        nd.direction = NavigationDirection.START;
                    } else if (direction.equals("Go straight")) {
                        nd.direction = NavigationDirection.STRAIGHT;
                    } else if (direction.equals("Slight left")) {
                        nd.direction = NavigationDirection.SLIGHT_LEFT;
                    } else if (direction.equals("Slight right")) {
                        nd.direction = NavigationDirection.SLIGHT_RIGHT;
                    } else if (direction.equals("Turn right")) {
                        nd.direction = NavigationDirection.RIGHT;
                    } else if (direction.equals("Turn left")) {
                        nd.direction = NavigationDirection.LEFT;
                    } else if (direction.equals("Sharp left")) {
                        nd.direction = NavigationDirection.SHARP_LEFT;
                    } else if (direction.equals("Sharp right")) {
                        nd.direction = NavigationDirection.SHARP_RIGHT;
                    } else {
                        return null;
                    }

                    nd.way = m.group(2);
                    try {
                        nd.distance = Double.parseDouble(m.group(3));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    return nd;
                } else {
                    // not a valid nd
                    return null;
                }
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof NavigationDirection) {
                    return direction == ((NavigationDirection) o).direction
                            && way.equals(((NavigationDirection) o).way)
                            && distance == ((NavigationDirection) o).distance;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(direction, way, distance);
            }
        }
    }

