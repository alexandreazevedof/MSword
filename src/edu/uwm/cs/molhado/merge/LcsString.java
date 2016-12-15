package edu.uwm.cs.molhado.merge;

import java.util.List;
import java.util.Vector;

public class LcsString extends LongestCommonSubsequence<Character> {
  private String x;
  private String y;

  public LcsString(String from, String to) {
    this.x = from;
    this.y = to;
  }

  protected int lengthOfY() {
    return y.length();
  }

  protected int lengthOfX() {
    return x.length();
  }

  protected Character valueOfX(int index) {
    return x.charAt(index);
  }

  protected Character valueOfY(int index) {
    return y.charAt(index);
  }

  public String getHtmlDiff() {
    DiffType type = null;
    List<DiffEntry<Character>> diffs = diff();
    StringBuffer buf = new StringBuffer();

    for (DiffEntry<Character> entry : diffs) {
      if (type != entry.getType()) {
        if (type != null) {
          buf.append("</span>");
        }
        buf.append("<span class=\"" + entry.getType().getName()
                + "\">");
        type = entry.getType();
      }
      buf.append(escapeHtml(entry.getValue()));
    }
    buf.append("</span>");
    return buf.toString();
  }

  private String escapeHtml(Character ch) {

    switch (ch) {
      case '<':
        return "<";
      case '>':
        return ">";
      case '"':
        return "\"";
      default:
        return ch.toString();
    }
  }



  public static void main(String[] args) {

		LcsString lcs = new LcsString("ab","ba");
		lcs.calculateLcs();
		List<Character> backtrack = lcs.backtrack();
		lcs.toString();
		System.out.println(backtrack);
		

    String A = "ba";
    String O = "ba";
    String B = "ab";
    Vector<Chunk<Character>> chunks = getDiff3(A, O, B);
    System.out.println("=========done============");
    for (Chunk<Character> chunk : chunks) {
      System.out.print(chunk);
      if (chunk.isConflict()){
        System.out.println("Found conflict");
        for(Object x : chunk.getConflictingNodes()){
          System.out.print("conflicting nodes:");
          System.out.println(x);
        }
      }
    }
  }

  public static Vector<Chunk<Character>> getDiff3(String A, String O, String B){

    LcsString seq1 = new LcsString(O, A);
    LcsString seq2 = new LcsString(O, B);
    seq1.backtrack();
    seq2.backtrack();
    int MA[][] = seq1.b;
    int MB[][] = seq2.b;

    Vector<Chunk<Character>> chunks = new Vector<Chunk<Character>>();

    int lo =0, la = 0, lb = 0;

    //step 2,
    step2:while(lo <= O.length() && la <= A.length() && lb <= B.length()){
      boolean i_exists = false;
      int i = 0;
      while(!i_exists && lo+i <O.length() && la+i <A.length() && lb+i<B.length()){
        ++i;
        if ( MA[lo+i][la+i] == 0 || MB[lo+i][lb+i] == 0 ){
          i_exists = true;
        }
      }
      if (!i_exists ) {
        //print stable chunk
        if (lo+1<=lo+i){
          Chunk<Character> chunk = makeChunk(A,O,B,la+1,lo+1,lb+1,la+i,lo+i,lb+i);
          if (chunk!=null) chunks.add(chunk);
          System.out.print(chunk+":245");
        }
        lo =lo+i; la=la+i; lb=lb+i;
        break step2; //go to step 3, print remaining unstable chunk
      }
      if (i == 1){ // (a)
        for(int o=lo+1; o <= O.length(); o++){
          for(int a=la+1; a<=A.length() ; a++){
            for(int b=lb+1; b<=B.length(); b++){
              if (MA[o][a] == 1 && MB[o][b] == 1){
                //unstable chunk
                Chunk<Character> chunk =
                        makeChunk(A,O,B,la+1,lo+1,lb+1,a-1,o-1,b-1);
                if (chunk != null) chunks.add(chunk);
                System.out.print(chunk+":258");
                lo=o-1;la=a-1;lb=b-1;
                continue step2;
              }
            }
          }
        }
        break; //o does not exist, go to step 3
      } else if (i > 1){ // (b)
        //stable chunk
        Chunk<Character> chunk =
                makeChunk(A,O,B,la+1,lo+1,lb+1,la+i-1,lo+i-1,lb+i-1);
        if (chunk!=null) chunks.add(chunk);
        System.out.print(chunk+":270");
        lo=lo+i-1; la=la+i-1; lb=lb+i-1;
        continue step2;
      }
    }

    //step3: print unstable chunks left over
    if (( lo < O.length() || la<A.length() || lb < B.length())){
      Chunk<Character> chunk =
              makeChunk(A,O,B,la+1,lo+1,lb+1,A.length(),O.length(),B.length());
      if (chunk != null) chunks.add(chunk);
      System.out.print(chunk+":280");
    }

    return chunks;
  }

  private static Chunk<Character> makeChunk(String A, String O, String B,
          int la, int lo, int lb, int sa, int so, int sb) {
    Vector<Character> a = new Vector<Character>();
    Vector<Character> b = new Vector<Character>();
    Vector<Character> o = new Vector<Character>();
    for (int i = la; i <= sa; i++) {
      a.add(A.charAt(i - 1));
    }
    for (int i = lb; i <= sb; i++) {
      b.add(B.charAt(i - 1));
    }
    for (int i = lo; i <= so; i++) {
      o.add(O.charAt(i - 1));
    }
    if (a.isEmpty() && b.isEmpty() && o.isEmpty()) return null;
    Chunk<Character> chunk = new Chunk<Character>();
    if (o.equals(b) && !b.equals(a)){
      chunk.setType(Chunk.CHANGE_IN_A);
    } else if (o.equals(a) && !a.equals(b)){
      chunk.setType(Chunk.CHANGE_IN_B);
    } else if (!o.equals(a) && a.equals(b)){
      chunk.setType(Chunk.FALSELY_CONFLICTING);
    } else if (!o.equals(a) && !a.equals(b) && !b.equals(o)){
      chunk.setType(Chunk.TRUELY_CONFLICTING);
    }
    chunk.setA(a);
    chunk.setB(b);
    chunk.setO(o);
    return chunk;
  }

  @Override
  protected boolean isInX(Character c) {
    return isIn(c, x);
  }

  @Override
  protected boolean isInY(Character c) {
    return isIn(c, y);
  }

  private boolean isIn(Character c, String s) {
    for (int i = 0; i < s.length(); i++) {
      if (c == s.charAt(i))
        return true;
    }
    return false;
  }

}
