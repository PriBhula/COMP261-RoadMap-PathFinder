
public class Fringe implements Comparable<Object> {
	public Node current;
	public Node prev;
	public double costToHere;
	public double totalCost;
	
	public Fringe(Node node,Node prev,double cost,double total){
		this.current = node;
		this.prev = prev;
		this.costToHere = cost;
		this.totalCost = total;
	}

	public int compareTo(Object other){
		Fringe f = (Fringe) other;
		if (this.totalCost<f.totalCost){
			return -1;
		}
		if(this.totalCost>f.totalCost){
			return 1;
		}
		else{
			return 0;
		}
	}
	
	public Node getCurrent(){
		return current;
	}
	public Node getPrev(){
		return prev;
	}
	public double getCost(){
		return costToHere;
	}
	public double getTotalCost(){
		return totalCost;
	}
	public String toString(){
		return ("Current: "+current+" Prev: "+prev+" Cost: "+costToHere+" TotalCost: "+totalCost);
	}
}
