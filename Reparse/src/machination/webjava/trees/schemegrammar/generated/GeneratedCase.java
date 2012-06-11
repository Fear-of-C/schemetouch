package machination.webjava.trees.schemegrammar.generated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;


/**
 * Implements a Scheme macro
 * 
 * What we need to do:
 * 1) Correctly Transform Scheme Macros.
 * 2) Transform for display (might not match source transformation).
 * 3) Propogate environment information.
 * 
 * @author nick
 *
 */
public class GeneratedCase extends SchemeSyntax{

	private SchemeList clause;
	
	private GeneratedPattern pattern;
	private Template template;
	private SchemeObject fender;
	
	/**
	 * Maps variable name to pattern var object.
	 */
	private Map<String, PatternVar> pVars;
	
	private Set<String> literals;
	
	private SchemeSymbol name;
	
	/**
	 * 
	 * @param name
	 * @param obj - corresponding to the case as it appears in Scheme
	 */
	public GeneratedCase(SchemeSymbol name, SchemeList clause, Set<String> literals) {
		this.clause = clause;
		this.literals = literals;
		this.name = name;
		this.pattern = new GeneratedPattern(name, clause.first(), literals);
		

		pVars = new HashMap<String, PatternVar>();
		for(PatternVar p : pattern.getPatternVars()){
			pVars.put(p.getString(), p);
		}
		
		if(clause.get(1).isList()){
			this.template = new ChildedTemplate(clause.get(1), pVars, clause.getEnvironment());
		}else{
			if(clause.get(1) instanceof SchemeSymbol){
				this.template = new TemplateVar(((SchemeSymbol) clause.get(1)), clause.getEnvironment());
			}else{
				template = new TemplateLiteral(clause.get(1));
			}
		}
		propagateScopes();
	}
	
	/**
	 * Method called in constructor after the pattern and template have been set.
	 * Takes the known scopes from the template and brings them into the pattern.
	 * This appears to be done as of now.
	 * 
	 * Should also propogate roles, such as whether something is a function call,
	 * whether it is the condition in an if, etc.
	 * 
	 * Note that some pattern variables might not even show up in template!
	 */
	private void propagateScopes(){ 
		Map<String, List<TemplateVar>> st = template.getVariables();
		for(PatternVar p : pVars.values()){
			List<TemplateVar> tVars = st.get(p.getString());
			p.addTemplates(tVars);
			p.accountForAdditionalScope(pVars);
		}
		//we've got scopes into the vars, now let's put them into the patterns
		pattern.configureScopes();
	}
	
	/**
	 * Steps:
	 * 1) Match pattern to object in such a way as to line up variables
	 * 2) Generated flagged (hygeinic) symbols and literals
	 * -must handle multiple applications of macros
	 * 3) Output the created Objects.
	 * 
	 * 
	 * @return a SchemeObject suitable for display to the user.  May make
	 * use of our expanded notation and annotations to symbols for hints
	 * that they are hygeinic and do not match external variables.
	 */
	public SchemeObject transformPrettily(SchemeObject toTransform){
		if(!pattern.match(toTransform)){
			throw new UnsupportedOperationException("Object to transform does not match this pattern.");
		}
		//matchedSyntax should now contain a map of pattern variable to matched symbol
		//now we get that map into a form which the template can use
		SyntaxMatched matchedSyntax = toTransform.getMatch();
		Map<String, List<SchemeObject>> roleNames = new HashMap<String, List<SchemeObject>>(matchedSyntax.getRoleMatches().size());
		for(Map.Entry<SyntaxRelationship, List<SchemeObject>> entry : matchedSyntax.getRoleMatches().entrySet()){
			roleNames.put(entry.getKey().role, entry.getValue());
		}
		
		return template.transform(toTransform.getParent(), roleNames, 0);
	}
	
	/**
	 * @param prettilyTransformed is a SchemeObject transformed for display
	 * 
	 * @return a SchemeObject that is the result of this case performing a transformation
	 * that will implement correct hygeine in a way that does not break the interpreter
	 */
	public SchemeObject transformCorrectly(SchemeObject prettilyTransformed){
		return null;
	}
	
	public String toString(){
		return "Generated macro: patt: " + pattern + " temp: " + template;
	}

	@Override
	public Set<? extends SchemeObject> getPossible(SchemeObject owner) {
		return pattern.getPossible(owner);
	}

	@Override
	public boolean matchStructure(SchemeObject o) {
		return pattern.match(o);
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		throw new UnsupportedOperationException("Can't change case symbol.");
	}

	@Override
	public String getName() {
		return pattern.getName();
	}

