package machination.webjava.trees.schemegrammar.generated;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;

/**
 * Represents symbols introduced during a template expansion that are sometimes not pattern vars.
 * 
 * Has to deal with scope availability (which propogates back to pattern).  May use
 * the minimum available scope, since 
 * 
 * @author nick
 *
 */
public class TemplateVar extends SchemeSyntax implements Template{

	protected SchemeSymbol origin;
	protected SchemeEnv baseEnv;
	
	public TemplateVar(SchemeSymbol s, ChildedTemplate t){
		this(s, t.getBaseEnv());
	}
	
	public TemplateVar(SchemeSymbol s, SchemeEnv environment) {
		origin = s;
		baseEnv = environment;
	}

	/**
	 * Assumes that this variable was never matched but exists in the template anyway.
	 * 
	 * Beware of macro expansions that create a variable in the template.
	 * 
	 * @param matchedTo
	 * @return a SchemeObject ready for insertion in place of this template var
	 */
	public SchemeObject emit(SchemeObject parent){
		SchemeEnv newEnv = parent != null ? parent.getEnvironment() : origin.getEnvironment();
		//System.out.println(origin + " - " + origin.getBindingEnv());
		//System.out.println(origin.getEnvironment());
		SchemeObject r = null;
		if((origin.getBindingEnv() == null) || origin.getBindingEnv().equals(newEnv.bindingEnvironment(origin))){
			//then we are safe to dump this in without trouble
			r = origin.getFactory().cloneObject(parent, origin, origin.getEnvironment());
		}
		//otherwise
		if(newEnv.includes(origin.getBindingEnv())){
			//then the symbol has been shadowed - signal a name change
			r = origin.getFactory().lowerEnvironmentSymbol(parent, origin);
		}
		if(r == null){
			System.out.println(origin.getBindingEnv() + " - " + newEnv);
			//if newEnv does not include the original binding, this operation was illegal
			throw new IllegalStateException("Can't use template symbol outside its environment.");
		}
		r.startMatch().matchOriginal = this;
		return r;
	}
	
	/**
	 * Assuming this variable exists in the pattern, takes the object it matched to and its
	 * new parent.
	 * 
	 * This matched to might not be a symbol.
	 * 
	 * Don't worry about generating a special breed of Object in here, since there is
	 * no such thing as shadowing when we're grabbing a symbol from the expand-time
	 * environment.
	 * 
	 * @return a SchemeObject ready for insertion in place of this template var
	 */
	public SchemeObject replace(SchemeObject parent, SchemeObject matchedTo){
		return parent.getFactory().cloneObject(parent, matchedTo, parent.getEnvironment());
	}
	
	@Override
	public SchemeObject transform(SchemeObject parent, Map<String, List<SchemeObject>> matched, int position){
		if(matched.containsKey(origin.getString())){
			return replace(parent, matched.get(origin.getString()).get(position));
		}
		return emit(parent);
	}
	
	/**
	 * Returns the set of variables defined in the pattern that are available to this template variable.
	 * This might not form into any sort of convenient scoping group, as we may have situations where
	 * the macro transformer spreads out template variables contained within a let or lambda into
	 * places in a pattern that don't even follow normal scoping conventions.
	 * 
	 * @param pVars - a mapping of variable name to pattern variable - used because this class
	 * only knows the names of template vars
	 * @return a set of pattern variables that will be available to this template variable after
	 * the transformation
	 */
	public Set<PatternVar> getAvailable(Map<String, PatternVar> pVars){
		Set<SchemeSymbol> definedDifference = baseEnv.getDifference(origin.getEnvironment());
		Set<PatternVar> available = new HashSet<PatternVar>(definedDifference.size());
		for(SchemeSymbol sym : definedDifference){
			if(pVars.containsKey(sym.toString())){
				available.add(pVars.get(sym.toString()));
			}
		}
		return available;
	}

	@Override
	public Set<? extends SchemeObject> getPossible(SchemeObject owner) {
		return Collections.singleton(emit(null));
	}

	/**
	 * A template symbol should never have to match.
	 */
	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		return false;
	}

	@Override
	public Map<String, List<TemplateVar>> getVariables() {
		return Collections.singletonMap(origin.getString(), Collections.singletonList(this));
	}

	@Override
	public String getName() {
		return origin.getString();
	}

	@Override
	public boolean matchStructure(SchemeObject o) {
		return true;
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		throw new UnsupportedOperationException("Template vars are not symboled.");
	}
	
}




