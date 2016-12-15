package edu.uwm.cs.gpl.graph;

public class UnweightedEdge<V> extends Edge<V,UnweightedEdge<V>> implements Cloneable{

	public UnweightedEdge(V left, V right){
		super(left,right);
	}
	
	@Override
	public boolean equals(Object edge){
		if(edge == null)
			return false;
		if(edge == this)
			return true;
		if (!(edge instanceof UnweightedEdge<?>))
			return false;
		final UnweightedEdge<?> e = (UnweightedEdge<?>)edge;
		
		return (e.getLeft().equals(left) && e.getRight().equals(right));
	}
	
	@Override
	public int hashCode(){
		return left.hashCode() * right.hashCode() * 53;
	}
	
	@Override
	public Object clone(){
		return new UnweightedEdge<V>(left.getValue(), right.getValue());
	}
}
