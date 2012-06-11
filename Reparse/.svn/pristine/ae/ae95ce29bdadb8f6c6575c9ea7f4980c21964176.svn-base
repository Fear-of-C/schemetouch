package machination.webjava.trees.schemegrammar;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

/**
 * This is a trouble class.
 * 
 * 1) It overmatches in structural phase due to dynamicSyntax not always matching as much as it should.
 * 2) It has 2 ways to be childed - through defined procedures, and through the definition of a procedure.
 * This could be a good thing - one is structural, other is runtime matchable.
 * Maybe DynamicSyntax should do the same thing...
 * Identifiers do something similar by matching structurally.
 * 
 * Thinking this should map to 2 forms.  One of them is the standard one (created as part of the original
 * PossibilitySyntax).  The other looks into identifier space to see if it can match a function defined
 * somewhere in the environment.  We are going to wind up suggesting some wrong options.  That is
 * perfectly okay.  We are only in serious trouble if we exclude a correct possibility.  The user
 * should already be capable of filtering most of the stupid shit we suggest.
 * 
 * Required Features:
 * 1) Implements a lambda parser that can interpret lambda args.  Does not need to do anything else with lambdas.
 * 2) Has both its singular possibility AND a dynamic syntax that takes all objects which are in fact defined
 * as procedures in the environment.
 * 
 * Possibly Valuable Features:
 * 1) Ability to create procedure syntax from javascript method headers (useful in Biwa).
 * 
 * This is THE FUNDAMENTAL/DEFAULT syntax for lists.  If there is no defined syntax to match a list and no
 * special subsyntax overrides it, the the list is to be considered a procedure.  This is not a convention
 * of Scheme instant but a fundamental point about the nature of Scheme itself.
 * 
 * This is one of the first PossibleSyntax objects to use both a SchemeSyntax possibility AND a list of 
 * child PossibleSyntax objects.
 * 
 * @author nick
 *
 */
public class ProcedureSyntax extends PossibilitySyntax{

	@Override
	public Set<PossibleSyntax> getChildren(SchemeObject context) {
		if(context == null){
			return Collections.emptySet();
		}
		Set<PossibleSyntax> proceduresDefined = new LinkedHashSet<PossibleSyntax>();
		SchemeEnv current = context.getEnvironment();
		while(current != null){
			proceduresDefined.addAll(current.enumerateProcedures().values());
			current = current.getParent();
		}
		//System.out.println(proceduresDefined);
		return proceduresDefined;
	}

	public ProcedureSyntax(){
		super("baseprocedure");
	}
	
	public ChildedSyntax getCallByLambda(SchemeSymbol symbol, SchemeList lambda){
		return null;
	}
	/**
	 * 
	 * @return a childedSyntax that matches some expression plus whatever the
	 * given lambda is going to return.  This could be very useful if we
	 * have an expression of the form ((lambda stuff) more stuff)
	 */
	
	public ChildedSyntax getNamelessCallByLambda(){
		return null;
	}
	
	public static PossibleSyntax getCallByArgCounts(SchemeSymbol symbol, int min, int max){
		if(min == max){
			ChildedSyntax proc = new ChildedSyntax(symbol);
			for(int i = 0; i < min; i++){
				proc.addChild(new SyntaxRelationship(proc, symbol.getFactory().getSyntax("expr"),
						"arg" + i, 1));
			}
			//then the procedure takes a fixed # of arguments
			return new PossibilitySyntax(proc);
		}
		if(max == -1){
			//then we have an infininte-argument procedure
			ChildedSyntax proc = new ChildedSyntax(symbol);
			proc.addChild(new SyntaxRelationship(proc, symbol.getFactory().getSyntax("expr"),
					"arguments", -1));
			return new PossibilitySyntax(proc);
		}
		if(min < max){
			//this is a particularly annoying case
			PossibilitySyntax p = new PossibilitySyntax(symbol.getString());
			for(int argCount = min; argCount < max; argCount++){
				ChildedSyntax proc = new ChildedSyntax(symbol);
				for(int i = 0; i < argCount; i++){
					proc.addChild(new SyntaxRelationship(proc, symbol.getFactory().getSyntax("expr"),
							"arg" + i, 1));
				}
				p.addPossibility(proc);
			}
			return p;
		}
		throw new UnsupportedOperationException("Max arguments " + max + " is less than min " + min);
	}
}
