package ch.ethz.sae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.IntegerType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.Chain;
import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Interval;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.Tcons1;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;

/* numerical analysis */
public class Analysis extends ForwardBranchedFlowAnalysis<AWrapper> {

	/* Apron abstract domain instance */
	public static Manager man;

	/* Apron environment */
	public static Environment env;
	
	// Pointer analysis
	private PAG pointsToAnalysis = Verifier.pointsToAnalysis;
	
	// HashMap to with the current bounds on the speeds of the cars
	public HashMap<String, Boolean> speedSet = new HashMap<String, Boolean>();
	
	// HashMap to check if a particular car was returned or not
	public HashMap<String, Boolean> carsReturned = new HashMap<String, Boolean>();
	
	// HashMap to check if the speed of a particular car was set at some point
	public HashMap<String, Integer> speeds = new HashMap<String, Integer>();

	public void run() {
		doAnalysis();
	}

	@Override
	protected void flowThrough(AWrapper inWrapper, Unit op,
			List<AWrapper> intoIfWrappers, List<AWrapper> intoElseWrappers) {

		Stmt stmt = (Stmt) op;
		Abstract1 intoIf = null;
		Abstract1 intoElse = null;

		try {
			intoIf = new Abstract1(man, inWrapper.get());
			intoElse = new Abstract1(man, inWrapper.get());
				
			if (stmt instanceof DefinitionStmt) {
				/* assignment statement */

				DefinitionStmt defStmt = (DefinitionStmt) stmt;
				Value lhs = defStmt.getLeftOp();
				Value rhs = defStmt.getRightOp();
				
				if (lhs instanceof JimpleLocal) {
					/* assignment to a local var */
					String lhs_varName = ((JimpleLocal) lhs).getName();

					if (rhs instanceof IntConstant) {
						/* assign constant to a local var (e.g., n = 10) */
						IntConstant c = ((IntConstant) rhs);
						Texpr1Node constantNode = new Texpr1CstNode(new MpqScalar(
								c.value));
						Texpr1Intern internalNode = new Texpr1Intern(env, constantNode);
						intoIf.assign(man, lhs_varName, internalNode, null);
						/* o_branchout does not matter for Definition statements */
					
					} else if (rhs instanceof JimpleLocal) {
						// Assign variable to variable (e.g. x = y)
						JimpleLocal rhsVariable = (JimpleLocal) rhs;
						// Make sure that this variable is known to the analyzer, it 
						// could be some object that the analysis doesn't take into account
						if (env.hasVar(rhsVariable.getName())) {
							Texpr1Node varNode = new Texpr1VarNode(rhsVariable.getName());
							Texpr1Intern internalNode = new Texpr1Intern(env, varNode);
							intoIf.assign(man, lhs_varName, internalNode, null);
						}
						
					} else if (rhs instanceof BinopExpr) {
						// Assign binary operator expression to variable (e.g. x = 1 + y)
						BinopExpr rhsBinOp = (BinopExpr) rhs;
						Value left = rhsBinOp.getOp1();
						Value right = rhsBinOp.getOp2();
						Texpr1Node leftNode = null;
						Texpr1Node rightNode = null;
						boolean validVarNames = true;
						
						// Create the proper Texpr1Node based on the type of the operands
						if (left instanceof IntConstant) {
							IntConstant leftConstant = (IntConstant) left;
							leftNode = new Texpr1CstNode(new MpqScalar(leftConstant.value));	
						} else if (left instanceof JimpleLocal) {
							JimpleLocal leftVariable = (JimpleLocal) left;
							if (env.hasVar(leftVariable.getName())) leftNode = new Texpr1VarNode(leftVariable.getName());
							else validVarNames = false;
						}
						if (right instanceof IntConstant) {
							IntConstant rightConstant = (IntConstant) right;
							rightNode = new Texpr1CstNode(new MpqScalar(rightConstant.value));
						} else if (right instanceof JimpleLocal) {
							JimpleLocal rightVariable = (JimpleLocal) right;
							if (env.hasVar(rightVariable.getName())) rightNode = new Texpr1VarNode(rightVariable.getName());
							else validVarNames = false;
						}
						
						// Select the appropriate operator based on the type of the expression
						int operator = 0;
						if (rhs instanceof JAddExpr) {
							operator = Texpr1BinNode.OP_ADD;
						} else if (rhs instanceof JSubExpr) {
							operator = Texpr1BinNode.OP_SUB;
						} else if (rhs instanceof JMulExpr) {
							operator = Texpr1BinNode.OP_MUL;
						}
						
						// Finally assign intoIf
						if (validVarNames) {
							Texpr1Node binNode = new Texpr1BinNode(operator, leftNode, rightNode);
							Texpr1Intern internalNode = new Texpr1Intern(env, binNode);
							intoIf.assign(man, lhs_varName, internalNode, null);
						}
						
						// The other cases do not need to be handled
					}
				}

			} else if (stmt instanceof JIfStmt) {
				
				Value ce = ((JIfStmt) stmt).getCondition();  // ce represents condition expression
				Value lhs = ((BinopExpr) ce).getOp1();
				Value rhs = ((BinopExpr) ce).getOp2();
								
				// lhs and rhs can either be a local variable or a int constant. The following code is translating it into node
				// Create the proper Texpr1Node based on the type of the operands
				Texpr1Node binopLeft = null;
				Texpr1Node binopRight = null;
				Interval leftInterval = null;
				Interval rightInterval = null;
				
				boolean validVarNames = true;
				
				if (lhs instanceof IntConstant) {
					IntConstant leftConstant = (IntConstant) lhs;
					binopLeft = new Texpr1CstNode(new MpqScalar(leftConstant.value));
					leftInterval = new Interval(leftConstant.value, leftConstant.value);
				} else if (lhs instanceof JimpleLocal) {
					JimpleLocal leftVariable = (JimpleLocal) lhs;
					if (env.hasVar(leftVariable.getName())) {
						binopLeft = new Texpr1VarNode(leftVariable.getName());
						leftInterval = intoIf.getBound(man, leftVariable.getName());
					}
					else validVarNames = false;
				}
				
				if (rhs instanceof IntConstant) {
					IntConstant rightConstant = (IntConstant) rhs;
					binopRight = new Texpr1CstNode(new MpqScalar(rightConstant.value));
					rightInterval = new Interval(rightConstant.value, rightConstant.value);
				} else if (rhs instanceof JimpleLocal) {
					JimpleLocal rightVariable = (JimpleLocal) rhs;
					if (env.hasVar(rightVariable.getName())) {
						binopRight = new Texpr1VarNode(rightVariable.getName());
						rightInterval = intoIf.getBound(man, rightVariable.getName());
					}
					else validVarNames = false;
				}
				
				
				// finished parsing the binary operator, the following code handle the constrains read from the if statement
				// we first create a T expression node using the constructor in Texpr1BinNode(OP_SUB, binopLeft, binopRight)
				if (validVarNames) {
					// By default, all the other variables are set to some random value, so joining two non-null Abstracts with
					// disjoint conditions produces <Top>
					Texpr1Node left_right = new Texpr1BinNode(Texpr1BinNode.OP_SUB, binopLeft, binopRight);  // left - right
					Texpr1Node right_left = new Texpr1BinNode(Texpr1BinNode.OP_SUB, binopRight, binopLeft);  // right - left
										
					// Jimple translates if (b) {s1} s2 into if (!b) goto s2; s1; s2, so this case is actually if (x == y) s1 else s2
					if (ce instanceof JNeExpr) {
						
						// for equal expression there is one type of fallout and two types of branchout
						// fallout (entering the body of if statement)
						Tcons1 ifConstraint = new Tcons1(env, Tcons1.EQ, left_right);
						Abstract1 ifConstraintAbstract = new Abstract1(man, new Tcons1[] {ifConstraint});
						intoIf.meet(man, ifConstraintAbstract);
						
						// entering the else body
						// Then x != y, so we need to join x < y and x > y. If we already know that x == y, then the else
						// branch is unreachable.
						if (leftInterval.isScalar() && rightInterval.isScalar() && leftInterval.isEqual(rightInterval)) {
							intoElse = null;
						} else {
							Tcons1 elseConstraint1 = new Tcons1(env, Tcons1.SUP, left_right);
							Tcons1 elseConstraint2 = new Tcons1(env, Tcons1.SUP, right_left);
							Abstract1 elseConstraint1Abstract = new Abstract1(man, new Tcons1[] {elseConstraint1});
							Abstract1 elseConstraint2Abstract = new Abstract1(man, new Tcons1[] {elseConstraint2});
							elseConstraint1Abstract.join(man, elseConstraint2Abstract);
							intoElse.meet(man, elseConstraint1Abstract);
						}
					}
					
					// If (x != y) s1 else s2
					else if (ce instanceof JEqExpr) {
						
						// If we know that x == y, the if branch should be unreachable
						if (leftInterval.isScalar() && rightInterval.isScalar() && leftInterval.isEqual(rightInterval)) {
							intoIf = null;
						} else {
							Tcons1 ifConstraint1 = new Tcons1(env, Tcons1.SUP, left_right);
							Tcons1 ifConstraint2 = new Tcons1(env, Tcons1.SUP, right_left);
							Abstract1 ifConstraint1Abstract = new Abstract1(man, new Tcons1[] {ifConstraint1});
							Abstract1 ifConstraint2Abstract = new Abstract1(man, new Tcons1[] {ifConstraint2});
							ifConstraint1Abstract.join(man, ifConstraint2Abstract);
							intoIf.meet(man, ifConstraint1Abstract);
						}
						
						Tcons1 elseConstraint = new Tcons1(env, Tcons1.EQ, left_right);
						Abstract1 elseConstraintAbstract = new Abstract1(man, new Tcons1[] {elseConstraint});
						intoElse.meet(man, elseConstraintAbstract);
					}
					
					// If (x < y) s1 else s2
					else if (ce instanceof JGeExpr) {
						if (leftInterval.isEqual(new Interval(Integer.MAX_VALUE, Integer.MAX_VALUE))
								|| rightInterval.isEqual(new Interval(Integer.MIN_VALUE, Integer.MIN_VALUE))) {
							intoIf = null;
						} else {
							Tcons1 ifConstraint = new Tcons1(env, Tcons1.SUP, right_left);
							Abstract1 ifConstraintAbstract = new Abstract1(man, new Tcons1[] {ifConstraint});
							intoIf.meet(man, ifConstraintAbstract);
						}
						
						Tcons1 elseConstraint = new Tcons1(env, Tcons1.SUPEQ, left_right);
						Abstract1 elseConstraintAbstract = new Abstract1(man, new Tcons1[] {elseConstraint});
						intoElse.meet(man, elseConstraintAbstract);
					}
					
					// If (x <= y) s1 else s2
					else if (ce instanceof JGtExpr) {
						Tcons1 ifConstraint = new Tcons1(env, Tcons1.SUPEQ, right_left);
						Abstract1 ifConstraintAbstract = new Abstract1(man, new Tcons1[] {ifConstraint});
						intoIf.meet(man, ifConstraintAbstract);
						
						if (leftInterval.isEqual(new Interval(Integer.MIN_VALUE, Integer.MIN_VALUE))
								|| rightInterval.isEqual(new Interval(Integer.MAX_VALUE, Integer.MAX_VALUE))) {
							intoElse = null;
						} else {
							Tcons1 elseConstraint = new Tcons1(env, Tcons1.SUP, left_right);
							Abstract1 elseConstraintAbstract = new Abstract1(man, new Tcons1[] {elseConstraint});
							intoElse.meet(man, elseConstraintAbstract);
						}
					}
					// If (x > y) s1 else s2
					else if (ce instanceof JLeExpr) {
						if (rightInterval.isEqual(new Interval(Integer.MAX_VALUE, Integer.MAX_VALUE))
								|| leftInterval.isEqual(new Interval(Integer.MIN_VALUE, Integer.MIN_VALUE))) {
							intoIf = null;
						} else {
							Tcons1 ifConstraint = new Tcons1(env, Tcons1.SUP, left_right);
							Abstract1 ifConstraintAbstract = new Abstract1(man, new Tcons1[] {ifConstraint});
							intoIf.meet(man, ifConstraintAbstract);
						}
						
						Tcons1 elseConstraint = new Tcons1(env, Tcons1.SUPEQ, right_left);
						Abstract1 elseConstraintAbstract = new Abstract1(man, new Tcons1[] {elseConstraint});
						intoElse.meet(man, elseConstraintAbstract);
					}
					
					// If (x >= y) s1 else s2
					else if (ce instanceof JLtExpr) {
						Tcons1 ifConstraint = new Tcons1(env, Tcons1.SUPEQ, left_right);
						Abstract1 ifConstraintAbstract = new Abstract1(man, new Tcons1[] {ifConstraint});
						intoIf.meet(man, ifConstraintAbstract);
						
						if (rightInterval.isEqual(new Interval(Integer.MIN_VALUE, Integer.MIN_VALUE))
								|| leftInterval.isEqual(new Interval(Integer.MAX_VALUE, Integer.MAX_VALUE))) {
							intoElse = null;
						} else {
							Tcons1 elseConstraint = new Tcons1(env, Tcons1.SUP, right_left);
							Abstract1 elseConstraintAbstract = new Abstract1(man, new Tcons1[] {elseConstraint});
							intoElse.meet(man, elseConstraintAbstract);
						}
					}
				}
			}
				
			// Invocations of setSpeed
			else if (stmt instanceof JInvokeStmt) {
				
				// Make sure this method call is reachable (don't do anything otherwise)
				if (!inWrapper.get().isBottom(man)) {
					InvokeExpr expr = stmt.getInvokeExpr();

					// Calls to setSpeed are VirtualInvokeExprs
					if (expr instanceof VirtualInvokeExpr) {
						
						VirtualInvokeExpr speedInvocation = (VirtualInvokeExpr) expr;
						Value speed = speedInvocation.getArg(0);
						final PointsToSetInternal p2set = (PointsToSetInternal) pointsToAnalysis.reachingObjects((Local) speedInvocation.getBase());
						
						// Find the constraints for the argument passed to setSpeed
						final Texpr1Intern xp;
						if (speed instanceof IntConstant) {
							IntConstant c = ((IntConstant) speed);
							Texpr1Node rAr = new Texpr1CstNode(new MpqScalar(c.value));
							xp = new Texpr1Intern(env, rAr);
						} else if (speed instanceof JimpleLocal) {
							JimpleLocal speedVariable = (JimpleLocal) speed;
							if (env.hasVar(speedVariable.getName())) {
								Texpr1Node rAr = new Texpr1VarNode(speedVariable.getName());
								xp = new Texpr1Intern(env, rAr);
							} else xp = null;
						} else xp = null;
						
						// Update the speeds for all the objects that the returned reference could point to
						if (xp != null) {
							final Abstract1 tempIntoIf = intoIf;
							p2set.forall(new P2SetVisitor() {
								@Override
								public void visit(Node n) {
									try {
										if (p2set.size() == 1) {
											tempIntoIf.assign(man, "speed" + n.getNumber(), xp, null);
										} else {
											// If the returned reference could point to several objects, we cannot
											// simply update the speed of all the objects, since the returned reference
											// actually only points to one of them (updating the others could result in 
											// a false speed for those objects). Therefore, we have to take the maximum.
											Abstract1 intoIfNew = tempIntoIf.assignCopy(man, "speed" + n.getNumber(), xp, null);
											String fakeVarName = "speed" + n.getNumber();
											Interval oldSpeedInterval = tempIntoIf.getBound(man, fakeVarName);
											double[] temp = new double[1];
											oldSpeedInterval.sup.toDouble(temp, 0);
											int oldSpeed = (int) temp[0];
											Texpr1Node rAr = new Texpr1CstNode(new MpqScalar(oldSpeed));
											Texpr1Intern xp = new Texpr1Intern(env, rAr);
											tempIntoIf.assign(man, "speed" + n.getNumber(), xp, null);
											tempIntoIf.join(man, intoIfNew);
										}
										speedSet.put("speed" + n.getNumber(), true);
									} catch (ApronException e) {
										e.printStackTrace();
									}								
								}
							});
						}
					}
				}
			}
			
			// Returning a Car
			else if (stmt instanceof JReturnStmt) {
				
				// Make sure this return statement is reachable (don't do anything otherwise)
				if (!inWrapper.get().isBottom(man) && ((JReturnStmt) stmt).getOp() instanceof Local) {
					Local returnValue = (Local) ((JReturnStmt) stmt).getOp();
					PointsToSetInternal p2ByRetStmt = (PointsToSetInternal) pointsToAnalysis.reachingObjects(returnValue);
					// Set the upper bound for the speed according to what we have in our Abstract
					final Abstract1 tempIntoIf = intoIf;
					p2ByRetStmt.forall(new P2SetVisitor() {
						@Override
						public void visit(Node n) {
							try {
								String fakeVarName = "speed" + n.getNumber();
								Interval speedInterval = tempIntoIf.getBound(man, fakeVarName);
								double[] temp = new double[1];
								speedInterval.sup.toDouble(temp, 0);
								int speed = (int) temp[0];
								carsReturned.put(fakeVarName, true);
								speeds.put(fakeVarName, Math.max(speeds.get(fakeVarName), speed));
							} catch (ApronException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			/* - code below propagates the intoIf and intoElse computed before (as side effect); 
			 * - no need to change anything below; 
			 * - just make sure that intoIf and intoElse vars are computed before; 
			*/
			for (Iterator<AWrapper> it = intoIfWrappers.iterator(); it.hasNext();) {
				AWrapper op1 = it.next();
				if (intoIf != null) {
					op1.set(intoIf);
				}
			}

			for (Iterator<AWrapper> it = intoElseWrappers.iterator(); it.hasNext();) {
				AWrapper op1 = it.next();
				if (intoElse != null) {
					op1.set(intoElse);
				}
			}

		} catch (ApronException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/* ======================================================== */

	/* no need to use or change the variables and methods below */

	/* collect all the local int variables in the class */
	private void recordIntLocalVars() {

		Chain<Local> locals = g.getBody().getLocals();

		int count = 0;
		Iterator<Local> it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			if (next.getType() instanceof IntegerType)
				count += 1;
		}

		local_ints = new String[count];

		int i = 0;
		it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			String name = next.getName();
			if (next.getType() instanceof IntegerType)
				local_ints[i++] = name;
		}
	}

	/* collect all the int fields of the class */
	private void recordIntClassVars() {

		Chain<SootField> ifields = jclass.getFields();

		int count = 0;
		Iterator<SootField> it = ifields.iterator();
		while (it.hasNext()) {
			SootField next = it.next();
			if (next.getType() instanceof IntegerType)
				count += 1;
		}

		class_ints = new String[count];

		int i = 0;
		it = ifields.iterator();
		while (it.hasNext()) {
			SootField next = it.next();
			String name = next.getName();
			if (next.getType() instanceof IntegerType)
				class_ints[i++] = name;
		}
	}
	
	// Find all the physical objects created by the program and create a 
	// fake speed variable for each of them
	private void recordCarVars() {
		Chain<Local> locals = g.getBody().getLocals();

		carVariables = new HashSet<String>();
		
		ArrayList<Local> carVariableLocals = new ArrayList<Local>();
		
		for (Local local : locals) {
			JimpleLocal jimpleLocal = (JimpleLocal) local;
			if (jimpleLocal.getType() instanceof RefType) {
				carVariableLocals.add(local);
			}
		}
		
		// We need to get the PointsToSet of every local Car variable and add an entry
		// for it in the environment
		for (Local local : carVariableLocals) {
			PointsToSetInternal p2set = (PointsToSetInternal) pointsToAnalysis.reachingObjects(local);
			p2set.forall(new P2SetVisitor() {
				@Override
				public final void visit(Node n) {
					String fakeVarName = "speed" + n.getNumber();
					carVariables.add(fakeVarName);
					speedSet.put(fakeVarName, false);
					carsReturned.put(fakeVarName, false);
					speeds.put(fakeVarName, Integer.MIN_VALUE);
				}
			});
		}
	}

	/* build an environment with integer variables */
	public void buildEnvironment() {

		recordIntLocalVars();
		recordIntClassVars();
		recordCarVars();

		String ints[] = new String[local_ints.length + class_ints.length + carVariables.size()];

		/* add local ints */
		for (int i = 0; i < local_ints.length; i++) {
			ints[i] = local_ints[i];
		}

		/* add class ints */
		for (int i = 0; i < class_ints.length; i++) {
			ints[local_ints.length + i] = class_ints[i];
		}
		
		/* add car speed variables */
		int i = 0;
		for (String carVariable : carVariables) {
			ints[local_ints.length + class_ints.length + i] = carVariable;
			i++;
		}

		env = new Environment(ints, reals);
	}

	/* instantiate the polyhedra domain */
	private void instantiateDomain() {
		man = new Polka(true);
	}

	/* constructor */
	public Analysis(UnitGraph g, SootClass jc) {
		super(g);

		this.g = g;
		this.jclass = jc;

		buildEnvironment();
		instantiateDomain();

		loopHeads = new HashMap<Unit, Counter>();
		backJumps = new HashMap<Unit, Counter>();
		for (Loop l : new LoopNestTree(g.getBody())) {
			loopHeads.put(l.getHead(), new Counter(0));
			backJumps.put(l.getBackJumpStmt(), new Counter(0));
		}
	}

	@Override
	protected AWrapper entryInitialFlow() {
		Abstract1 top = null;
		try {
			top = new Abstract1(man, env);
			// We have to initialize each of the speed variables to -inf, otherwise when entering
			// an if statement where the speed is set in the if-branch but not in the else-branch,
			// the upper bound after the if-statement will be +inf
			for (String varName : carVariables) {
				Texpr1Node constantNode = new Texpr1CstNode(new MpqScalar(Integer.MIN_VALUE));
				Texpr1Intern internalNode = new Texpr1Intern(env, constantNode);
				top.assign(man, varName, internalNode, null);
			}
		} catch (ApronException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return new AWrapper(top);
	}

	private static class Counter {
		int value;

		Counter(int v) {
			value = v;
		}
	}

	@Override
	protected void merge(Unit succNode, AWrapper in_w1, AWrapper in_w2, AWrapper out_w) {
		Counter count = loopHeads.get(succNode);

		Abstract1 in1 = in_w1.get();
		Abstract1 in2 = in_w2.get();
		Abstract1 out = null;

		try {
			if (count != null) {
				++count.value;
				if (count.value < WIDENING_THRESHOLD) {
					out = in1.joinCopy(man, in2);
				} else {
					out = in1.widening(man, in1.joinCopy(man, in2));
				}
			} else {
				out = in1.joinCopy(man, in2);
			}
			out_w.set(out);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	protected void merge(AWrapper in_w1, AWrapper in_w2, AWrapper out_w) {

		Abstract1 in1 = in_w1.get();
		Abstract1 in2 = in_w2.get();
		Abstract1 out = null;

		try {
			out = in1.joinCopy(man, in2);
		} catch (ApronException e) {
			e.printStackTrace();
		}

		out_w.set(out);
	}

	@Override
	protected AWrapper newInitialFlow() {
		Abstract1 bottom = null;

		try {
			bottom = new Abstract1(man, env, true);
		} catch (ApronException e) {
		}
		AWrapper a = new AWrapper(bottom);
		a.man = man;
		return a;

	}

	@Override
	protected void copy(AWrapper source, AWrapper dest) {
		try {
			dest.set(new Abstract1(man, source.get()));
		} catch (ApronException e) {
			e.printStackTrace();
		}
	}

	/* widening threshold and widening points */
	private static final int WIDENING_THRESHOLD = 6;
	private HashMap<Unit, Counter> loopHeads, backJumps;

	/* Soot unit graph */
	public UnitGraph g;
	public SootClass jclass;

	/* integer local variables of the method; */
	public String local_ints[];
	/* integer class variables where the method is defined */
	private String class_ints[];
	/* variables for the speed of various Cars */
	public HashSet<String> carVariables;

	/* real variables */
	public static String reals[] = { "x" };
}
