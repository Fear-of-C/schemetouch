package machination.webjava.trees.scheme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import machination.webjava.trees.schemegrammar.ChildedSyntax;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;


/**
 * Basic LISP list class - contains pairs.  This class overrides the Pair
 * display headers when active.
 * 
 * This class uses containment of a pair chain.  This means that while the pairs
 * may have the list's parent as their children, it can definitely not allow
 * them to register themselves as individual children (lest we muck up the
 * tree display).  It could either overwrite the parental chain entirely
 * or just be incompatible w/ self-registering children.  While the former
 * is cleaner, the latter lets us keep the pair chain intact, which might
 * be better in the long run.  An even better option would be to pretend
 * that the "list" Object acts as a ghost between the pair chain
 * and the parent.
 * 
 * Must define conventions between parents and children.
 * 1) Something can claim a parent
 * of which it is not actually a child in order to ready itself for
 * adoption.  This way, we can start building caches of possible ends
 * to trees for an evaluator/builder system.
 * 2) What happens if something denies parentage of an item claiming
 * it as a child?  It might go poorly to give an Object multiple parents,
 * as it must then try to inherit multiple environments.  Maybe we forbid
 * this and make add call setParent by default.
 * 3) We are so far unclear as to whether it is a list's responsibility to
 * distribute environments or the childrens' responsibility to find them.
 * -it may be difficult to propagate such info all the way up the chain
 * -the macro system wants symbols to hang onto their own required and
 * created scopes.  we should find a way to re-use whatever we can
 * -this would suggest that scope usage occurs at the lowest reasonable
 * level, which is actually high by normal standards
 * -should macros attempt to "factor" scope?  maybe later - it's not necessary
 * now; plus, pattern vars may become lists, so it works more like our current
 * system than not
 * -this implies that we create in this current system a way for any object
 * to run up the parent chain looking for scope
 * -all the work we did with syntax should take care of declarations - just need
 * to make sure that macros propagate these up the chain
 * 4) this class needs to adapt its syntax to match its car when necessary
 * 
 * 
 * @author nick
 *
 */
public class SchemeList extends SchemeObject implements Iterable<SchemeObject>, ReplaceListener{
	
	public SchemeList(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment) {
		super(parent, factory, environment);
		createLastEnv();
		constituents = new LinkedList<SchemeObject>();
	}
	
	public SchemeList(SchemeList predecessor, int location) {
		super(predecessor.parent, predecessor.factory, predecessor.environment);
		createLastEnv();
		constituents = predecessor.constituents.subList(location, predecessor.size());
	}
	
	public void createLastEnv(){
		lastEnv = new DefaultSchemeEnv(getEnvironment());
	}
	
	/**
	 * Stores a pair chain.  The first item in the pair must have as its
	 * parent this list.
	 */
	protected List<SchemeObject> constituents;
	protected boolean proper = true;
	
	private SchemeSymbol head;
	
	public SchemeSymbol getHead(){
		return head;
	}
	
	public boolean isProper() {
		return proper;
	}

	public void setProper(boolean proper) {
		this.proper = proper;
	}
	
	public List<SchemeObject> getByRel(SyntaxRelationship rel2) {
		return getMatch().getRoleMatches().get(rel2);
	}
	
	/**
	 * This method should always be true in most implementations.
	 */
	public boolean isList(){
		return true;
	}
	
	public String getHeadString(){
		if(head == null){
			if(getSyntax() == null){
				return "list";
			}
			return getSyntax().getName();
		}
		return head.getString();
	}
	
	//TODO: figure out how to deal with dotted lists that are actually proper when expanded
	//(+ 1 . (2 3 4))
	public boolean isProperList(){
		return factory.isNil(constituents.get(constituents.size() - 1));
	}
	
	public boolean hasPair(){
		return constituents.size() > 0;
	}
	
	/**
	 * Convenience method for efficiently appending things to the front of a list.  Only
	 * efficient when using a linked list.
	 * 
	 * This is the low level way to do this.  The higher level method is through the
	 * add method.  This method does not do anything with syntax and assumes that the
	 * list is being built up.
	 *  
	 * @param front
	 */
	public void shift(SchemeObject front){
		front.setParent(this);
		constituents.add(0, front);
	}
	
	public void checkHead(){
		if((getSyntax() != null) && getSyntax().hasSymbol() && (head == null)){
			head = (SchemeSymbol) constituents.remove(0);
			head.setBottomSyntax(null);
		}
		if((head != null) && (getSyntax() != null) && !getSyntax().hasSymbol()){
			constituents.add(0, head);
			head = null;
		}
		for(SchemeObject child : constituents){
			if(child instanceof SchemeList){
				((SchemeList) child).checkHead();
			}
		}
	}
	
	public void uncheckHead(){
		if(head != null){
			constituents.add(0, head);
			head = null;
		}
		for(SchemeObject child : constituents){
			if(child instanceof SchemeList){
				((SchemeList) child).uncheckHead();
			}
		}
	}
	
