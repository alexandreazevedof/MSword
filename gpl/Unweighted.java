package edu.uwm.cs.gpl.graph;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;

public class Unweighted<T> extends Graph<T,UnweightedEdge<T>,Vertex<T,UnweightedEdge<T>>>{
	
	public Unweighted(Direction direction, Search search, EnumSet<Algorithms> algorithms){
		super(direction,search,algorithms);
	}
	
	public boolean addEdge(T value1, T value2){
		return super.addEdge(new UnweightedEdge<T>(value1,value2));
	}

	public boolean addVertex(T value){
		return super.addVertex(new Vertex<T,UnweightedEdge<T>>(value));
	}

	//TODO: factor
	private void _search(Vertex<T,UnweightedEdge<T>> v,ArrayList<UnweightedEdge<T>> edges, Hashtable<T,Boolean> visited){
		visited.put(v.getValue(), true);
		for(UnweightedEdge<T> e : v.getNeighbors())
			edges.add(e);
	}
	
	//TODO: Take an F<a,b>?
	public Unweighted<T> search(T start, T target){
		Unweighted<T> g = new Unweighted<T>(direction, search, algorithms);
		Vertex<T,UnweightedEdge<T>> vStart = getVertex(start);
		ArrayList<UnweightedEdge<T>> edges = new ArrayList<UnweightedEdge<T>>();
		Hashtable<T,Boolean> visited = new Hashtable<T,Boolean>();
		
		if(vStart == null)
			return g;
		
		g.addVertex(start);

		T v = start;
		_search(vStart,edges,visited);

		switch(search){
			case DFS:
				while(edges.size() > 0){
					UnweightedEdge<T> e = edges.remove(edges.size() - 1);
					T w = e.getRight();
					if(!visited.get(w).booleanValue()){
						g.addVertex(w);
						g.addEdge(v, w);
					}
				}
			case BFS:
				while(edges.size() > 0){
					UnweightedEdge<T> e = edges.remove(0);
					T w = e.getRight();
					if(!visited.get(w).booleanValue()){
						g.addVertex(w);
						g.addEdge(v, w);
					}
				}
				return g;
			default:
				throw new IllegalStateException("Unrecognized search algorithm.");
		}
	}

	//TODO: equals && hashCode?
}