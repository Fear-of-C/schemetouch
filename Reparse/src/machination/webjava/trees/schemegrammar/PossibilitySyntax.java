package machination.webjava.trees.schemegrammar;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import machination.webjava.trees.scheme.SchemeObject;

/**
 * 
 * Note that children may have 2 orders:
 * lexical order, and dependency order
 * 
 * Might be nice if we could have somewhere in the Scheme logic a way to get
 * full variable dependencies - this would give us a sequentialism graph,
 * and hence a parallelism availability calculator.
 * LONG LIVE VIPASSANA!
 * This should happen primarily in the object classes.  The syntax should merely
 * specify where declarations and scope changes occur.
 * 
 * Responsibilities:
 * 1) Saying where/when scope has been passed around or altered
 * 2) filtering the available children list
 * -what does it actually match?  SchemeObjects?  MetaObjects?  Other Syntax?
 * If matching objects, need some way to not fully fill them in
 * that moves the child request system into the scheme tree, away from the display
 * which is probably right
 * 
 * @author nick
 *
 */
public class PossibilitySyntax extends PossibleSyntax{
	
	//we are keeping possibilities in priority order, but what of children?  Can we sort those?
	//TODO: figure out if we want to re-arrange this for better information theoretic meaning
	private SortedSet<SchemeSyntax> possibilities;
	private Set<PossibleSyntax> children;


	private String name;
	
	protected PossibilitySyntax(){
		possibilities = new TreeSet<SchemeSyntax>();
		children = new TreeSet<PossibleSyntax>();
		
	}
	
	public PossibilitySyntax(String name){
		this();
		this.name = name;
	}
	
	public PossibilitySyntax(SchemeSyntax singleton){
		this(singleton.getName());
		possibilities.add(singleton);
	}

	public void addPossibility(SchemeSyntax toAdd){
		possibilities.add(toAdd);
	}
	
	/**
	 * Adds a child.
	 * 
	 * @param morePossible
	 */
	public void addPossibilities(PossibleSyntax morePossible){
		children.add(morePossible);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<SchemeSyntax> getPossible(SchemeObject position) {
		return possibilities;
	}

	public String toString(){
		return name;
	}

	@Override
	public Set<PossibleSyntax> getChildren(SchemeObject context) {
		return children;
	}
}

