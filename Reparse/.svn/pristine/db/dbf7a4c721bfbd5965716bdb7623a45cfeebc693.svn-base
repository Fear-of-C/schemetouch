package machination.webjava.trees.schemegrammar.generated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.PossibilitySyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;

/**
 * Needs to augment environment with new syntax.  Which class takes responsibility for doing so?
 * This class can know how the structure works, but it can't deal too hard with the environment.
 * Might need to add this capability into... hmm... it could exist in the childrens' environment
 * handler methods.  This will be handed some standard ChildedSyntax and such to work with.
 * How can we augment?
 * 
 * Either need to alter the childedSyntax objects added (a pain in the ass, but might work
 * better with procedures anyway), or somehow grab more info from the PossibilitySyntax.
 * 
 * This will appear atop the stack.  That is possibly useful.  The problem is that it
 * really needs to put the info into the let or define statement enclosing it.
 * 
 * We could add code to check whether a definition is syntactic within the environment.
 * This seems to be the fastest way for now.
 * 
 * @author nick
 *
 */
public class SyntaxRulesHandler extends PossibilitySyntax{

	public SyntaxRulesHandler() {
		super("syntax-rules");
	}
	
	public static boolean syntaxReady(SchemeObject o){
		//System.out.println(" ready? " + (o.getMatch().getFromRole("\"rule\"") != null) + " - " + o.getMatch().getRoleMatches());
		return o.getMatch().getFromRole("\"rule\"") != null;
	}
	
	public static PossibilitySyntax getSpecified(SchemeSymbol name, SchemeObject o){
		SyntaxMatched matched = o.getMatch();
		//System.out.println("specifying " + name.getString() + " - " + o.getString());
		//System.out.println(matched.getRoleMatches());
		List<SchemeObject> literalList = matched.getFromRole("\"patternLiteral\"");
		Set<String> literals = new HashSet<String>();
		//TODO: find out why this doesn't break thing up
		for(SchemeObject lit : ((SchemeList) literalList.get(0))){
			literals.add(lit.getString());
		}
		PossibilitySyntax p = new PossibilitySyntax(name.getString());
		for(SchemeObject rule : matched.getFromRole("\"rule\"")){
			p.addPossibility(new GeneratedCase(name, (SchemeList) rule, literals));
		}
		//System.out.println("asked to create syntax " + p);
		return p;
	}
}
