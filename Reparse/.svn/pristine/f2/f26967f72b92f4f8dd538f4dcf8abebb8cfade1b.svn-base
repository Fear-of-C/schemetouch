package machination.webjava.trees.schemegrammar.generated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;

/**
 * 
 * This class does much of the heavy lifting for pattern matching.  It has to decide
 * what to do with hygeinic macro expanses to avoid accidental scope capture.
 * 
 * Now understand almost enough to implement the lazy expansion system described in the
 * paper.  Only thing is, we have 2 transformations: 1 for the user, 1 for the compiler.
 * Then again, might be best to use same algorithm for both, since the user should be
 * able to see that scopes don't overlap.
 * 
 * Second problem is scope propogation from template to pattern.  This is also rather
 * intense, but we just have to analyze the template in the macro's current environment
 * and ensure that it doesn't spit out any macros we haven't seen yet (or make a 2-pass parse).
 * Note that with structure, things may uppropogate in extremely complex and interesting ways.
 * 
 * What if we define new notation to refer to different versions of symbols?  For example, a macro
 * expansion could produce macroname' and macroname$?  Maybe we could alter the lexical structure of
 * the language to create certain macro-legal syntax with which the user cannot interfere.  This could
 * mirror syntax highlighting in some sense.
 * 
 * The automatic legality checks should prevent use of macros that use symbols defined outside of
 * their enclosing environment.
 * (let-syntax ((divide (lambda (x )
 * (let ((/ +))
 * (syntax-case x ()
 * (( e 1 e 2 ) (syntax (/ e 1 e 2 ))))))))
 * (let ((/ ∗)) (divide 2 1)))
 * The above code returns a syntax-case that always has a variable that has gone out of scope.  In
 * reality, it should just break.  How do we break this reliably?  Maybe we can put additional
 * restrictions on the kind of functions that can appear within a transformer (stipulate that any
 * generated macros must be usable outside of the function if the function appears inside of
 * any sort of syntax definition).  This seems sketchy, but it may work if we have a good function
 * tracking system.  The question is: can we definitely track all user-generated functions in this
 * way?  Alternatively we could, upon finding that a syntax-case is in use, prevent returning it
 * outside of any scope that it relies upon.  This means that we can't set! something to a syntax-case
 * inside of certain let statements, can't return one out of a lambda in some cases, etc.
 *
 * 
 * What to display to the user?
 * 1) Must be readable (no "G1" identifier).
 * 2) Must be capable of showing non-capture.
 * 3) Should not rely on color or other design anomolies. 
 * 
 * What to pass into the interpreter?
 * 1) Must not use non-standard language features.
 * 2) Must expand performantly.
 * 
 * Can we merge these?
 * The only major problem seems to be the use of things in
 * the macro which may have been shadowed in the expansion environment.
 * (let (if #f) (or2 #t #t))
 * Use the fact that we know where all the enviroments are.  We can find that
 * "if" and at compile time, figure out what happens to it.  Can probably
 * do this in the reverse parse phase, and keep some of that info around.
 * 
 * Rules:
 * 1) Any variable that exists in the pattern, we do not bother with.  It's named
 * by the expand-time environment and should therefore become that symbol (without renaming?).
 * 2) Any variable that comes purely from the template, we 
 * check against the expansion-time environment to see if it's been remapped,
 * and if it has, rename it in all occurrences.
 * 3) Any pattern literals should fail to match if defined in the expand-time environment,
 * since they have stopped being literals.  It should probably be illegal to define
 * a literal that exists in the macro definition's environment.
 * 4) Any literals or quoted expressions in the template do absolutely nothing.  We don't
 * remap them or try to change the display.
 * 
 * Problem: if this matches a list, then what happens when the list casts it to a ChildedSyntax?
 * 
 * Should it really be a possibleSyntax?
 * 
 * @author nick
 *
 */
public class PatternVar extends PossibleSyntax{
	
	private SchemeSymbol origin;
	
	public PatternVar(SchemeSymbol origin, GeneratedPattern p) {
		super();
		this.origin = origin;
		this.pattern = p;
		templateVars = new HashSet<TemplateVar>(1);
		//System.out.println("Crated Pattern Var " + origin.getString());
	}

	/**
	 * The symbol in the pattern that created this.
	 */
	private GeneratedPattern pattern;
	
	/**
	 * Should hold all template variables with the same name as this pattern variable.
	 * They may differ in scope or syntactic meaning.
	 * A pattern variable should never appear twice, so this one-to-many makes sense.
	 */
	private Set<TemplateVar> templateVars;
	
	/**
	 * Holds other pattern variables that will give scope to all uses of this variable
	 */
	private Set<PatternVar> additionalScope;
	
	/**
	 * 
	 * @param toAdd - template symbol which this could match to
	 */
	public void addTemplate(TemplateVar toAdd){
		templateVars.add(toAdd);
	}
	
	public void addTemplates(Collection<TemplateVar> tVars){
		//System.out.println(tVars);
		templateVars.addAll(tVars);
	}
	
	public String getString(){
		return origin.getString();
	}
	
	public void changeSyntax(PossibleSyntax newChild){
		this.getChildren(null).clear();
		this.getChildren(null).add(newChild);
	}
	
	/**
	 * 
	 * 
	 * @return a set of pattern-symbols that will match to whatever could be
	 * in this environment but is not in the defining environment of the
	 * syntax-case
	 */
	public void accountForAdditionalScope(Map<String, PatternVar> pVars){
		Set<PatternVar> availableScope = null;
		Iterator<TemplateVar> it = templateVars.iterator();
		if(it.hasNext()){
			availableScope = new HashSet<PatternVar>(it.next().getAvailable(pVars));
		}else{
			additionalScope = Collections.emptySet();
		}
 		while(it.hasNext()){
 			availableScope.retainAll(it.next().getAvailable(pVars));
 		}
 		additionalScope = availableScope;
	}
	
	@Override
	public String toString(){
		return "pVar " + origin.getString();
	}

	@Override
	public Set<PossibleSyntax> getChildren(SchemeObject context) {
		return Collections.singleton(context.getFactory().getSyntax("expr"));
	}

	@Override
	public String getName() {
		return origin.getString();
	}

	@Override
	public Set<SchemeSyntax> getPossible(SchemeObject position) {
		return Collections.emptySet();
	}
}
