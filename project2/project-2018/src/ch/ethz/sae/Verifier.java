package ch.ethz.sae;

import java.util.HashMap;
import java.util.Iterator;

import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.toolkits.graph.BriefUnitGraph;

public class Verifier {
	
	private static String noSpeedSet = "NO SPEED SET";
	public static PAG pointsToAnalysis;

	public static void main(String[] args) {
//		long startTime = System.currentTimeMillis();
		if (args.length != 1) {
			System.err
					.println("Usage: java -classpath soot-2.5.0.jar:./bin ch.ethz.sae.Verifier <class to test>");
			System.exit(-1);
		}
		String className = args[0];
		SootClass sootClass = loadClass(className);

		pointsToAnalysis = doPointsToAnalysis(sootClass);
		
		// Idea
		// 1. In JReturn, keep list of cars that are being returned;
		// 2. Use the pointer analysis to determine which cars are pointing to which
		// 3. Go through the statements and look for method calls to setSpeed, look at the the state
		//    of the variables before the method call.
		
		/* iterating over all test methods (you can assume that these is only one test method per test class) */
		for (SootMethod method : sootClass.getMethods()) {

			if (method.getName().contains("<init>")) {
				/* skipping the constructor of the class */
				continue;
			}
			
//			System.out.println("\n\n\n\n\n========================= NEW ANALYSIS =========================\n");
//			System.out.println("Current test: " + method.getName());
//			
			Analysis analysis = new Analysis(new BriefUnitGraph(
					method.retrieveActiveBody()), sootClass);
			analysis.run();
//			
//			System.out.println("\n======================= ANALYSIS RESULTS =======================\n");
//			
//			PatchingChain<Unit> statements = method.getActiveBody().getUnits();
//			
//			Iterator<Unit> it = statements.iterator();
//			Unit prevStmt = null;
//			if (it.hasNext()) {
//				prevStmt = it.next();
//				System.out.println("After executing: " + prevStmt + ", state is ");
//			}
//			while (it.hasNext()) {
//				prevStmt = it.next();
//				AWrapper flowBefore = analysis.getFlowBefore(prevStmt);
//				System.out.println("    " + flowBefore);
//				System.out.println("After executing: " + prevStmt + ", state is ");
//			}
//			
//			try {
//				System.out.println("    " + analysis.getFlowBefore(prevStmt));
//			} catch (NullPointerException e) {
//				System.out.println("    None");
//			}
			
			/* TODO: compute the upper bound for the speed */			
			
			/* Hints:
			 * - iterate over method.retrieveActiveBody().getUnits() to get the statements
			 * - use analysis.getFlowBefore(...) to retrieve the abstract state before a statement of the method 
			 *
			 * - use pointsToAnalysis.reachingObjects(...) to get the points-to set of local pointer variable 
			 * - use the P2SetVisitor class to visit all objects in the points-to set
			*/
			
			/* TODO: print to console the upper bound or NO SPEED SET; 
			 * - make sure it is the last line printed by your program; 
			*/
			
//			System.out.println("\nHas the speed for this object been set at some point? " + analysis.speedSet);
//			System.out.println("Speeds of all car objects:                            " + analysis.speeds);
//			System.out.println("Has this object been returned?                        " + analysis.carsReturned);
			
			int max = Integer.MIN_VALUE;
			boolean set = false;
			for (String objectName : analysis.speedSet.keySet()) {
				if (analysis.speedSet.get(objectName) && analysis.carsReturned.get(objectName) && analysis.speeds.get(objectName) >= max) {
					max = analysis.speeds.get(objectName);
					set = true;
				}
			}
			
//			System.out.print("\n => ");
			System.out.println(set ? max : noSpeedSet);
//			System.out.println(System.currentTimeMillis() - startTime);
		}
	}
	
	/* =================================================================================== */

	/* no need to change the methods below; they are already used in the main method above */

	/* load Soot class */
	private static SootClass loadClass(String name) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		return c;
	}

	/* run the Soot Spark points-to analysis */
	private static PAG doPointsToAnalysis(SootClass c) {
		Scene.v().setEntryPoints(c.getMethods());

		HashMap<String, String> options = new HashMap<String, String>();
		options.put("enabled", "true");
		options.put("verbose", "false");
		options.put("propagator", "worklist");
		options.put("simple-edges-bidirectional", "false");
		options.put("on-fly-cg", "true");
		options.put("set-impl", "double");
		options.put("double-set-old", "hybrid");
		options.put("double-set-new", "hybrid");

		SparkTransformer.v().transform("", options);
		PAG pag = (PAG) Scene.v().getPointsToAnalysis();

		return pag;
	}
}
