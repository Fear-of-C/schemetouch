package machination.webjava.trees.schemegrammar;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;

/**
 * We will probably cache and flag this class rather heavily in order to reduce the
 * overhead of keeping so many map objects.
 * 
 * Thinking more now, we REALLY need this class to be recursive.
 * 
 * Ideas
 * 1) Matching based on relationships is awkward.
 * 2) Matching based on Syntax is painful and likely to break.
 * 3) Matching based on role is slightly painful, but needs uniqueness of roles.
 * 
 * Maybe this class should act in some sense like a reverse scope.  Things that
 * get matched in children get overriden in parents.  This seems to go well
 * with the notion of headfirst expansion at least in analogy.  If we can make
 * this more rigorous, it could tell us how to handle macro expansion scope.
 * 
 * Also note that as it currently stands, we can get the interesting binding
 * effects if we assume that generated templates create a new environment.
 * (I think).
 * 
 * Is the usedSymbols list potentially just a subset of the values of roleMatches?
 * 
 * Maybe it tracks alterations all the way up the hierarchy - it keeps a set of symbols
 * altered that are still in scope at this new level.
 * 
 * Implicit Contracts:
 * 1) Any declaration that appears multiple times with the same symbols might be overwritten.
 * If a parent overwrites it, the value is well-behaved.  When children overwrite each other,
 * it might not be well-defined which child "wins."
 * 
 * Note that we deal with peer conflicts in the environment.  If we define a variable and redefine
 * it elsewhere, each has its own matched, and the environment deals with which version ends up where.
 * 
 * We might also create a new SchemeSymbol for each time that a variable gets altered or redefined.
 * Hard to say about that right now - we don't want to accidentally imply separate bindings after a set!.
 *  
 * @author nick
 *
 */
public class SyntaxMatched {
	
	public SyntaxMatched(){
		roleMatches = new LinkedHashMap<SyntaxRelationship, List<SchemeObject>>();
	}
	
	public SyntaxMatched(SyntaxMatched[] children){
		this();
		for(SyntaxMatched child : children){
			roleMatches.putAll(child.roleMatches);
		}
	}
	
	public SchemeSyntax matchOriginal;
	
	public static SyntaxMatched aggregateHierarchy(SchemeObject root){
		SyntaxMatched r = new SyntaxMatched();
		if(root.isList()){
			for(SchemeObject child : ((SchemeList) root)){
				SyntaxMatched cm = aggregateHierarchy(child);
				r.getRoleMatches().putAll(cm.getRoleMatches());
			}
		}
		if(root.getMatch() != null){
			r.getRoleMatches().putAll(root.getMatch().getRoleMatches());
		}
		return r;
	}
	
	/**
	 * Maps by role string.  Do we even need this?  Could be convenient
	 * for getting a tree-like breakdown.  I think that this should extend
	 * peers and overwrite children, but right now, peers get overwritten
	 */
	private Map<SyntaxRelationship, List<SchemeObject>> roleMatches;
	
	//TODO: eliminate this double-dereferencing hack
	public void become(SyntaxMatched m){
		this.matchOriginal = m.matchOriginal;
		this.roleMatches = m.roleMatches;
	}
	
	/**
	 * Caches matched syntax during a structural matching phase.
	 */
	public Map<PossibleSyntax, SortedMap<SchemeSyntax, List<PossibleSyntax>>> cache =
		new HashMap<PossibleSyntax, SortedMap<SchemeSyntax, List<PossibleSyntax>>>();
	
	public void put(SyntaxRelationship s, List<SchemeObject> l){
		roleMatches.put(s, l);
	}
	
	public Map<SyntaxRelationship, List<SchemeObject>> getRoleMatches(){
		return roleMatches;
	}
	
	public List<SchemeObject> getFromRole(String r){
		for(SyntaxRelationship rel : getRoleMatches().keySet()){
			if(rel.role.equals(r)){
				return roleMatches.get(rel);
			}
		}
		return null;
	}
}
