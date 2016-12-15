package edu.uwm.cs.molhado.merge;

import java.util.Vector;

/**
 *
 * @author chengt
 */
public class Chunk<T> {

	public static final int CHANGE_IN_A = 0;
	public static final int CHANGE_IN_B = 1;
  public static final int FALSELY_CONFLICTING = 2;
  public static final int TRUELY_CONFLICTING = 3;
	public static final int STABLE= 4;

  private int type;
	private Vector<T> A = new Vector<T>();
	private Vector<T> O = new Vector<T>();
	private Vector<T> B = new Vector<T>();

	public Chunk(){
    this.type = STABLE;
	}
	public Chunk(int type) {
		this.type = type;
	}

	public void setType(int type){
		this.type = type;
	}

	public int getType(){
	  return type;
	}

	public void setA(Vector<T> A) {
		this.A = A;
	}

  public Vector<T> getA(){
    return A;
  }

	public void setB(Vector<T> B) {
		this.B = B;
	}

  public Vector<T> getB(){
    return B;
  }

	public void setO(Vector<T> O) {
		this.O = O;
  }

  public Vector<T> getO(){
    return O;
  }

	public boolean isConflict() {
		return type == FALSELY_CONFLICTING || 
            type == TRUELY_CONFLICTING;
  }
  public Vector<T> getConflictingNodes(){
    Vector<T> v = new Vector<T>();
    if (isConflict()){
    }
    return v;
  }

  public Vector<T> getMergeList(){
    Vector list = new Vector();
    if (type == CHANGE_IN_A){
      list.addAll(A);
    }else if (type == CHANGE_IN_B){
      list.addAll(B);
    }else if (type == STABLE){
      list.addAll(A);
    } else if (type == FALSELY_CONFLICTING ){
      list.addAll(A);
    } else {
      System.out.println(A);
      System.out.println(O);
      System.out.println(B);
      throw new RuntimeException("Conflict");
    }
    return list;
  }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (type == CHANGE_IN_A) {
      sb.append('[');
			for (T a : A) {
				sb.append(a);
			}
      sb.append("]\n");
		} else if (type == CHANGE_IN_B) {
      sb.append('[');
			for (T b : B) {
				sb.append(b); 
			}
      sb.append("]\n");
		} else if (type == STABLE){ sb.append('['); for (T a : A) {
				sb.append(a);
			}
      sb.append("]\n");
		}else {
			sb.append("<<<<<<<<<<<<<<<<<A\n");
      sb.append('[');
			for (T a : A) {
				sb.append(a);
			}
      sb.append("]\n");
			sb.append("|||||||||||||||||O\n");
      sb.append('[');
			for (T o : O) {
				sb.append(o);
			}
      sb.append("]\n");
			sb.append("==================\n");
      sb.append('[');
			for (T b : B) {
				sb.append(b);
			}
      sb.append("]\n");
			sb.append(">>>>>>>>>>>>>>>>>B\n");
		}
		return sb.toString();
	}

	public static void main(String[] args) {
	}
}