	@Override
	public void completeSyntax() {
		uncheckHead();
		super.completeSyntax();
		checkHead();
		confirmExistence();
	}

	/**
	 * Recurs down the tree, setting syntax and environment (which is itself
	 * necessary for syntax).
	 * 
	 */
	/*
	@Override
	public void structureSyntax(){
		
		super.structureSyntax();
		SyntaxMatched sm = getMatch();
		//now we need the environment to start working
		
		//we don't really count it as a child
		
		
		//SchemeEnv current = getEnvironment();
		//now recur down to children
		//PROBLEM: we must go down list in order!!!
		for (Map.Entry<SyntaxRelationship, List<SchemeObject>> entry : sm.getRoleMatches().entrySet()){
			//children.put(entry.getKey(), entry.getValue());
			for(SchemeObject child : entry.getValue()){
				//System.out.println("child " + child.getString() + " in role " + entry.getKey());
				child.setRole(entry.getKey());
				//child.setEnvironment(getSyntax().getEnvironmentForChild(this, child, current));
				//child.structureSyntax();
				//current = getSyntax().getEnvironemntFromChild(this, child, current);
			}
		}
		
	}
	*/

	public ChildedSyntax getSyntax(){
		return (ChildedSyntax) super.getSyntax();
	}

	@Override
	public Iterator<SchemeObject> iterator() {
		return constituents.iterator();
	}
	
	public SchemeObject first(){
		return constituents.get(0);
	}
	
	public SchemeObject get(int index){
		return constituents.get(index);
	}
	
	public SchemeList rest(int position){
		return new SchemeList(this, position);
	}
	
	public int size(){
		return constituents.size();
		//TODO: check if improper lists work correctly
	}
	
	/**
	 * Low-level add.  External methods should replace with a null
	 * 1st argument.
	 * 
	 * Thinking that we should add to the end of the "others" list.
	 * Do not append, as appending will cause bugs when there exist syntaxes after the object.
	 * 
	 * @param o
	 */
	protected void add(SchemeObject o){
		if(!(o.getRole().num == SyntaxRelationship.INF)){
			throw new UnsupportedOperationException("May not add another " + o.getRole().role + " to " + this.getSyntax());
		}
		List<SchemeObject> others = getByRel(o.getRole());
		SchemeObject placeHolder = others.get(others.size() - 1);
		int totalIndex = constituents.indexOf(placeHolder) + 1;
		int syntaxIndex = others.size();
		//always add another object as if it were the new placeholder object
		constituents.add(totalIndex, o);
		others.add(syntaxIndex, o);
		if((o.getSyntax() instanceof ChildedSyntax) && ((ChildedSyntax) o.getSyntax()).hasOutscopes()){
			getSyntax().getEnvironmentFromChild(this, o, getEndEnvironment());
		}
		//when adding an object, must make sure that any scope puts get propagated thoroughly
		//next, we check to see if we must add variables to an internal scope - but a higher parent may own this
		//for now, we could hack by just propagating up the tree in a separate method
		checkScope(o);
		if(!o.isPlaceHolder()){
			o.confirmExistence();
		}
		//System.out.println("Added " + o + " to " + constituents + " r " + getMatch().getRoleMatches());
		
		return;
	}
	
	public void copySyntaxTo(SchemeObject o){
		super.copySyntaxTo(o);
		uncheckHead();
		((SchemeList) o).uncheckHead();
		Iterator<SchemeObject> me = iterator();
		Iterator<SchemeObject> it = ((SchemeList) o).iterator();
		while(me.hasNext()){
			me.next().copySyntaxTo(it.next());
		}
		checkHead();
		((SchemeList) o).checkHead();
	}
	
	private void checkScope(SchemeObject o){
		if((getSyntax() != null)){
			getSyntax().resetDecls(this);
		}
		if(getParent() != null){
			((SchemeList) getParent()).checkScope(this);
		}
	}
	
	/**
	 * Low-level remove.  External classes should call replace with a null
	 * 2nd argument.s
	 * @param o
	 */
	protected void remove(SchemeObject o){
		constituents.remove(o);
		//System.out.println("removing from " + this + " R " + getByRel(o.getRole()));
		getByRel(o.getRole()).remove(o);
		
	}
	
	@Override
	public void confirmExistence(){
		for(SchemeObject child : constituents){
			child.confirmExistence();
		}
	}
	
	@Override
	public void clearSyntax(){
		super.clearSyntax();
		for(SchemeObject child : this){
			child.clearSyntax();
		}
	}

	@Override
	public final void gotReplace(SchemeObject old, SchemeObject n) {
		if(old == null){
			//then we are inserting a new object
			add(n);
			return;
		}
		if(n == null){
			//then we are removing an object
			remove(old);
			return;
		}
		//then we have a true replace
		replaceObject(old, n);
	}
	
	@Override
	public void delete(){
		super.delete();
		checkHead();
		for(int i = size() - 1; i >= 0 ; i--){
			constituents.get(i).delete();
		}
	}
	
