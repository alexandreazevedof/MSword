package edu.uwm.cs.gpl.graph;

import java.util.ArrayList;
import java.util.EnumSet;

public abstract class Graph<T,E extends Edge<T,E>,V extends Vertex<T,E>> {
	protected ArrayList<V> vertices = new ArrayList<V>();
	protected Direction direction;
	protected Search search;
	protected EnumSet<Algorithms> algorithms;
	
	public Graph(Direction direction, Search search, EnumSet<Algorithms> algorithms){
		this.direction = direction;
		this.search = search;
		this.algorithms = algorithms;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean addEdge(E edge1){
		Vertex<T, E> vertex1 = getVertex(edge1.getLeft());
		Vertex<T, E> vertex2 = getVertex(edge1.getRight());
		if(vertex1 == null || vertex2 == null)
			throw new IllegalStateException("The vertices must exist before an edge can be added");

		ArrayList<E> neighbors1 = vertex1.getNeighbors(),
				     neighbors2 = vertex2.getNeighbors();

		//unecessary to check both neighbor lists
		if(neighbors1.contains(edge1))
			throw new IllegalStateException("The edge already exists");
		
		switch(direction){
			case DIRECTED:
				return neighbors1.add(edge1);
			case UNDIRECTED:
				E edge2 =(E)edge1.clone();
				edge2.setLeft(edge1.getRight());
				edge2.setRight(edge1.getLeft());
				return neighbors1.add(edge1) && neighbors2.add(edge2);
			default:
				throw new IllegalStateException("Unrecognized direction");
		}
	}
	
	protected boolean addVertex(V v){
		if(v == null)
			throw new IllegalStateException("Vertex cannot be null");
		
		if(getVertex(v.getValue()) != null)
			return false;
		return vertices.add(v);
	}
	
	protected V getVertex(T value){
		for(V v : vertices)
			if(v.getValue().equals(value))
				return v;
		return null;
	}

	public boolean isEdge(T value1, T value2){
		if(value1 == null || value2 == null)
			throw new IllegalStateException("Vertex cannot be null");
		Vertex<T,E> vertex = getVertex(value1);
		return (vertex == null) ? false : vertex.isEdge(value2);
	}
	
	public boolean isVertex(T value){
		if(value == null)
			throw new IllegalStateException("Vertex cannot be null");
		return getVertex(value) != null;
	}

	public boolean removeEdge(T value1, T value2){
		if(value1 == null || value2 == null)
			throw new IllegalStateException("Vertex cannot be null");
		Vertex<T,E> vertex1 = getVertex(value1),
				    vertex2 = getVertex(value2);
		if(vertex1 == null || vertex2 == null)
			throw new IllegalStateException("The vertices must both exist before removing an edge");

		if(!vertex1.isEdge(value2))
			throw new IllegalStateException("An edge must exist before it can be removed");
		
		switch(direction){
			case DIRECTED:
				return vertex1.removeEdge(vertex2);
			case UNDIRECTED:
				return vertex1.removeEdge(vertex2) && vertex2.removeEdge(vertex1);
			default:
				throw new IllegalStateException("Unrecognized direction");
		}
	}

	public boolean removeVertex(T value){
		if(value == null)
			throw new IllegalStateException("Vertex cannot be null");
		Vertex<T,E> vertex = getVertex(value);

		if(vertex == null)
			throw new IllegalStateException("The vertex must exist before being removed");

		vertices.remove(vertex);
		
		for(Vertex<T,E> v : vertices)
			if(v.isEdge(value))
				v.removeEdge(vertex);
		return true;
	}

}
