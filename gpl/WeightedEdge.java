package edu.uwm.cs.gpl.graph;

public class WeightedEdge<V> extends Edge<V,WeightedEdge<V>> implements Cloneable{
	private int weight = 0;
	
	public WeightedEdge(V left, V right, int weight){
		super(left, right);
		this.setWeight(weight);
	}

	public void setWeight(int weight) {
		if(weight < 0)
			throw new IllegalArgumentException("weight must be non-negative.");
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public Object clone(){
		return new WeightedEdge<V>(left.getValue(),right.getValue(),weight);
	}
	
	@Override
	public boolean equals(Object edge){
		if(edge == null)
			return false;
		if(edge == this)
			return true;
		if (!(edge instanceof WeightedEdge<?>))
			return false;
		final WeightedEdge<?> e = (WeightedEdge<?>)edge;
		
		return (e.getLeft().equals(left) && e.getRight().equals(right));
	}
	
	@Override
	public int hashCode(){
		return left.hashCode() * right.hashCode() * 53;
	}
}