	@Override
	public final void replace(SchemeObject next){
		//System.out.println("I, " + this + " am being replaced: " + constituents + " by " + next);
		super.replace(next);
		checkHead();
		for(int i = size() - 1; i >= 0; i--){
			constituents.get(i).delete();
		}
		//System.out.println("I, " + this + " have been replaced " + constituents + " by " + next);
	}
	
	@Override
	public void clearSyntaxRecursive(){
		super.clearSyntaxRecursive();
		for(SchemeObject child : constituents){
			child.clearSyntaxRecursive();
		}
	}
	
	/**
	 * Represents that a datum (but not its owning SchemeObject) was replaced.
	 * This happens when a list replaces its car.
	 * 
	 * This method should only be called when the object at the location of owner
	 * is no longer, in fact, the datum owned by owner.  Furthermore, convention dictates
	 * that it only occur when the object in question has not changed its value in the
	 * logical tree.  So if a number self-replaces by another number, it should full
	 * replace in the hierarchy.
	 * 
	 * @param owner
	 */
	public void datumReplaced(SchemeObject owner){
		//base implementation does not hold a datum
	}
	
	protected void replaceObject(SchemeObject old, SchemeObject n){
		checkClone(old, n);
		uncheckHead();
		int prevLocation = constituents.indexOf(old);
		if(prevLocation < 0){
			throw new UnsupportedOperationException(this + " never contained " + old);
		}
		//System.out.println("Actually replacing in list");
		//System.out.println(getDescriptionRecursive());
		constituents.set(prevLocation, n);
		int oldRoleLoc = getByRel(old.getRole()).indexOf(old);
		try{
			getByRel(old.getRole()).set(oldRoleLoc, n);
		}catch(UnsupportedOperationException e){
			//TODO: this is a stupid hack to get rid of singleton lists
			getMatch().put(old.getRole(), new ArrayList<SchemeObject>(getByRel(old.getRole())));
			getByRel(old.getRole()).set(oldRoleLoc, n);
			
		}
		
		//System.out.println("Object getting replaced.");
		if((n.getSyntax() instanceof ChildedSyntax) && ((ChildedSyntax) n.getSyntax()).hasOutscopes()){
			//removing should already be taken care of by the self-deletionism of destroyed lists
			//System.out.println("found outscopes from " + this + " -> " + n + " ; " + this.getSyntax() + " -> " + n.getSyntax());
			//but we must ensure that are subsequent syntax adds its outscopes effectively
			//TODO: in fact, we might move this functionality in the matching so that it happens naturally
			//System.out.println(n.getDescriptionRecursive());
			getSyntax().getEnvironmentFromChild(this, n, getEndEnvironment());
		}
		checkHead();
	}
	
	/**
	 * Checks if the list is a placeholder list, meaning that it holds
	 * only placeholder objects.
	 */
	public boolean isPlaceHolder(){
		if(getFactory().isNil(this)){
			return false;
		}
		uncheckHead();
		for(SchemeObject child : constituents){
			if(!child.isPlaceHolder()){
				return false;
			}
		}
		checkHead();
		return true;
	}
	
	public void checkClone(SchemeObject old, SchemeObject n){
		/*in reference implementation, checks if we need to clone self
		//this occurs especially if this object is no longer a valid placeholder
		//which happens when a child gets filled in - any child!
		 * may have to deal with ((null null) null) and replace outer, not inner list
		 * 
		 * could check for
		 * 1) are we an infinite object?
		 * 2) were we previously a placeholder?
		 * -defined somewhat recursively, especially down multiple levels
		 * 
		 * also, want the new placeholder to fall in after this one - this stops being a placeholder
		 * 
		 * Problem is that this object sometimes gets called when it shouldn't - it should only
		 * activate when clearly the entire hierarchy is a placeholder
		*/
		if(isPlaceHolder() && !n.isPlaceHolder() && (getRole() != null)){
			SchemeList parent = (SchemeList) getParent();
			//check parent first as not to make clones of clones
			parent.checkClone(old, n);
			if(getRole().num == SyntaxRelationship.INF){
				dupe();
			}
		}
	}
	
	/**
	 * 
	 * @return the constituents object, for classes that need to monitor it for changes
	 */
	public List<SchemeObject> accessConstituents(){
		return constituents;
	}
	
	@Override
	public String getDescriptionRecursive(){
		String descrip = "List" + getString() +" " + (getMatch() == null ? null : getSyntax()) + ":\n\t";
		for(SchemeObject child : this){
			assert(child != this);
			//System.out.println(this + " _ " + child + " _____ " + this.getSyntax());
			String cd = child.getDescriptionRecursive();
			cd = cd.replace("\n", "\n\t");
			descrip += cd + "\n\t";
		}
		descrip = descrip.substring(0, descrip.length() - 2);
		return descrip;
	}

	public SchemeEnv lastEnv;
	
	/**
	 * This holds a separate environment in which children may add definitions.
	 * 
	 * @return the environment after the last child has been parsed.
	 */
	public SchemeEnv getEndEnvironment() {
		return lastEnv;
	}
}

