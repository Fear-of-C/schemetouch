package machination.webjava.trees.scheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;


/**
 * Wrapper Object for an actual Scheme implementation.  Holds the object's syntax
 * and memory of the Object held.  Holds some display information as well.
 * 
 * This does cause some duplication of the Scheme tree, but it holds additional information.
 * Unlike the Scheme tree, this tree holds references to parents, maintains parsed (syntax)
 * information, and can get access to peer Objects.  It also keeps metaenvironments.
 * 
 * A default Scheme Object is merely an empty request.  Anything more must have a type of
 * some sort.
 * 
 * @author nick
 *
 */
public class SchemeObject implements Comparable<SchemeObject>{
	
	public int childNum;

	protected SchemeObject parent;
	protected SchemeFactory factory;

	protected SchemeEnv environment;
	
	/**
	 * This can hold any number of possibilitySyntax.
	 * They get more specific up the stack.
	 * TODO: merge into matched
	 */
	protected Stack<PossibleSyntax> syntax;
	
	public void clearSyntax(){
		syntax.clear();
		startMatch();
	}
	
	public void configureSyntax(List<PossibleSyntax> sn){
		syntax.clear();
		syntax.addAll(sn);
	}
	
	public void addTopSyntax(PossibleSyntax ps){
		syntax.add(ps);
	}
	
	public void addBottomSyntax(PossibleSyntax ps){
		syntax.add(0, ps);
	}
	
	/**
	 * May cache a syntax matching.
	 */
	private SyntaxMatched matched;
	
	/**
	 * This Object should know its parent syntax and how
	 * its own syntax relates.
	 */
	private SyntaxRelationship role;
	
	private Set<ReplaceListener> replaces;
	
	public SchemeObject(SchemeObject parent){
		this(parent, parent.getFactory(), parent.getEnvironment());
	}
	
	public SchemeObject (SchemeObject parent, SchemeEnv environment){
		this(parent, parent.getFactory(), environment);
	}
	
	public SchemeObject(SchemeObject parent,
			SchemeFactory factory, SchemeEnv environment) {
		super();
		syntax = new Stack<PossibleSyntax>();
		replaces = new HashSet<ReplaceListener>();
		setParent(parent);
		this.factory = factory;
		setEnvironment(environment);
	}

	public void setParent(SchemeObject parent){
		if(this.parent != null){
			removeReplaceListener((ReplaceListener) parent);
		}
		this.parent = parent;
		if(parent != null){
			registerReplaceListener((ReplaceListener) parent);
		}
	}
	
	/**
	 * Method requires that the root possibilitySyntax must absolutely be
	 * at the bottom of the stack.
	 * @param role
	 */
	public void setRole(SyntaxRelationship role){
		this.role = role;
		if(role != null){
			setBottomSyntax(role.child);
		}
	}
	
	/**
	 * Adds a possibilitySyntax to the top of the syntax stack.  Checks compatibility with the previous
	 * syntax but does not match forward.
	 * 
	 * @param possibleSyntax
	 */
	public void addSyntax(PossibleSyntax possibleSyntax){
		if(syntax.isEmpty()){
			syntax.add(possibleSyntax);
			return;
		}
		PossibleSyntax previous = syntax.peek();
		if(!previous.match(possibleSyntax, this)){
			throw new IllegalArgumentException("Adding non-matching syntax " + possibleSyntax + " to " + previous);
		}
		syntax.add(possibleSyntax);
	}
	
	public void completeSyntax(){
		if(syntax.isEmpty()){
			//then we are almost certainly the root - act accordingly
			syntax.add(getFactory().getSyntax("body"));
		}
		//System.out.println("Starting with " + syntax);
		if(!syntax.peek().match(this)){
			//System.out.println(this.getDescriptionRecursive());
			throw new IllegalStateException("Failed to complete syntax for " + this);
		}
	}
	
	public void clearSyntaxRecursive(){
		clearSyntax();
		setEnvironment(null);
	}
	
	public void setBottomSyntax(PossibleSyntax child){
		syntax.clear();
		syntax.push(child);
	}

	public SchemeFactory getFactory() {
		return factory;
	}

	public Object getDatum(){
		return null;
	}
	
	public SchemeEnv getEnvironment() {
		return environment;
	}

	public void setEnvironment(SchemeEnv environment) {
		/*if(this.environment != null){
			removeReplaceListener(environment);
		}*/
		this.environment = environment;
		/*if(environment != null){
			registerReplaceListener(environment);
		}*/
	}


	public boolean isPair(){
		return false;
	}
	
	public boolean isList(){
		return false;
	}
	
	public boolean isSymbol(){
		return false;
	}
	
	public boolean isVector(){
		return false;
	}
	
	public SchemeSyntax getSyntax(){
		return matched == null ? null : matched.matchOriginal;
	}
	
	public SyntaxMatched startMatch(){
		return (matched = new SyntaxMatched());
	}
	
	public SyntaxMatched getMatch(){
		return matched;
	}
	
	public boolean schemeEquals(SchemeObject other){
		if(getDatum() == null){
			return this.equals(other);
		}
		return getDatum().equals(other.getDatum());
	}
	
	public Set<SchemeSymbol> getUsedSymbols(){
		return Collections.emptySet();
	}
	
	public SyntaxRelationship getRole(){
		return role;
	}

	public SchemeObject getParent() {
		return parent;
	}

	public String getString() {
		if(getDatum() != null){
			return getDatum().toString();
		}
		return "__unspecified";
	}
	
