package machination.webjava.client.scheme.biwa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.PossibilitySyntax;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.ProcedureSyntax;

/**
 * Base environment dervied from Biwa's environment.  Should be able to load in everything from the javascript.
 * 
 * Wraps BiwaScheme.TopEnv
 * 
 * @author nick
 *
 */
public class BiwaBaseEnv implements SchemeEnv{
	
	private Set<SchemeSymbol> symbols = new LinkedHashSet<SchemeSymbol>();
	private Set<SchemeSymbol> syntax = new LinkedHashSet<SchemeSymbol>();
	private Set<SchemeSymbol> identifiers = new LinkedHashSet<SchemeSymbol>();
	
	private Map<String, SchemeSymbol> identNames = new TreeMap<String, SchemeSymbol>();
	
	
	private Map<String, PossibleSyntax> procedures = new TreeMap<String, PossibleSyntax>();
	
	private Map<String, PossibleSyntax> procNames = new TreeMap<String, PossibleSyntax>();
	
	public BiwaBaseEnv(BiwaSchemeFactory f, List<String> available){
		Stack<PossibilitySyntax> cps = new Stack<PossibilitySyntax>();
		Map<String, PossibilitySyntax> cats = new HashMap<String, PossibilitySyntax>();
		for(int i = 0; i < available.size(); i++){
			String curr = available.get(i);
			if(curr.trim().length() == 0){
				continue;
			}
			//System.out.println("Curr " + curr);
			if(curr.startsWith("__")){
				String name = curr.replace("__", "");
				if(!cats.containsKey(name)){
					cats.put(name, new PossibilitySyntax(name));
				}
				if(curr.startsWith("____")){
					while(cps.size() > 1){
						cps.pop();
					}
					cps.peek().addPossibilities(cats.get(name));
					cps.push(cats.get(name));
				}else{
					cps.clear();
					cps.push(cats.get(name));
					procedures.put(name, cats.get(name));
				}
			}else{
				String name = curr;
				BiwaSchemeSymbol sym = new BiwaSchemeSymbol(null, f, f.getEnv(), name);
				//System.out.print(" " + name);
				PossibleSyntax ts = ProcedureSyntax.getCallByArgCounts(
						sym, f.getScheme().getFuncs().getMinArgs(name),
						f.getScheme().getFuncs().getMaxArgs(name));
				cps.peek().addPossibilities(ts);
				//symbols.add(sym);
				//identifiers.add(sym);
				identNames.put(name, sym);
				procNames.put(name, ts);
			}
		}
		//System.out.println(name + ": " + f.getScheme().getFuncs().getMinArgs(name) + " - " + f.getScheme().getFuncs().getMaxArgs(name));
		//TODO: add other stuff, like syntax
		identifiers.addAll(identNames.values());
		symbols.addAll(identNames.values());
	}
	
	@Override
	public void gotReplace(SchemeObject old, SchemeObject n) {
		throw new UnsupportedOperationException("Can't replace in the base");
	}

	/**
	 * We are using this method to mask the existence of symbols we don't want to deal with,
	 * and to make the program refresh with each pass.  This MIGHT cause some problems, if there
	 * is any way for things to wind up in Biwa's environment.  Really, we'd be much better off
	 * with some way to reload Biwa on each pass in addition to this.  But it will do for a demo.
	 */
	@Override
	public boolean isDefined(SchemeSymbol sym) {
		//return f.getScheme().isDefined(sym.getString());
		return identNames.containsKey(sym.getString());
	}

	@Override
	public boolean includes(SchemeEnv other) {
		return false;
	}

	@Override
	public SchemeEnv bindingEnvironment(SchemeSymbol o) {
		//not used in this release
		return null;
	}

	@Override
	public VariableDeclaration get(SchemeSymbol sym) {
		if(isDefined(sym)){
			return new VariableDeclaration(null, null);
		}
		return null;
	}

	@Override
	public PossibleSyntax findSyntax(SchemeSymbol sym) {
		return null;
	}

	@Override
	public SortedSet<SchemeObject> getAllUses(SchemeSymbol name) {
		return null;
	}

	@Override
	public void define(SchemeSymbol sym, SchemeObject definition) {
		throw new UnsupportedOperationException("Cannot add new symbols to base.");		

	}

	@Override
	public void put(VariableDeclaration vd) {
		throw new UnsupportedOperationException("Cannot add new symbols to base.");		

	}

	@Override
	public void putAll(Collection<VariableDeclaration> vds) {
		throw new UnsupportedOperationException("Cannot add new symbols to base.");		

	}

	@Override
	public Set<SchemeSymbol> enumerateSymbols() {
		return symbols;
	}

	@Override
	public Set<SchemeSymbol> enumerateIdentifiers() {
		return identifiers;
	}

	@Override
	public Set<SchemeSymbol> enumerateSyntax() {
		return syntax;
	}

	@Override
	public Map<String, PossibleSyntax> enumerateProcedures() {
		return procedures;
	}
	

	@Override
	public Set<SchemeSymbol> enumerateAllSyntax() {
		return enumerateSyntax();
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
	public SchemeEnv getParent() {
		return null;
	}

	@Override
	public void registerUse(SchemeSymbol use) {
		
	}

	@Override
	public void deleteUse(SchemeSymbol use) {
		
	}

	@Override
	public void putSyntax(SchemeSymbol sym, PossibleSyntax p) {
		throw new UnsupportedOperationException("Cannot add new symbols to base.");

	}

	@Override
	public void finishSyntax() {
		throw new UnsupportedOperationException("Cannot add new syntax to base.");
	}

	@Override
	public PossibleSyntax findSyntax(String string) {
		return null;
	}

	@Override
	public PossibleSyntax findProcedure(String s) {
		return procedures.get(s);
	}

	@Override
	public PossibleSyntax namedSyntaxLookup(String name) {
		return procNames.get(name);
	}

}
