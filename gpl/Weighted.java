package edu.uwm.cs.gpl.graph;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;

public class Weighted<T> extends Graph<T, WeightedEdge<T>, Vertex<T,WeightedEdge<T>>> {

	public Weighted(Direction direction, Search search, EnumSet<Algorithms> algorithms){
		super(direction,search,algorithms);
	}

	public boolean addEdge(T value1, T value2, int weight){
		return super.addEdge(new WeightedEdge<T>(value1,value2,weight));
	}
	
	public boolean addVertex(T value){
		return super.addVertex(new Vertex<T,WeightedEdge<T>>(value));
	}

	//TODO: factor
	private void _search(Vertex<T,WeightedEdge<T>> v,ArrayList<WeightedEdge<T>> edges, Hashtable<T,Boolean> visited){
		visited.put(v.getValue(), true);
		for(WeightedEdge<T> e : v.getNeighbors())
			edges.add(e);
	}
	
	//TODO: Take an F<a,b>?
	public Weighted<T> search(T start, T target){
		Weighted<T> g = new Weighted<T>(direction, search, algorithms);
		Vertex<T,WeightedEdge<T>> vStart = getVertex(start);
		ArrayList<WeightedEdge<T>> edges = new ArrayList<WeightedEdge<T>>();
		Hashtable<T,Boolean> visited = new Hashtable<T,Boolean>();
		
		if(vStart == null)
			return g;
		
		g.addVertex(start);

		T v = start;
		_search(vStart,edges,visited);

		switch(search){
			case DFS:
				while(edges.size() > 0){
					WeightedEdge<T> e = edges.remove(edges.size() - 1);
					T w = e.getRight();
					if(!visited.get(w).booleanValue()){
						g.addVertex(w);
						g.addEdge(v, w, e.getWeight());
					}
				}
			case BFS:
				while(edges.size() > 0){
					WeightedEdge<T> e = edges.remove(0);
					T w = e.getRight();
					if(!visited.get(w).booleanValue()){
						g.addVertex(w);
						g.addEdge(v, w, e.getWeight());
					}
				}
				return g;
			default:
				throw new IllegalStateException("Unrecognized search algorithm.");
		}
	}
	
	//TODO: equals && hashCode?
}