package machination.webjava.trees.scheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.generated.GeneratedCase;
import machination.webjava.trees.schemegrammar.generated.SyntaxRulesHandler;


public class DefaultSchemeEnv implements SchemeEnv{

	private SchemeEnv parent;
	
	/**
	 * Used when all we have is a symbol name and are looking for the
	 * symbol.  We might upgrade to a sortedMap to allow alphabetical search.
	 * 
	 * Right now we are not using this.
	 * 
	 * Later, we could make the other maps sorted and use this as a way to obtain
	 * a proper key for searching those.
	 */
	private Map<String, SchemeSymbol> symbolsByName;
	
	/**
	 * Declaration-definition pairs by name
	 * 
	 * this refers to both syntax and symbol
	 */
	private Map<SchemeObject, SchemeEnv.VariableDeclaration> declarations;
		
	private Map<SchemeObject, SchemeEnv.VariableDeclaration> defMap = new HashMap<SchemeObject, SchemeEnv.VariableDeclaration>();
	
	/**
	 * Returns all the uses of a given symbol.  This feature is not
	 * fully supported for 0.1 release.  Later, use the same
	 * style of logic to track alterations.
	 */
	private Map<String, SortedSet<SchemeObject>> uses;
	
	/**
	 * Holds named syntactic constructs defined in this environment.
	 * 
	 * It's up to let-syntax and define-syntax to put the syntax
	 * into the correct environment.
	 * 
	 */
	public Map<String, PossibleSyntax> syntax;
	
	private Set<VariableDeclaration> pendingSyntax;
	
	private Map<String, PossibleSyntax> procedures;
	
	
	public DefaultSchemeEnv(SchemeEnv parent){
		declarations = new HashMap<SchemeObject, SchemeEnv.VariableDeclaration>();
		uses = new HashMap<String, SortedSet<SchemeObject>>();
		syntax = new TreeMap<String, PossibleSyntax>();
		symbolsByName = new HashMap<String, SchemeSymbol>();
		this.parent = parent;
		pendingSyntax = new HashSet<VariableDeclaration>();
		procedures = new TreeMap<String, PossibleSyntax>();
	}
	
	/**
	 * Be careful about using this, because it might get called
	 * when the object in question is not bound at all - for instance,
	 * in the case of a template variable.
	 */
	@Override
	public SchemeEnv bindingEnvironment(SchemeSymbol sym) {
		if(symbolsByName.containsKey(sym.getString())){
			return this;
		}
		return getParent() == null ? null : getParent().bindingEnvironment(sym);
	}

	@Override
	public void define(SchemeSymbol sym, SchemeObject definition) {
		if(sym == null){
			throw new UnsupportedOperationException("Must have a symbol to add.");
		}
		VariableDeclaration v = new VariableDeclaration(sym, definition);
		put(v);
	}

	@Override
	public Set<SchemeSymbol> enumerateSymbols() {
		return new LinkedHashSet<SchemeSymbol>(symbolsByName.values());
	}

	@Override
	public PossibleSyntax findSyntax(SchemeSymbol o) {
		return findSyntax(o.getString());
	}

	@Override
	public VariableDeclaration get(SchemeSymbol sym) {
		if(declarations.containsKey(sym.getString())){
			return declarations.get(sym.getString());
		}
		return null;
	}

	@Override
	public SortedSet<SchemeObject> getAllUses(SchemeSymbol name) {
		SortedSet<SchemeObject> allUses = uses.get(name.getString());
		if(uses == null){
			return new TreeSet<SchemeObject>();
		}
		return allUses;
	}

	@Override
	public Set<SchemeSymbol> getDifference(SchemeEnv child) {
		Set<SchemeSymbol> difference = new HashSet<SchemeSymbol>();
		while((child != null) && (child != this)){
			difference.addAll(child.enumerateSymbols());
			child = child.getParent();
		}
		if(child == null){
			throw new UnsupportedOperationException("Differencing with non-child environment.");
		}
		return difference;
	}

	@Override
	public boolean includes(SchemeEnv other) {
		while((other != null) && (other != this)){
			other = other.getParent();
		}
		return other == this;
	}

