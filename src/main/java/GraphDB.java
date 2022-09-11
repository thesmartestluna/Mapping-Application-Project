import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /**
     * Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc.
     */
    public class Node {
        public long id;
        public double lon;
        public double lat;
        public String name;
        public List<Long> adj;

        public List<String> ways;

        private Node(long id, double lon, double lat) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            this.ways = new ArrayList<>();
            this.adj = new LinkedList<>();
        }
    }

//    public class Edge{
//        private long v;
//        private long w;
//        private String name;
//        private double weight;
//
//        private Edge(long v, long w, String name){
//            this.v = v;
//            this.w = w;
//            this.name = name;
//        }
//    }

    /* All connected nodes*/
    public static Map<Long, Node> nodes = new HashMap<>();
    /* All nodes*/
    public static Map<Long, Node> locations = new HashMap<>();
    /* Location names and their corresponding list of ids.*/
    public static Map<String, ArrayList<Long>> names = new HashMap<>();
    /* For autocomplete purposes*/
    public final TrieST trie = new TrieST();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        Iterator<Map.Entry<Long, Node>> entries = nodes.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Long, Node> entry = entries.next();
            if (entry.getValue().adj.isEmpty()) {
//                nodes.remove(entry.getKey());
                entries.remove();
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     *
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        checkVertex(v);
        return nodes.get(v).adj;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    static double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double shortestDist = Double.MAX_VALUE;
        long closestID = -1;
        for (long id : nodes.keySet()) {
            double distance = distance(nodes.get(id).lon, nodes.get(id).lat, lon, lat);
            if (distance < shortestDist) {
                shortestDist = distance;
                closestID = id;
            }
        }
        return closestID;
    }

    /**
     * Gets the longitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    static double lon(long v) {
        checkVertex(v);
        return nodes.get(v).lon;
    }

    /**
     * Gets the latitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    static double lat(long v) {
        checkVertex(v);
        return nodes.get(v).lat;
    }

    void addNode(long id, double lon, double lat) {
        Node node = new Node(id, lon, lat);
        nodes.put(id, node);
        locations.put(id, node);
    }

    void addEdge(long v, long w) {
        checkVertex(v);
        checkVertex(w);
        nodes.get(v).adj.add(w);
        nodes.get(w).adj.add(v);
//        System.out.println(v);
//        System.out.println(w);
    }

    /* Connect all edges in a way and give names to ways.*/
    void addWay(ArrayList<Long> way, String wayName) {
        nodes.get(way.get(0)).ways.add(wayName);
        for (int i = 1; i < way.size(); i++) {
            nodes.get(way.get(i)).ways.add(wayName);
            addEdge(way.get(i - 1), way.get(i));
        }

    }

    /* Give names to nodes.*/
    void addName(long id, double lon, double lat, String locName) {
        nodes.get(id).name = locName;
        locations.get(id).name = locName;
        String cleanName = cleanString(locName);
        if (!names.containsKey(cleanName)) {
            names.put(cleanName, new ArrayList<>());
        }
        names.get(cleanName).add(id);
        trie.put(cleanName, id);
    }

    /* Get the name of ways*/
    List<String> getWayNames(long v) {
        checkVertex(v);
        List<String> wayNames = new LinkedList<>();
        for (String way : nodes.get(v).ways) {
            wayNames.add(way);
        }
        return wayNames;
    }

    /* For searching purposes.*/
    ArrayList<Long> getLocations(String name) {
        return names.get(cleanString(name));
    }

    static void checkVertex(long v) {
        if (!nodes.containsKey(v)) {
            throw new IllegalArgumentException("Vertex " + v + " is not in the graph");
        }
    }

    List<String> keysWithPrefixOf(String prefix) {
        List<String> locNames = new ArrayList<>();
        for (Object key : trie.keysWithPrefix(cleanString(prefix))) {
            for (Long id : names.get(key)) {
                locNames.add(locations.get(id).name);
            }
        }
        return locNames;
    }
}
