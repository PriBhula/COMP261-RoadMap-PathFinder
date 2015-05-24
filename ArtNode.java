import java.util.ArrayDeque;


public class ArtNode {

	private Node node;
	private ArtNode parent;
	private double depth;
	private double reachBack;
	private ArrayDeque<Node> children;

	public ArtNode(Node node, double depth, ArtNode parent){
		this.node = node;
		this.depth = depth;
		this.parent = parent;
	}

	public Node getNode(){
		return node;
	}

	public ArtNode getParent(){
		return parent;
	}
	
	public void addChild(Node child){
		children.offer(child);
	}
	
	public void addChildren(ArrayDeque<Node> children){
		this.children = children;
	}

	public ArrayDeque<Node> getChildren(){
		return children;
	}
	
	public void setReachBack(double reach){
		reachBack = reach;
	}

	public double getReachBack(){
		return reachBack;
	}
	
	public double getDepth(){
		return depth;
	}
}
