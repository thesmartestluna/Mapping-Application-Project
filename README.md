# Web-Mapping-Application-Project
<h4> This project, inspired by Google Map, is a web mapping application from course CS61B in UC Berkeley. </h4>
<h4> I worked with real-world mapping data and developed the back end (i.e. the web server that powers the API that the front end makes requests to) with Maven. </h4>
<p> Users have access to the following features in the application:
  <ul>
    <li> Searching for locations and finding out where the locations are on map </li>
      <p align="center">
        <img src="https://github.com/thesmartestluna/Web-Mapping-Application-Project/blob/main/search.png" width = 700 title="location search">
      </p>
    <li> Finding the shortest path between two points and reading the driving directions for the given route </li>
      <p align="center">
        <img src="https://github.com/thesmartestluna/web-mapping-application-project/blob/main/navigation.png" width = 700 title="navigation">
      </p>
    <li> Selecting autocompleted results in search box </li>
      <p align="center">
        <img src="https://github.com/thesmartestluna/web-mapping-application-project/blob/main/autocomplete.png" width = 700 title="autocomplete">
      </p>
    <li> Zooming in/out and dragging the map aorund to exlore </li>
   </ul>
<h3> Backend Files Description </h3>
   
| Files | Description |
| --- | --- |
| Rasterer | Raster and render map images based on user's requested area and zoom level |
| GraphDB | Build a graph representation with real-world data in Berkeley OSM and clean the graph; Implement autocompletion with Trie data structure |
| GraphBuildingHandler | Handler used by SAX parser to parse Nodes and Ways from Berkeley OSM file |
| Router | Use A* algorithm to find the shortest path between two points; Generate navigation directions based on the shortest path |

<h3> How to run </h3>
<ol>
  <li> Git clone this repository and library-sp18 folder (in CS61B skeleton folder), which contains Open Street Maps images and dataset </li>
  <li> Run with IntelliJ/ Compile in terminal (build system: Maven)</li>
   
    
