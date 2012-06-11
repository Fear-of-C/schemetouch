package machination.webjava.trees.schemegrammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

/**
 * @author nick
 *
 */
public class ChildedSyntax extends SchemeSyntax{
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	protected ChildedSyntax(){
		super();
		outScopes = new HashSet<String>();
		newScopes = new HashMap<SyntaxRelationship, Set<String>>();
		varDeclarations = new HashMap<String, VariableCreation>();
		children = new ArrayList<SyntaxRelationship>();
	}

	public ChildedSyntax(String name) {
		this();
		this.name = name;
	}
	
	public ChildedSyntax(SchemeSymbol sym){
		this(sym.getString());
		setSymbol(sym);
	}
	
	@Override
	public void setSymbol(SchemeSymbol sym){
		symbol = sym;
		this.priority = 1;
	}
	
	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}
	
	public boolean hasSymbol(){
		return symbol != null;
	}

	private boolean override;

	/**
	 * If this is non-null, then it represents the head of the list.
	 */
	private SchemeSymbol symbol;
	
	
	
	/**
	 * Obvious
	 */
	private List<SyntaxRelationship> children;
	
	/**
	 * Associates a variable's creating syntax with its defining syntax.
	 * @author nick
	 *
	 */
	private class VariableCreation{
		
		
		public VariableCreation(List<SyntaxRelationship> declarationChain,
				List<SyntaxRelationship> definitionChain) {
			super();
			this.declarationChain = declarationChain;
			this.definitionChain = definitionChain;
		}
		public List<SyntaxRelationship> declarationChain;
		public List<SyntaxRelationship> definitionChain;
		
		public String toString(){
			return "VC " + declarationChain + " - " + definitionChain;
		}
	}
	
	/**
	 * Used by children to look up things that should exist in scope.
	 * 
	 * Declarations may hold anything that will be declared here or in a child.
	 * 
	 * May declare the same thing in more than 1 child, in which case correct behavior
	 * is to append it multiple times when matching syntax.
	 * 
	 * **/
	private Map<String, VariableCreation> varDeclarations;
	
	public void configureVarDecls(Map<String, List<SyntaxRelationship>> dec, Map<String, List<SyntaxRelationship>> def){
		for(String entryKey : dec.keySet()){
			varDeclarations.put(entryKey, new VariableCreation(dec.get(entryKey), def.get(entryKey)));
		}
		//System.out.println(" configuring scopes with " + dec + " __ " + def + "\t\t\t\t\t\t" + this);
	}
	
	public boolean declaresVar(SchemeObject child){
		return varDeclarations.containsKey(child.getRole().role);
	}
	
	public void resetDecls(SchemeList owner){
		//System.out.println("all new scopes: " + newScopes.entrySet());
		for(Map.Entry<SyntaxRelationship, Set<String>> ns  : newScopes.entrySet()){
			//System.out.println("relationship " + ns.getKey().role + " -> " + owner.getByRel(ns.getKey()));
			for(SchemeObject obj : owner.getByRel(ns.getKey())){
				//System.out.println("resetting declarations for " + obj);
				for(String roleToReset : ns.getValue()){
					//System.out.println("reset: " + getVarDecl(owner, roleToReset));
					obj.getEnvironment().putAll(getVarDecl(owner, roleToReset));
				}
			}
		}
	}
	
	/**
	 * Holds all roles that will get put into the containing scope.
	 */
	private Set<String> outScopes;
	
	public void addOutScope(String name){
		outScopes.add(name);
	}
	
	public boolean hasOutscope(SyntaxRelationship role){
		return outScopes.contains(role.role);
	}
	
	/**
	 * Holds all children that have new scope.  Check to make sure that the entered
	 * strings are literally the roles that will enter this scope.
	 */
	private Map<SyntaxRelationship, Set<String>> newScopes;
	
	public void putNewScope(SyntaxRelationship rel, String name){

		if(!newScopes.containsKey(rel)){
			newScopes.put(rel, new HashSet<String>());
		}
		newScopes.get(rel).add(name);
	}
	
	
	/*
	 * We are not going to deal with variable alteration until some time later.
	 * Need to get something out before adding every feature.
	 */

	private int vector = 1;
	
	/**
	 * Vector and dot hold whether this syntax represents a Scheme vector or can be an improper list.
	 * Some things can be both.
	 */
	public int getVector() {
		return vector;
	}

	public void setVector(int vector) {
		this.vector = vector;
	}

	public int getDot() {
		return dot;
	}

	public void setDot(int dot) {
		this.dot = dot;
	}

	private int dot = 1;
	
	public void addChild(SyntaxRelationship rel){
		children.add(rel);
	}
	
	public List<SyntaxRelationship> getChildren() {
		return children;
	}
	
	public void setChildren(List<SyntaxRelationship> children) {
		this.children = children;
	}
	
	/**
	 * This gives the new environments for children.  This should not create a separate
	 * environment instance for every potentially new child, but that is a performance
	 * issue to address later.
	 * TODO: recycle environments
	 * 
	 * Would be cool to make a dummy variable that references whatever occurred in
	 * the declaration.  So if we have a let statement without variable names,
	 * we could let the code refer to some of these unnamed variables without
	 * necessarily knowing what's going to go in there.
	 * 
	 * We need some scope cascade - a define should create a variable in the parent scope.
	 * 
	 * @return
	 */
	public SchemeEnv getEnvironmentForChild(SchemeObject owner, SchemeObject child, SchemeEnv current){
		return getEnvironmentForChild(owner, child.getRole(), current);
	}
	
	public SchemeEnv getEnvironmentForChild(SchemeObject owner, SyntaxRelationship child, SchemeEnv current){
		if(newScopes.containsKey(child)){
			SchemeEnv newEnv = owner.getFactory().subEnvironment(current);
			for(String role : newScopes.get(child)){
				newEnv.putAll(getVarDecl(owner, role));
			}
			return newEnv;
		}
		return current;
	}
	
	/**
	 * Gets the environment created in a define or similar.
	 * 
	 * This method is for INTERNAL use - for example, when a define-syntax declscopes a name,
	 * it must be able to use that name inside of subsequent children.
	 * 
	 * TODO: get working with macros that bury scope deeply.
	 * 
	 * @param owner
	 * @param rel
	 * @return
	 */
	public SchemeEnv getEnvironmentFromChild(SchemeList owner, SchemeObject child, SchemeEnv current){
		//System.out.println("should be grabbing environment from child " + child + " - " + (child.getMatch()
		//		!= null ? child.getMatch().getRoleMatches() : null));
		if(child.getSyntax() instanceof ChildedSyntax){
			return ((ChildedSyntax) child.getSyntax()).getEnvironmentSubsequentToSelf(child, current);
		}
		return current;
	}
	
	/**
	 * This should tie in with the getEnvironmentFromChild method.
	 * 
	 * @param owner
	 * @param parent
	 * @return
	 */
	public SchemeEnv getEnvironmentSubsequentToSelf(SchemeObject owner, SchemeEnv current){
		//System.out.println("Looking for env subsequent to " + this+ " " + owner + " " + outScopes);
		if(outScopes.isEmpty()){
			return current;
		}
		//System.out.println("Found env subseq to " + this + " " + owner);
		SchemeEnv newEnv = current;
		for(String out : outScopes){
			//System.out.println("child  out: " + out +" owner is " + owner.getString());
			newEnv.putAll(getVarDecl(owner, out));
		}
		//System.out.println("Rebuilt env from " + owner.getString());
		//newEnv.finishSyntax();
		return newEnv;
	}
	
	/**
	 * This method dynamically builds a list of all the variable symbols or to-be-declared
	 * variable symbols that will accrue to a role.
	 * 
	 * One bit of nastiness here is that things may try to match way too high up.  If this
	 * is the case, then we should be sure to return a fail rather than throwing an
	 * exception.
	 * 
	 * @param owner
	 * @param role
	 * @return
	 */
	public List<SchemeEnv.VariableDeclaration> getVarDecl(SchemeObject owner, String role){
		
		try{
			//System.out.println(this);
			VariableCreation vcs = varDeclarations.get(role);
			//System.out.println(vcs.declarationChain);
			/*
			 * If we don't know where this variable came from, ask the parent to find it.
			 */
			//System.out.println("checking var decs " + this + " -- " + varDeclarations.get(role) + " _ " + owner.getString() + " _ " + role);
			if((vcs == null) || (((SchemeList) owner).getByRel(vcs.declarationChain.get(0)) == null)){
				if(owner.getParent() == null){
					return Collections.emptyList();
				}
				return ((ChildedSyntax) owner.getParent().getSyntax()).getVarDecl(owner.getParent(), role);
			}
			
			List<SchemeObject> declarations = Collections.singletonList(owner);
			for(SyntaxRelationship rel : vcs.declarationChain){
				List<SchemeObject> newDecls = new ArrayList<SchemeObject>(declarations.size());
				for(SchemeObject declared : declarations){
					//System.out.println("I'm looking for " + rel);
					//System.out.println("I have " + declared.getString());
					//System.out.println("It knows about " + declared.getMatch().getRoleMatches());
					newDecls.addAll(((SchemeList) declared).getByRel(rel));
				}
				declarations = newDecls;
			}
			List<SchemeObject> definitions = Collections.singletonList(owner);
			if(vcs.definitionChain != null){
				for(SyntaxRelationship rel : vcs.definitionChain){
					List<SchemeObject> newDefs = new ArrayList<SchemeObject>(definitions.size());
					for(SchemeObject defined : definitions){
						newDefs.addAll(((SchemeList) defined).getByRel(rel));
					}
					definitions = newDefs; 
				}
				List<SchemeEnv.VariableDeclaration> vd = new ArrayList<SchemeEnv.VariableDeclaration>(declarations.size());
				for(int i = 0; i < declarations.size(); i++){
					vd.add(new SchemeEnv.VariableDeclaration((SchemeSymbol) declarations.get(i), definitions.get(i)));
				}
				return vd;
			}else{
				List<SchemeEnv.VariableDeclaration> vd = new ArrayList<SchemeEnv.VariableDeclaration>(declarations.size());
				for(int i = 0; i < declarations.size(); i++){
					vd.add(new SchemeEnv.VariableDeclaration((SchemeSymbol) declarations.get(i), null));					
				}
				return vd;
			}
		}
		catch(ClassCastException ce){
			//TODO: something?
			//usually happens when we matched something of the wrong structure to create scope based on.
			//at this point, we should not be too worried about the missing scope
		}
		catch(NullPointerException e){
			
		}
		return Collections.singletonList(new SchemeEnv.VariableDeclaration(
				new SchemeSymbol(null, owner.getFactory(), owner.getEnvironment(), "error"), null));
		
	}
	
	/**
	 * This method is giving us hell by inserting null SchemeObjects into things where there should
	 * be header symbols.  Partly this is due to templates not realizing that they have header symbols.
	 * 
	 * So templates are currently blind to whether they are symboled or not.  This need not be a killer,
	 * should there exist a way to have template literals propagate through as themselves.
	 * 
	 * The real issue is templates having multiple kinds of syntax.  The template is itself a syntax, but
	 * the generated objects are also syntaxes of a sort.  So a template should create something that
	 * represents a blank version of what it would output, or should move this functionality to a different
	 * method.
	 * 
	 * Another issue is with generation ambiguity/non-ambiguity.  We probably want the interface bridge
	 * to automatically expand syntaxes with low ambiguity, but this is a job for the tunable UI, as it
	 * may want to, for instance, expand on an ambiguity of 2 as well as of 1, if there are extra
	 * slots left in the selection interface for list structure.
	 * 
	 * So we separate out the template code and fix this method to give a placeholder object with a fully
	 * configured syntax stack.  The "owner" will either be a parent or the object we're about to replace.
	 * Probably the latter.  Either way, we should try not to worry about parents too much here, since
	 * the UI bridge will take care of them.  
	 * 
	 * We should probably propagate down as far as the syntax is definite, which includes through other
	 * child lists.
	 */
	@Override
	public Set<SchemeObject> getPossible(SchemeObject owner){
		Stack<SchemeObject> constituents = new Stack<SchemeObject>();
		SchemeList possibilityList = owner.getFactory().makeEmptyList(owner.getParent());
		for(SyntaxRelationship child : children){
			SchemeObject childObj = null;
			LinkedList<PossibleSyntax> ss = new LinkedList<PossibleSyntax>();
			ss.add(child.child);
			while(true){
				PossibleSyntax current = ss.getLast();
				if(current.getChildren(owner).isEmpty() && (current.getPossible(owner).size() == 1)){
					//if the syntax is a legitimate singleton
					SchemeSyntax synt = current.getPossible(owner).iterator().next();
					if(synt.getPossible(owner).size() == 1){
						//System.out.println("Creating possible for " + synt + " in " + this);
						childObj = synt.getPossible(owner).iterator().next();
						//System.out.println("Created a symbol? " + (childObj instanceof SchemeSymbol));
						childObj.setRole(child);
						childObj.configureSyntax(ss);
						break;
					}else{
						//this should not really happen right now - if the schemesyntax creates many possible schemeobjects
						childObj = owner.getFactory().holdSyntax(owner, ss, null,  child);
						break;
					}
				}else if((current.getChildren(owner).size() == 1) && current.getPossible(owner).isEmpty()){
					//if the syntax is a childed singleton
					current = current.getChildren(owner).iterator().next();
					ss.add(current);
				}else{
					//the syntax is not a singleton at all
					childObj = owner.getFactory().holdSyntax(owner, ss, null, child);
					break;
				}
			}
			constituents.push(childObj);
		}
		possibilityList.startMatch().matchOriginal = this;
		while(!constituents.isEmpty()){
			possibilityList.shift(constituents.pop());
		}
		if(symbol != null){
			possibilityList.shift(owner.getFactory().cloneObject(null, symbol, owner.getEnvironment()));
		}
		return Collections.singleton(((SchemeObject) possibilityList));
	}
	public MetaSyntax<?> matchSyntax(MetaSyntax<?> other){
		return this.equals(other) ? other : null;
	}
	
	public SchemeSymbol getSymbol(){
		return symbol;
	}
	
	/**
	 * 
	 * @param sl
	 * @return whether or not this childedSyntax matches the given child structure.
	 * Must match recursively by gathering child possibilities and looking for
	 * compatibility.
	 * 
	 * Would REALLY like to be able to pass methods here in order to use same logic for structural,
	 * child match.  For now, may hack w/ booleans
	 * 
	 * We need to somehow get defined syntax into the environment.  It should come out as SYNTAX and
	 * not as an IDENTIFIER the way that other things might.  The problem is that the macro parser
	 * can't parse the macro until it has matched.  Could:
	 * 1) tell the environment to accelerate the match.
	 * 2) have the childedSyntax check retro-actively
	 * 3) add a separate method that is called later to finish out syntax definitions, since we should never
	 * attempt to expand out a macro in an internally recursive way
	 * 
	 * Need to deal with syntax that may have many declarations preceding any definitions.  Maybe we just
	 * keep trying until it sticks :)
	 * 
	 * 
	 */
	protected boolean matchByChildren(SchemeList sl, boolean dynamic, int syntaxPosition, int itemPosition){

		/*
		 * This way, having a child fail doesn't block the match.
		 * It's crazy useful for error checking.
		 * 
		 */
		boolean childrenMatch = true;
		
		int totalItems = sl.size();
		int totalSyntax = children.size();
		//TODO: actually implement rolling scope, such as in let*
		SyntaxMatched matched = null;
		if(dynamic){
			if(!matchByChildren(sl, false, syntaxPosition, itemPosition)){
				//this should auto-reset sl's matchOriginal to null
				//note that this method may itself fail and yet leave structural matches in children
				//it's not subsequent children but subsequent match-failing children
				//this is because the true match gives up and does not continue to reset matches all the way down
				//can we safely call a recursive clearchildren method?  since we're not caching matches, probably
				//TODO: clear matches going down recursively
				return false;
			}
			matched = sl.getMatch();
			sl.createLastEnv();
			for(SchemeObject child : sl){
				getEnvironmentFromChild(sl, child, sl.getEndEnvironment());
			}
			//System.out.println("trying to match " + dynamic + " " + sl.getString() + " with syntax " +getPriority() + " " + this  + this.getChildren());
		}else{
			matched = sl.startMatch();
			matched.matchOriginal = this;
		}
				
		if((symbol != null) && ((totalItems == 0) || !(symbol.getString().equals(sl.get(0).getString())))){
			//System.out.println("Symbols didn't match.  " + this);
			matched.matchOriginal = null;
			return false;
		}
		//System.out.println("trying to match " + dynamic + " " + sl.getString() + " with syntax " +getPriority() + " " + this  + this.getChildren());
		//if(dynamic  && (symbol != null) && symbol.getString().equals("apply")){
		//	System.out.println(sl.getDescriptionRecursive());
		//}

		/* This is going to get really nasty, but essentially, we have to deal with environments in steps.
		 *  The way forward:
		 *  1.3) We must tell the structural match to still put declarations in scope (just not necessarily definitions).
		 *  In fact, it probably could put definitions in scope and get away with doing so.  The big difference is that
		 *  it's not allowed to use any scope in its matching.  Note that because we are doing scope by parent, we
		 *  need only modify this class.  Way easier than redoing whole syntax system.
		 *  1.4) We only need match correctness to go deep enough to uniquify and scopify this.  We may either cut
		 *  off at that point (iterative deepening) or allow everything below to go ahead and try stuff (depth-first).
		 *  Depth-first is an aggressor search that will break occasionally (but not often).  Iterative deepening is
		 *  optimal but more difficult to implement.  Let's use DFS until we can't.
		 *  1.5) It's actually the responsibility of the PossibilitySyntax to handle the fact that we may have multiple
		 *  different ChildedSyntax objects matching.  Since matches get replace on matching, however, we should
		 *  do both matches in this class.
		 *  1.55) DynamicSyntax will match as procedures if they get too far down.  That's perfectly fine.
		 *  1.6) This class deals with anything that must go down the tree.  During a structural match, it should
		 *  ignore scope (by calling structuralMatch on children) but still fill in its own matcher object.  This allows
		 *  the true match to make use of the already setup scopes.  We don't particularly care that children might not
		 *  be filling in their own match objects, since they don't contribute to scoping.
		 *  1.65) We must tell the children to fill in their own matcher objects, but there might be some abiguity.
		 *  We don't care.  Why not?  Because we require that all of the scopes going into this object are in
		 *  fact totally unambiguous, so we can just throw away everything else.
		 *  1.7) During the final match, we use the environment already configured in the match object.  The
		 *  getEnvironmentForChild method should handle this, and we can modify getEnvironmentFromChild to just pour
		 *  scopes into the current environment, then call it on all children.
		 *  1.N) That might be game. 
		 *  2) Make sure that correct lists generate from placeholders.
		 *  3) Check behavior upon object replace.  For now, we might forbid rematch - syntax has to change at the parent.
		 *  If the user wants to undo a syntactic decision, she must delete the parent node.
		 *  This is a minimum functionality.
		 */	
		
		while((syntaxPosition < totalSyntax) && (itemPosition <= totalItems)){
			if(itemPosition == totalItems){
				//then we need to see if stuff can match nothing
				for(SyntaxRelationship rel : children.subList(syntaxPosition, children.size())){
					if((rel.num == SyntaxRelationship.INF)){
						matched.put(rel, new ArrayList<SchemeObject>(0));
					}else if(rel.num == SyntaxRelationship.DOT_TAIL){
						if(rel.equals(children.get(totalSyntax - 1))){
							matched.put(rel, Collections.singletonList((SchemeObject) sl));
						}else{
							//System.out.println("dot tail fail");
							matched.matchOriginal = null;
							return false;
						}
					}else{
						matched.matchOriginal = null;
						//System.out.println("offending link " + rel.role + " - " + rel.num);
						return false;
					}
				}
				//System.out.println("matched nothing!");
				return true;
			}
			SyntaxRelationship rel = children.get(syntaxPosition);
			SchemeObject childObj = sl.get(itemPosition);
			
			matched.put(rel, Collections.singletonList(childObj));
			childObj.setRole(rel);
			childObj.setEnvironment(getEnvironmentForChild(sl, rel, sl.getEndEnvironment()));
			matched.put(rel, Collections.singletonList(childObj));
			
			if(rel.num == SyntaxRelationship.INF){
				/*
				 * In order for letrec to work, this may need to add itself to scope before it matches.
				 * 
				 * So the containing letrec deals with scopes for the letscope and letdecls.  This does
				 * not quite work out, because the scopes do not come into play until after the letscope
				 * has fully finished parsing, when we NEED them to come into play inside of the letdecl itself.
				 * 
				 * Fixes:
				 * 1) Possibly use a cleverly applied _scope keyword over the letscope in letrec, plus a _declscope
				 * on each instance of letdecl.  In theory, this means that the letscope is itself in a new scope
				 * and that all of the letdecls dump into that scope.
				 * 2) Cause the _scope keyword to precache before the match is complete.  This seems like it would be
				 * much harder, as we might have to alter the structure of parsing to pass scope awareness between
				 * child levels.
				 * 3) Put a _declare and _scope into the letscope on the letdecl.  Check to make sure that inf matches
				 * account for this possibility.
				 * 
				 */
				int remainingInfs = 0;
				if(syntaxPosition < totalSyntax){
					for(SyntaxRelationship later : children.subList(syntaxPosition+1, children.size())){
						remainingInfs += later.num == SyntaxRelationship.INF ? 1 : 0;
					}
				}
				List<SchemeObject> matchedInf = new LinkedList<SchemeObject>();
				//System.out.println(childObj.getRole());
				//System.out.println(childObj.getSyntaxStack());
				while(((totalItems - itemPosition) >= (totalSyntax - syntaxPosition - remainingInfs))
						&& 
						//boolean hack location #1
						((!dynamic) ? !rel.child.matchStructure(childObj).isEmpty() : rel.child.match(childObj))
						){
					//System.out.println("inf-matched " + childObj.getString() + " -- " + rel.child);
					matchedInf.add(childObj);
					//if(rolling){
					//	current = getEnvironmentFromChild(sl, childObj, current);
					//}
					itemPosition++;
					if(itemPosition >= totalItems){
						break;
					}
					childObj = sl.get(itemPosition);
					matched.put(rel, matchedInf);
					childObj.setRole(rel);
					childObj.setEnvironment(getEnvironmentForChild(sl, rel, sl.getEndEnvironment()));
				}
				syntaxPosition ++;
				matched.put(rel, matchedInf);
				if(itemPosition >= totalItems){
					//then we've matched everything
					continue;
				}
				//otherwise we failed to match something
			}else if (rel.num == SyntaxRelationship.DOT_TAIL){
				matched.put(rel, Collections.singletonList((SchemeObject) sl.rest(itemPosition)));
				childObj.setEnvironment(getEnvironmentForChild(sl, rel, sl.getEndEnvironment()));
				itemPosition = totalItems;
				syntaxPosition++;
			}else{
				//if(dynamic)
				//System.out.println("Matching " + rel.child + " - " + childObj);
				if((!dynamic) ? rel.child.matchStructure(childObj).isEmpty() : !rel.child.match(childObj)){
					//boolean hack location #2
					//System.out.println("Child failed to match " + childObj.getString() + " : " + rel.child + " " + rel.child.getPossible(childObj));
					matched.matchOriginal = null;
					//if(rolling){
					//	current = getEnvironmentFromChild(sl, childObj, current);
					//}
					return false;
					//childrenMatch = false;
					//we want things to continue matching, but they may have serious scope errors because of this non-match
					//we could try to kill matching on scope implosion, but that's a little too complicated
					//we could dump a dummy into scope
					//other problem is with structural match-clearing
				}
				itemPosition++;
				syntaxPosition++;
			}
		}
		if(!((itemPosition == totalItems) && (syntaxPosition == totalSyntax))){
			//if(dynamic)
			//ystem.out.println("total match failed items: " + itemPosition +
			//	"/" + totalItems + " syntaxes " + syntaxPosition + "/" + totalSyntax + " -- " + this + " -- " + sl.getString());
			matched.matchOriginal = null;
			return false;
		}
		//System.out.println("match successful! " + this + " -- " + sl.getString() + "items: " + itemPosition +
		//			"/" + totalItems + " syntaxes " + syntaxPosition + "/" + totalSyntax + " -> " + dynamic);
		if(dynamic){
			sl.getEndEnvironment().finishSyntax();
		}
		return childrenMatch;
	}
	
	/**
	 * Does this need to recur down?
	 * 
	 * In the case of macros, it really should, as 
	 * (var1 var2) != (var1 (var2 var3))
	 * 
	 * Other syntax should also recur down, matching structurally
	 * where possible.  This suggests some kind of memoization,
	 * which would need to be stored in the objects (as syntax is
	 * intentionally re-usable across parts of the tree).
	 * 
	 * This may have to use very similar logic to the full match method,
	 * even going so far as to directly recur down.
	 * 
	 * Trying to deal with tendency for subsequent matches to overwrite stuff.
	 * Problem is structural matches in the PossibleSyntax fucking up child matches.
	 * Since the PossibleSyntax will run everything on this.
	 * We might need to implement the cache or the iterative deepening.
	 * Or maybe it's enough to tell procedures to stop matching syntaxes.
	 * 
	 * TODO: check this aggressively to figure out WTF happens when too many things match.
	 * Right now, we might get away with it, since only "define" can declscope.
	 * We have restricted procedures from matching syntax defined in the grammar,
	 * which works as long as we don't have dynamically defined syntax.
	 * 
	 * This is something hellish to figure out: what to do when a match fails?
	 * It isn't significant unless we have declscopes involves (at least we believe).
	 * 
	 * The only other case in which this will probably arise is when dealing with
	 * multiple possibilities for things that have scope.  An example might be
	 * the multiply defined lambda function.  This may, however, not create a problem,
	 * because at least in theory, only one option should match structurally
	 * 
	 * @return
	 */
	public boolean matchStructure(SchemeObject o){
		if(o instanceof SchemeList){
			SyntaxMatched pastMatch = o.getMatch();
			SchemeList l = (SchemeList) o;
			boolean result = this.matchByChildren(l, false, 0, (symbol == null) ? 0 : 1);
			if(!result){
				o.setMatch(pastMatch);
			}
			return result;
		}
		return false;
	}

	@Override
	public boolean match(SchemeObject toTry) {
		boolean result = matchUnscoped(toTry);
		if(!result){
			toTry.clearSyntax();
		}
		if(toTry.getMatch() != null){
			toTry.getMatch().matchOriginal = this;
		}else{
			toTry.startMatch().matchOriginal = this;
		}
		return result;
	}
	
	//TODO: make this method fit its purpose here.
	@Override
	public boolean matchUnscoped(SchemeObject o){
		if(!(o instanceof SchemeList)){
			return false;
		}
		
		if(symbol != null){
			return matchByChildren((SchemeList) o, true, 0, 1);
		}
		return matchByChildren((SchemeList) o, true, 0, 0);
	}
	
	public String toString(){
		return name + "{childed}";
	}

	public boolean hasOutscopes() {
		return !outScopes.isEmpty();
	}
}





