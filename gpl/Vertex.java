package edu.uwm.cs.gpl.graph;

import java.util.ArrayList;

public class Vertex<V,E extends Edge<V,E>> {
	private V value;
	private ArrayList<E> neighbors = new ArrayList<E>(); 
	
	public Vertex(V value){
		if(value == null)
			throw new IllegalStateException("value cannot be null");
		this.value = value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public V getValue() {
		return value;
	}

	public ArrayList<E> getNeighbors(){
		return neighbors;
	}

	public boolean isEdge(V value){
		for(E edge : neighbors)
			if(edge.getRight().equals(value))
				return true;
		return false;
	}

	public boolean removeEdge(Vertex<V, E> vertex){
		E edge = null;

		for(E e : neighbors){
			if(e.getRight().equals(vertex)){
				edge = e;
				break;
			}
		}
		
		if(edge == null)
			throw new IllegalStateException("An edge must exist before it can be removed");
		
		return neighbors.remove(edge);
	}
	
	@Override
	public boolean equals(Object vertex){
		if(vertex == null)
			return false;
		if(vertex == this)
			return true;
		if (!(vertex instanceof Vertex<?,?>))
			return false;
		final Vertex<?,?> v = (Vertex<?,?>)vertex;
		
		return v.value.equals(this.value);
	}
	
	@Override
    public int hashCode() {
		return this.value.hashCode() * 37;
	}
}