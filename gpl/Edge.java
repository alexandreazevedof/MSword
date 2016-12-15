package edu.uwm.cs.gpl.graph;

public abstract class Edge<V,E extends Edge<V,E>> implements Cloneable{
	protected Vertex<V,E> left;
	protected Vertex<V,E> right;

	public Edge(V left, V right){
		this.setLeft(left);
		this.setRight(right);
	}
	
	public void setLeft(V value){
		this.left = new Vertex<V,E>(value);
	}
	
	public V getLeft(){
		return this.left.getValue();
	}
	
	public void setRight(V value){
		this.right = new Vertex<V,E>(value);
	}
	
	public V getRight(){
		return this.right.getValue();
	}
	
	@Override
	public abstract Object clone();
}