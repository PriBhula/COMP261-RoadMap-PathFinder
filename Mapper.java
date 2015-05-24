import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author Tony Butler-Yeoman
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;

	public List <Node> clickedNodes = new ArrayList <Node>();
	public Set <Road> toHighlight = new HashSet<Road>();
	public List<Segment>toHLight;
	public List<Segment> selSeg = new ArrayList<Segment>();
	public HashSet<Node> mapNodes;
	public double finalCost=0;

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		toHighlight.clear();
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
				closest.toString();
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}

		/*
		 * Calls the Articulation Points method. but it doesnt work :(
		 */
		/*Node root = closest;
		System.out.print("FINDING ART POINTS");
		articulationPoints(null, 0, root);
		System.out.print("FOUND ART POINTS");*/



		if (clickedNodes.size()==0){//if there are no clickedNodes, add closest
			clickedNodes.add(closest);
			return;
		}
		else if (clickedNodes.size()==1){//if there is one already, add another and find a path
			clickedNodes.add(closest);
			//System.out.println("FIND");
			pathfinder(clickedNodes.get(0),clickedNodes.get(1));
			graph.highlightedSegs = selSeg;
			graph.setHighlightS(selSeg);
			graph.setHighlightN(clickedNodes);
			redraw();
			List <String> roadNames = new ArrayList<String>();
			for (Segment s:selSeg){
				//double length = Math.round(s.length*100)/100;//trying to shorten the lengths to 2dp
				roadNames.add(s.road.name+ " "+s.length+"km ->");
			}		
			//System.out.println(roadNames);
			getTextOutputArea().setText("Distance: "+finalCost + " Roads: "+roadNames+ " GOAL REACHED");
			//System.out.print("YO PRINT G");
		}
		else if(clickedNodes.size()==2){//if there are two, then clear the array and start adding again
			clickedNodes.clear();
			clickedNodes.add(closest);
		}
	}



	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	/*
	 * A* algorithm	
	 */
	public void pathfinder(Node start, Node goal){
		finalCost = Double.POSITIVE_INFINITY;
		for (Node n:graph.nodes.values()){//initialise all the nodes
			//System.out.println("RESET");
			n.setVisit(false);
			n.setParent(null);
			n.setHeuristic(Double.POSITIVE_INFINITY);
		}
		Fringe startF = new Fringe(start, null, 0,estimate(start, goal));
		PriorityQueue<Fringe> fringe = new PriorityQueue<Fringe>();//start a new fringe
		fringe.offer(startF);//add the first fringe
		while(!fringe.isEmpty()){
			Fringe current = fringe.poll();
			Node node = current.getCurrent();
			Node prev = current.getPrev();
			double costToHere = current.getCost();
			double totalCostToEnd = current.getTotalCost();

			if(costToHere<node.getHeuristic()){//if the cost is less than the dist(better route)
				node.best = prev;
				node.setHeuristic(costToHere);
				if (!node.isVisited()){
					node.setVisit(true);
					node.setParent(prev);
					node.setHeuristic(costToHere);
				}
				if(node==goal){
					//System.out.println("FOUND!!!");
					finalCost = costToHere;
					returnPath(current);
				}
			}
			for (Node neighbour: node.getNeighbours()){//go through all neighbours
				double costToN = costToHere+node.getBetween(neighbour).length;
				if(costToN < neighbour.getHeuristic()){
					double estTotal = costToN+estimate(neighbour, goal);
					if(estTotal < finalCost){
						Fringe newF = new Fringe(neighbour,node,costToN,estTotal);
						fringe.offer(newF);
						returnPath(newF);
					}
					if(neighbour==goal){
						finalCost = costToN;
					}
				}
			}
		}
	}		

	public Collection<Segment> returnPath(Fringe current) {
		//System.out.println(n.toString());
		selSeg = new ArrayList<Segment>();
		Node goal = current.current;
		Node start = current.prev;
		while(start!=null){
			selSeg.add(goal.getBetween(start));//get the segments inbetween by iterating through all the neighbouring segments
			Node temp = start;
			start = start.best;
			goal = temp;
		}
		return selSeg;
	}

	public double estimate(Node start,Node goal){
		return Math.abs((start.getLocation().x - goal.getLocation().x)+Math.abs(start.getLocation().y-goal.getLocation().y));

	}

	public HashSet<Node> articulationPoints(Node start,int depth,Node root){
		//System.out.println("ONE");
		for (Node n: graph.nodes.values()){
			n.setVisit(false);
			n.setParent(null);
		}
		Stack<ArtNode> stack = new Stack<ArtNode>();
		ArtNode first = new ArtNode(root,0,null);
		stack.push(first);
		while(!stack.isEmpty()){
			ArtNode a = stack.peek();
			Node node = a.getNode();
			if(a.getChildren()==null){
				node.setVisit(true);
				node.setDepth(a.getDepth());
				a.setReachBack(a.getDepth());
				a.addChildren(new ArrayDeque<Node>());
				for(Node neighbour: node.getNeighbours()){
					if(!neighbour.equals(a.getParent().getNode())){
						a.getChildren().offer(neighbour);
					}
				}
			}
			else if (a.getChildren().isEmpty()){
				Node child = a.getChildren().poll();
				if(child.isVisited()){
					a.setReachBack(Math.min(a.getReachBack(),child.getDepth()));
				}
				else{
					stack.push(new ArtNode(child,node.getDepth()+1,a));
				}
			}
			else{
				if(node.getDepth()>1){
					if(a.getReachBack()>a.getParent().getDepth()){
						mapNodes.add(a.getParent().getNode());
					}
					a.getParent().setReachBack(Math.min(a.getParent().getReachBack(), a.getReachBack()));
				}
				stack.pop();
			}
		}
		System.out.print(mapNodes);
		return mapNodes;
	}


	public static void main(String[] args) {
		new Mapper();
	}
}

// code for COMP261 assignments