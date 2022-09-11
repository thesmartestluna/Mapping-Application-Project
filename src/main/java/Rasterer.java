import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private static final double root_ullon = MapServer.ROOT_ULLON;
    private static final double root_lrlon = MapServer.ROOT_LRLON;
    private static final double root_ullat = MapServer.ROOT_ULLAT;
    private static final double root_lrlat = MapServer.ROOT_LRLAT;
    private static final double tile_size = MapServer.TILE_SIZE;
    private static boolean query_success = true;

    public Rasterer() {
        // YOUR CODE HERE
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
//     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */


    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // System.out.println(params);
        Map<String, Object> results = new HashMap<>();

        /*Inputs of query*/
        double ullon = params.get("ullon");
        double lrlon = params.get("lrlon");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double width = params.get("w");
        int depth = findDepth((lrlon - ullon)/width);

        /*Find the indices of pictures returned*/
        double picWidth = (root_lrlon - root_ullon) / Math.pow(2, depth);
        double picHeight = (root_ullat - root_lrlat) / Math.pow(2, depth);

        int ul_lon_index = (int) Math.floor((ullon - root_ullon) / picWidth);
        int lr_lon_index = (int) Math.floor((lrlon - root_ullon) / picWidth);
        int ul_lat_index = (int) Math.floor((root_ullat - ullat) / picHeight);
        int lr_lat_index = (int) Math.floor((root_ullat - lrlat) / picHeight);

        /* Fill the render_grid with strings of pictures.*/
        String[][] render_grid = new String[lr_lat_index - ul_lat_index + 1][lr_lon_index - ul_lon_index + 1];
        for (int i = 0; i < lr_lat_index - ul_lat_index + 1; i++){
            for (int j = 0; j < lr_lon_index - ul_lon_index + 1; j++){
                render_grid[i][j] = "d" + depth + "_x" + (j + ul_lon_index) + "_y" + (i + ul_lat_index) + ".png";
            }
        }

        /* Find the four bounding corners*/
        double raster_ul_lon = root_ullon + ul_lon_index * picWidth;
        double raster_lr_lon = root_ullon + (lr_lon_index + 1) * picWidth;
        double raster_ul_lat = root_ullat - ul_lat_index * picHeight;
        double raster_lr_lat = root_ullat - (lr_lat_index + 1) * picHeight;

        /*Check boundary.*/
        if ((ullon >= root_lrlon) || (lrlon <= root_ullon) || (ullat <= root_lrlat) || (lrlat >= root_ullat) || (ullon > lrlon) || (ullat < lrlat)){
            query_success = false;
        }

        /* Put query results into the results map.*/
        results.put("render_grid", render_grid);
        results.put("raster_ul_lon", raster_ul_lon);
        results.put("raster_ul_lat", raster_ul_lat);
        results.put("raster_lr_lon", raster_lr_lon);
        results.put("raster_lr_lat", raster_lr_lat);
        results.put("depth", depth);
        results.put("query_success", query_success);

        return results;
    }

    /* Find the depth of the query.*/
    private int findDepth(double query_lonDPP){
        double depth_exact = Math.log((root_lrlon - root_ullon)/(query_lonDPP * tile_size)) / Math.log(2);
        int depth = (int) Math.min((int) Math.ceil(depth_exact), 7);
        return depth;
    }

}
