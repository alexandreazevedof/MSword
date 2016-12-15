package edu.uwm.cs.molhado.test;

import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author chengt
 */
public class TestObserver {

	public static void main(String[] args){
		MyObservable observable = new MyObservable();
		MyObserver observer = new MyObserver();
		observable.addObserver(observer);
		for(int i=0; i<10; i++){
			observable.change();
		}
	}
}

class MyObservable extends Observable{
	private int counter;
	void change(){
		setChanged();
		notifyObservers(new Integer(counter++));
	}
}

class MyObserver implements Observer{
	public void update(Observable o, Object arg) {
		System.out.println("update: " + arg);
	}
}