	@Override
	public boolean isDefined(SchemeSymbol sym) {
		//System.out.println("Looking for " + sym.getString() + " in " + symbolsByName);
		return symbolsByName.containsKey(sym.getString()) ||
			((getParent() != null) && (getParent().isDefined(sym)));
	}

	@Override
	public void put(VariableDeclaration vd) {
		//TODO: make dead environments deregister themselves
		//System.out.println(this + " putting " + vd);
		if(symbolsByName.containsKey(vd.decl.getString())){
			//this is probably already fully handled by registered listeners
			//TODO: check if we need to explicitly replace at this point in code
		}
		declarations.put(vd.decl, vd);
		defMap.put(vd.def, vd);
		symbolsByName.put(vd.decl.getString(), vd.decl);
		uses.put(vd.decl.getString(), new TreeSet<SchemeObject>());
		vd.decl.registerReplaceListener(this);
		
		if(vd.def != null){
			vd.def.registerReplaceListener(this);
		}
		//System.out.println(vd.def.getSyntaxStack());
		if((vd.def != null) && (vd.def.getSyntaxStack().peek() instanceof SyntaxRulesHandler)){
			pendingSyntax.add(vd);
		}
	}

	@Override
	public void putAll(Collection<VariableDeclaration> vds) {
		//System.out.println("putting all " + vds);
		for(VariableDeclaration vd : vds){
			put(vd);
		}
	}

	@Override
	public SchemeEnv getParent() {
		return parent;
	}

	/**
	 * We rely on replacement code to call this method.  Must take care that all additions should also
	 * register themselves as uses, and all deletions de-register.
	 */
	@Override
	public void registerUse(SchemeSymbol use) {
		if(use == null){
			return;
		}

		if(!uses.containsKey(use.getString())){
			if(getParent() == null){
				throw new IllegalStateException("Registering use for object not in environment " + use);
			}
			getParent().registerUse(use);
			return;
		}
		uses.get(use.getString()).add(use);
	}
	
	public void deleteUse(SchemeSymbol use) {
		if(use == null){
			return;
		}
		
		if(!uses.containsKey(use.getString())){
			if(getParent() == null){
				throw new IllegalStateException("Deleting use for object not in environment " + use);
			}
			getParent().deleteUse(use);
			return;
		}
		
		uses.get(use.getString()).remove(use);
	}
	

	/**
	 * Note that an object used in this environment might have been
	 * defined in a previous environment.  For now, we will assume
	 * that this only matters when a declaration or definition gets
	 * replaced.
	 * 
	 * This should also by its very nature take care of removing and/or
	 * adding things that 
	 */
	@Override
	public void gotReplace(SchemeObject old, SchemeObject n) {
		if(old == null){
			return;
		}
		//System.out.println(this + " replacing " + old + " -> " + n);
		if(n == null){
			//System.out.println("removing");
			//then we lose the object
			declarations.remove(old);
			defMap.remove(old);
			symbolsByName.remove(old.getString());
			old.removeReplaceListener(this);
			if(uses.containsKey(old.getString())){
				Set<SchemeObject> oldUses = new HashSet<SchemeObject>(uses.get(old.getString()));
				for(SchemeObject used : oldUses){
					used.unSpecify();
				}
				uses.remove(old.getString());
			}
			return;
		}
		//System.out.println("actually replacing");
		if(declarations.containsKey(old)){
			//System.out.println("replacing declared");
			if(symbolsByName.containsKey(old.getString())){
				symbolsByName.remove(old.getString());
				symbolsByName.put(n.getString(), (SchemeSymbol) n);
				//System.out.println("changed symbolByName " + old.getString() + " -> " + n.getString());
			}
			if(declarations.containsKey(old)){
				declarations.put(n, declarations.remove(old));
				//System.out.println(n);
				declarations.get(n).decl = ((SchemeSymbol) n);
				//System.out.println("changed declaration " + old + " -> " + n + " :: " + uses.get(old.getString()));
				/*
				 * This is when we have to go through and replace the damn thing.
				 * How?
				 * -could register all uses of a symbol at identifier creation time
				 * --assuming that we are matching identifiers, this could actually work
				 * as part of the match function
				 */
				//System.out.println("uses " +  uses.get(old.getString()));
				uses.put(n.getString(), new TreeSet<SchemeObject>());
				Set<SchemeObject> usesCopy = new HashSet<SchemeObject>(uses.get(old.getString()));
				for(SchemeObject o : usesCopy){
					//replace every use!
					//System.out.println("replacing use " + o + " -> " + n);
					o.removeReplaceListener(this);
					o.replace(o.getFactory().cloneObject(o.getParent(), n, o.getEnvironment()));
				}
				uses.remove(old.getString());
			}
			if(defMap.containsKey(old)){
				defMap.put(n, defMap.get(old));
				defMap.get(n).def = n;
				//System.out.println("changed definition " + old + " -> " + n);
			}
		}
	}