	@Override
	protected int getPriority() {
		return 1;
	}

	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		return pattern.match(toTry);
	}
	
	/**
	 * The new way forward:
	 * 1) We attach a "syntax cache" chain onto the end of case that includes elipses.
	 * This is one or more symbols out from the longest actual use of the macro, and
	 * provides the syntax for the suggest "next" object.  Since we do not expect
	 * users to insert lengthy macro chains, this should work adequately in most
	 * cases.  The only place where it's likely to fail is in the creation of huge cond
	 * statements and similar, such as when defining a large, phony "table" of
	 * equalities.  We might not worry about this at all in the very near future.
	 * In the far future, we might find a way to create such a chain with the assumption
	 * that no clause early in the frequency expansion maps to a literal or other
	 * class of closing/derailing symbols.
	 * 
	 * To think about: if we could create a non-linear cache chain (such
	 * as the frequency speedups), then we could both improve the efficiency
	 * of Scheme instant AND make progress on P vs. NP.
	 * 
	 * Either way, let's build the macroless version first.
	 * 
	 */
	@Override
	public boolean match(SchemeObject toTry) {
		return pattern.match(toTry);
	}
	
	/**
	 * This is currently planning to implement a breadth-first search
	 * All other macros should already be propagated and therefore take care of themselves.
	 * 
	 * This method shouldn't be here.  It should be in the generated.  Maybe in the handler.
	 * But no, it should be here, because the handler is supposed to be oblivious.
	 * 
	 * How do we detect stability?
	 */
	public static void propagateSyntax(Collection<PossibleSyntax> macros){
		List<GeneratedCase> caseQ = new ArrayList<GeneratedCase>();
		//the point of this is to create a list with some ordering, doesn't matter what
		for(PossibleSyntax cases : macros){
			for(SchemeSyntax c : cases.getPossible(null)){
				GeneratedCase gc = (GeneratedCase) c;
				caseQ.add(gc);
			}
		}
		
		while(true){
			int i = 0;
			for(GeneratedCase nextCase : caseQ){
				if(nextCase.propagateSyntax()){
					i++;
				}
			}
			if(i == caseQ.size()){
				break;
			}
		}
	}
	
	/**
	 * In theory, this should just work as is.
	 * 
	 * Next problem: the environment needs to call this to load in the syntax, but
	 * the syntax must be loaded for this to work recursively.
	 * 
	 * Note 1: this object CAN match without being propagated yet, so we move that step later.
	 * 
	 * Another problem: template variables are not adequately outputting literals.  Alternatively,
	 * template literals are not being used where they should be.
	 * 
	 * @return whether or not the syntax appeared stable
	 */
	public boolean propagateSyntax(){
		boolean changeFlag = false;
		SchemeObject templateGen = ((SchemeSyntax) template).getPossible(clause).iterator().next();
		//now we should recurisvely run down the object chain and see what was a pattern variable
		//Our first problem is that the template, as a syntax, will create things with template syntax
		//this should get ripped up and thrown away, so maybe we want to grab vars first!
		Map<String, List<SchemeObject>> varO = GeneratedCase.varObjects(templateGen);
		//TODO: find out if expr actualy does match anything!
		templateGen.addBottomSyntax(templateGen.getFactory().getSyntax("expr"));
		//warning: calling "match" here might be a bad idea, as this syntax is not yet in the environment!
		//we need to make sure that the environment recognizes the existence of this syntax before it uses it
		//which might be really, really hard
		System.out.println(templateGen.getDescriptionRecursive());
		if(!templateGen.getFactory().getSyntax("expr").match(templateGen)){
			throw new IllegalStateException("Generated Syntax " + this + " cannot propagate syntax");
		}
		System.out.println(templateGen.getDescriptionRecursive());
		//System.out.println(clause);
		//now we have the matched object, so we naively propagate from it
		//since we're dealing with vars, which syntax do we care about?  maybe the scheme syntax?
		Set<PatternVar> vars = pattern.getPatternVars();
		for(PatternVar var : vars){
			String varName = var.getName();
			List<SchemeObject> matchedTo = varO.get(varName);
			//System.out.println(varO);
			//SchemeSyntax theSyntax = matchedTo.iterator().next().getSyntax();
			//can this ever be indefinite?  might want to check bottom syntax
			//an expr possibilitySyntax should ALWAYS match a null datum!!!
			//System.out.println(varName);
			PossibleSyntax possibilities = matchedTo.iterator().next().getSyntaxStack().get(0);
			for(SchemeObject o : matchedTo){
				if(!o.getSyntaxStack().get(0).equals(possibilities)){
					//TODO: throw a better exception
					throw new IllegalStateException();
				}
			}
			//the bottom syntax is what's enforced by any syntax matching this - we want to use it!
			//anything on top of that is internal, probably useless
			if(!possibilities.equals(var.getChildren(templateGen).iterator().next())){
				var.changeSyntax(possibilities);
				changeFlag = true;
			}
		}
		return !changeFlag;
	}
	
	/**
	 * 
	 * 
	 * @param t
	 * @return mapping template syntax name string to the object  that actually uses it
	 */
	private static Map<String, List<SchemeObject>> varObjects(SchemeObject t){
		Map<String, List<SchemeObject>> r = new HashMap<String, List<SchemeObject>>();
		if(t.isList()){
			SchemeList l = (SchemeList) t;
			for(SchemeObject o : l){
				Map<String, List<SchemeObject>> childMap = varObjects(o);
				r.putAll(childMap);
			}
		}
		//System.out.println(r);
		//System.out.println(t);
		//System.out.println(t.getSyntax());
		if(!r.containsKey(t.getSyntax().getName())){
			r.put(t.getSyntax().getName(), new ArrayList<SchemeObject>());
		}
		r.get(t.getSyntax().getName()).add(t);
		return r;
	}
}















