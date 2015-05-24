import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author Tony Butler-Yeoman
 */
public class Node implements Comparable<Node>{

	public final int nodeID;
	public final Location location;
	public final Collection<Segment> segments;
	public boolean visited;
	public Node prev;
	public double heuristic;
	public double depth = Double.POSITIVE_INFINITY;
	public List<Segment> adj = new ArrayList<Segment>();
	public Node best;
	public List<Segment> outN = new ArrayList<Segment>();
	public List<Segment> inN = new ArrayList<Segment>();


	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<Segment>();
	}

	public void addSegment(Segment seg) {
		segments.add(seg);
	}

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}
	
	public void setVisit(boolean visit){
		this.visited = visit;
	}
	public boolean isVisited(){
		return this.visited;
	}

	public void setParent(Node n){
		this.prev = n;
	}

	public Node getParent(){
		return prev;
	}

	public void setHeuristic(double heur){
		this.heuristic = heur;
	}

	public double getHeuristic(){
		return this.heuristic;
	}

	public void setDepth(double dep){
		this.depth = dep;
	}

	public double getDepth(){
		return this.depth;
	}

	public void addToAdj(Segment s){
		this.adj.add(s);
	}

	public Location getLocation(){
		return location;
	}
	public ArrayList<Segment> getAdj(){
		return (ArrayList<Segment>) adj;
	}
	public ArrayList<Node> getNeighbours() {
		ArrayList<Node> neighbours = new ArrayList<Node>();
		for (Segment s:getOutNeighbours()){
			neighbours.add(s.getNeighbours(this));
		}
		return neighbours;
	}

	public void addInSegment(Segment s){
		inN.add(s);
	}	
	public void addOutSegment(Segment s){
		outN.add(s);
	}	

	public List<Segment> getOutNeighbours(){
		return outN;
	}	

	public List<Segment> getInNeighbours(){
		return inN;
	}	 

	public double costToNode(){
		return 0;
	}

	public void setCostToHere(double d) {
		this.heuristic += d;
	}

	public Segment getBetween(Node other) {
		for (Segment s : this.adj) {
			for (Segment os : other.adj) {
				if (s.equals(os))
					return s;
			}
		}
		return null;
	}


	public int compareTo(Node other) {
		return (int) ((this.costToNode()+this.getHeuristic()) - (other.costToNode()+other.getHeuristic()));
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location +  "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}


}

// code for COMP261 assignments