	/**
	 * Replaces this object in a tree.  Any children attached to this object
	 * stay with this object.
	 * 
	 * To remove the Object entirely, replace it with a null.
	 * 
	 * To add the Object, replace it with itself.
	 * 
	 * @param next
	 */
	public void replace(SchemeObject next){
		//copy the list in case we are replacing self
		//System.out.println("replacing: " + this + " -> " + next);
		List<ReplaceListener> rls = new ArrayList<ReplaceListener>(replaces);
		replaces.clear();
		if(parent != null){
			SchemeObject tempParent = parent;
			parent = null;
			next.setParent(tempParent);
			next.childNum = childNum;
		}else{
			throw new UnsupportedOperationException("Replacing only works when a parent exists.");
		}
		next.setRole(getRole());
		//give the incoming this object's environment and role
		//TODO: figure out why/if this is necessary
		next.setEnvironment(getEnvironment());
		if(getRole() != null){
			next.completeSyntax();
			if(getEnvironment() != null){
				if ((getMatch() != null) && getFactory().getSyntax("identifier").getSingleton().equals(getMatch().matchOriginal)){
					getEnvironment().deleteUse((SchemeSymbol) this);
				}
				//System.out.println(next.getMatch() + " -- " + next.getSyntaxStack());
				if((next.getMatch() != null) && getFactory().getSyntax("identifier").getSingleton().equals(next.getMatch().matchOriginal)){
					getEnvironment().registerUse((SchemeSymbol) next);
				}
			}
		}
		//System.out.println("rListeners: " + rls);
		for(ReplaceListener rl : rls){
			rl.gotReplace(this, next);
			next.registerReplaceListener(rl);
		}
	}
	
	public boolean isPlaceHolder(){
		//TODO: find out why this is needed.  Maybe if null = nil
		return (getDatum() == null) && !(getFactory().isNil(this));
	}
	
	/**
	 * Duplicates this object in the tree.  Designed for placeholders in infinite lists
	 * to add another instance.
	 * 
	 */
	public SchemeObject dupe(){
		if(getParent() == null){
			throw new IllegalStateException("Can't dupe with no parent.");
		}
		SchemeObject newObj = factory.cloneObject(getParent(), this, this.getEnvironment());
		newObj.setRole(getRole());
		copySyntaxTo(newObj);
		((SchemeList) getParent()).gotReplace(null, newObj);
		if(getEnvironment() != null){
			if ((getMatch() != null) && getFactory().getSyntax("identifier").getSingleton().equals(getMatch().matchOriginal) && (this instanceof SchemeSymbol)){
				getEnvironment().registerUse((SchemeSymbol) newObj);
			}
		}
		//what if the new object is a list?  then we must make sure that all children also have complete syntax
		newObj.completeSyntax();
		if(newObj instanceof SchemeList){
			((SchemeList) newObj).checkHead();
		}
		return newObj;
	}
	
	public void copySyntaxTo(SchemeObject o){
		o.configureSyntax(getSyntaxStack());
		if(getMatch() != null){
			o.startMatch().become(getMatch());
		}else{
			o.matched = null;
		}
	}
	
	/**
	 * Replaces this object with its unspecified variety.
	 */
	public void unSpecify(){
		if(isPlaceHolder()){
			return;
		}
		SchemeObject replacement = factory.holdSyntax(this, null, null, role);
		replace(replacement);
	}
	
	/**
	 * Basic removal method.  A bit flawed at this point, as it tends to mess up and redefine when it should remove.
	 * 
	 */
	public void delete(){
		if((getRole().num == SyntaxRelationship.INF) && (((SchemeList) getParent()).getByRel(getRole()).size() > 1)){
			if((getEnvironment() != null) && (getMatch() != null)){
				if (getFactory().getSyntax("identifier").getSingleton().equals(getMatch().matchOriginal)){
					getEnvironment().deleteUse((SchemeSymbol) this);
				}
			}
			
			for(ReplaceListener rl : new HashSet<ReplaceListener>(replaces)){
				rl.gotReplace(this, null);
			}
			replaces.clear();
			setParent(null);
			return;
		}
		
		unSpecify();
		return;
		
	}
	
	/**
	 * Registers uses in the environment.  Called after low-level construction of a scheme tree.
	 * Should also register when role specifies that it has a declscope, though that might be
	 * up to the parent.
	 */
	public void confirmExistence(){
		if((getEnvironment() != null) && (getMatch() != null)){
			if (getFactory().getSyntax("identifier").getSingleton().equals(getMatch().matchOriginal)){
				//System.out.println("I exist: " + this);
				getEnvironment().registerUse((SchemeSymbol) this);
			}
		}
	}
	
	public void registerReplaceListener(ReplaceListener r){
		replaces.add(r);
	}
	
	public void removeReplaceListener(ReplaceListener r){
		replaces.remove(r);
	}
	
	/**
	 * 
	 * @return a descriptive string for use in tree-like descriptions.
	 */
	public String getDescriptionRecursive(){
		return getString() + " " + (getMatch() == null ? null : getSyntax());
	}

	public Stack<PossibleSyntax> getSyntaxStack() {
		return syntax;
	}
	
	@Override
	public String toString(){
		return "SchemeObject: " + this.getString() + " " + this.hashCode();
	}

	public void setMatch(SyntaxMatched m) {
		this.matched = m;
	}

	@Override
	public int compareTo(SchemeObject arg0) {
		return (new Integer(hashCode())).compareTo(new Integer(arg0.hashCode()));
	}
}



