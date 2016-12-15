package edu.uwm.cs.molhado.test;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 *
 * @author chengt
 */
public class TestSAT {

	public static void main(String[] args) throws TimeoutException, ContradictionException{
		final int MAXVAR = 1000000;
		final int NBCLAUSES = 500000;
		ISolver solver = SolverFactory.newDefault();
		
		System.out.println(-1+1);
		// prepare the solver to accept MAXVAR variables. MANDATORY
		solver.newVar(3);
		// not mandatory for SAT solving. MANDATORY for MAXSAT solving
		solver.setExpectedNumberOfClauses(5);
		// Feed the solver using Dimacs format, using arrays of int
		// (best option to avoid dependencies on SAT4J IVecInt)
		//for (int i=0;i<NBCLAUSES;i++) {
			int [] clause1 = {1};// get the clause from somewhere
			int [] clause2 = {-1,-2, 3};
			int [] clause3 = {-3};
			//int [] clause3 = {-3,2};
			//int [] clause4 = {-3,1};
			//int [] clause5 = {-1,3};

			// the clause should not contain a 0, only integer (positive or negative)
			// with absolute values less or equal to MAXVAR
			// e.g. int [] clause = {1, -3, 7}; is fine
			// while int [] clause = {1, -3, 7, 0}; is not fine
			solver.addClause(new VecInt(clause1)); // adapt Array to IVecInt
			solver.addClause(new VecInt(clause2)); // adapt Array to IVecInt
			solver.addClause(new VecInt(clause3)); // adapt Array to IVecInt
			//solver.addClause(new VecInt(clause3)); // adapt Array to IVecInt
			//solver.addClause(new VecInt(clause4)); // adapt Array to IVecInt
		//	solver.addClause(new VecInt(clause6)); // adapt Array to IVecInt
		//}
		
		// we are done. Working now on the IProblem interface
		IProblem problem = solver;

		
		if (problem.isSatisfiable()) {
			int[] model = problem.model();
			for(int i=0; i<model.length; i++){
				System.out.println(model[i]); }
			System.out.println("Is satisfiable");
			System.out.println(problem.model(3));
			System.out.println(problem.model(2));
			System.out.println(problem.model(1));
		} else {
		System.out.println(problem.model());
			System.out.println("Is not satisfiable");
		// ...
		}

	}

}
