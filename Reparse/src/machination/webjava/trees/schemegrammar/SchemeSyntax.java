package machination.webjava.trees.schemegrammar;

import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

public abstract class SchemeSyntax implements MetaSyntax<SchemeObject>, Comparable<SchemeSyntax> {
	
	protected int priority;
	
	public SchemeSyntax(int priority){
		this.priority = priority;
	}
	
	public SchemeSyntax(){
		this.priority = 0;
	}
	
	/**
	 * 
	 * @return the priority of this SchemeSyntax in match search order.
	 */
	protected int getPriority(){
		return priority;
	}
	
	@Override
	public int compareTo(SchemeSyntax arg0) {
		if(getPriority() > arg0.getPriority()){
			return -1;
		}else if(getPriority() < arg0.getPriority()){
			return 1;
		}
		//now attempt by name
		int nameOrder = getName().compareTo(arg0.getName());
		if(nameOrder != 0){
			return nameOrder;
		}
		
		return (new Integer(hashCode())).compareTo(new Integer(arg0.hashCode()));
	}

	/**
	 * Structural matching method to be called when attempting to traverse down
	 * the parse tree.  May match the name, if one exists.
	 * 
	 * This method does NOT recur down the tree.  It should match almost instantly.
	 * This only works IF the Object tree is sufficiently laid out AND there is no
	 * ambiguity between structural matches.
	 * 
	 * It may match either based on children or on the type of Object in question
	 * (such as Number).
	 * 
	 * Might have to call this method from the objects themselves, as they know
	 * what type of object they are.
	 * 
	 * @return whether this syntax matches the given object.
	 */
	public abstract boolean matchStructure(SchemeObject o);

	public abstract Set<? extends SchemeObject> getPossible(SchemeObject owner);
	
	public abstract boolean matchUnscoped(SchemeObject toTry);
	
	public boolean matchToDepth(SchemeObject toTry, int depth){
		return this.matchStructure(toTry);
	}
	
	public boolean match(SchemeObject toTry) {
		SyntaxMatched matched = toTry.startMatch();
		if(this.matchUnscoped(toTry)){
			matched.matchOriginal = this;
			return true;
		}
		return false;
	}
	
	public abstract void setSymbol(SchemeSymbol s);

	public void setPriority(int i) {
		this.priority = i;
	}

	public SchemeSymbol getSymbol() {
		return null;
	}
}