	@Override
	public Set<SchemeSymbol> enumerateIdentifiers() {
		Set<SchemeSymbol> identifiers = new LinkedHashSet<SchemeSymbol>();
		identifiers.addAll(enumerateSymbols());
		identifiers.removeAll(enumerateSyntax());
		return identifiers;
	}

	@Override
	public Set<SchemeSymbol> enumerateSyntax() {
		HashSet<SchemeSymbol> syntaxes = new LinkedHashSet<SchemeSymbol>(syntax.size());
		for(String syntaxName : syntax.keySet()){
			if(!symbolsByName.containsKey(syntaxName) || (null == symbolsByName.get(syntaxName))){
				throw new IllegalStateException("Lost syntax name: " + syntaxName + " in " + symbolsByName);
			}
			syntaxes.add(symbolsByName.get(syntaxName));
		}
		for(VariableDeclaration pending : pendingSyntax){
			syntaxes.add(pending.decl);
		}
		return syntaxes;
	}
	
	@Override
	public Set<SchemeSymbol> enumerateAllSyntax(){
		Set<SchemeSymbol> total = enumerateSyntax();
		SchemeEnv parent = this.getParent();
		while(parent != null){
			total.addAll(parent.enumerateSyntax());
			parent = parent.getParent();
		}
		return total;
	}

	@Override
	public void putSyntax(SchemeSymbol sym, PossibleSyntax p) {
		syntax.put(sym.getString(), p);
		symbolsByName.put(sym.getString(), sym);
	}

	/**
	 * Call this method AFTER having loaded in all of the syntax that will be defined in this environment.
	 */
	@Override
	public void finishSyntax() {		
		Iterator<VariableDeclaration> pi = pendingSyntax.iterator();
		List<PossibleSyntax> forPropagation = new ArrayList<PossibleSyntax>(pendingSyntax.size());
		while(pi.hasNext()){
			VariableDeclaration pending = pi.next();
			if(SyntaxRulesHandler.syntaxReady(pending.def)){
				PossibleSyntax p = SyntaxRulesHandler.getSpecified(pending.decl, pending.def);
				forPropagation.add(p);
				putSyntax(pending.decl, p);
				//could be screwing up environments again, or something similar
				//must also ensure that the environment that owns this syntax is the one that does this first
				//System.out.println("Should have just put syntax " + syntax.get(pending.decl.getString()) + " <- " + this);
				pi.remove();
			}
		}
		GeneratedCase.propagateSyntax(forPropagation);
	}

	@Override
	public Map<String, PossibleSyntax> enumerateProcedures() {
		return procedures;
	}

	@Override
	public PossibleSyntax findSyntax(String string) {
		if(syntax.containsKey(string)){
			return syntax.get(string);
		}
		
		if(getParent() != null){
			return getParent().findSyntax(string);
		}
		return null;
	}

	@Override
	public PossibleSyntax findProcedure(String s) {
		if(procedures.containsKey(s)){
			return procedures.get(s);
		}
		
		if(getParent() != null){
			return getParent().findProcedure(s);
		}
		return null;
	}

	@Override
	public PossibleSyntax namedSyntaxLookup(String name) {
		if(getParent() != null){
			return getParent().namedSyntaxLookup(name);
		}
		return null;
	}

}
