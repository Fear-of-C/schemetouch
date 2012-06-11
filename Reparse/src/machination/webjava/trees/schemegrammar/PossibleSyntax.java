package machination.webjava.trees.schemegrammar;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

public abstract class PossibleSyntax  implements MetaSyntax<SchemeSyntax>, Comparable<PossibleSyntax>{
	
	@Override
	public int compareTo(PossibleSyntax arg0) {
		int nameOrder = getName().compareTo(arg0.getName());
		if(nameOrder != 0){
			return nameOrder;
		}
		
		return (new Integer(hashCode())).compareTo(new Integer(arg0.hashCode()));
	}

	public abstract String getName();

	public abstract Set<SchemeSyntax> getPossible(SchemeObject position);
	
	private SortedMap<SchemeSyntax, List<PossibleSyntax>> flatCache = null;
	
	private Map<String, SortedMap<SchemeSyntax, List<PossibleSyntax>>> namedCache = null;
	
	private SortedMap<SchemeSyntax, List<PossibleSyntax>> fullCache = null;
	
	public boolean match(SchemeSyntax toTry) {
		return getPossible(null).contains(toTry);
	}

	/**
	 * The hardest thing here is trying to re-use structured matching
	 * but still search in correct order.  At this point, we aren't
	 * re-using the structural match.
	 * 
	 * TODO: actually check priorities between named and unnamed caches.
	 * 
	 * @param toTry
	 * @param m
	 * @return
	 */
	public boolean match(SchemeObject toTry){
		//System.out.println("caches are " + flatCache + " _ " + namedCache + " _ " + fullCache);
		if((toTry.getDatum() == null) && !(toTry instanceof SchemeSymbol)){
			//this is an unknown/placeholder object
			//we give it this syntax, because we want it to have a starting place
			//we can still match it, however, if it's a placeholder symbol
			toTry.configureSyntax(Collections.singletonList(this));
			return true;
		}
		//System.out.println("Matching named " + toTry + " with " + this);
		if((toTry instanceof SchemeList) && (((SchemeList) toTry).size() >= 1)){
			SortedMap<SchemeSyntax, List<PossibleSyntax>> named = flattenNamed(toTry).get(((SchemeList) toTry).get(0).getString());
			if(named != null){
				for(Map.Entry<SchemeSyntax, List<PossibleSyntax>> e : named.entrySet()){
					if(e.getKey().match(toTry)){
						toTry.configureSyntax(e.getValue());
						return true;
					}
				}
			}
		}
		//System.out.println("Matching unnamed " + toTry + " with " + this);
		for(Map.Entry<SchemeSyntax, List<PossibleSyntax>> entry : flatten(toTry).entrySet()){
			SchemeSyntax s = entry.getKey();
			if(s.match(toTry)){
				toTry.configureSyntax(entry.getValue());
				return true;
			}
		}
		toTry.clearSyntax();
		//toTry.getMatch().matchOriginal = InvalidSyntax.singleton;
		//System.out.println("Match failed " + toTry + " with " + this);
		return false;
	}
	
	
	public SchemeSyntax getSingleton(){
		if(getPossible(null).size() != 1){
			return null;
		}
		return getPossible(null).iterator().next();
	}
	
	public abstract Set<PossibleSyntax> getChildren(SchemeObject context);

	/**
	 * TODO: check caching with dynamic syntax
	 * should be safe for now, as identifiers are below the level of possible syntax and are the only dynamic thing
	 * 
	 * @param context
	 * @return all leaves on the possibility tree, mapped to their
	 * parent lists
	 */
	public SortedMap<SchemeSyntax, List<PossibleSyntax>> flatten(SchemeObject context){
		if(flatCache != null){
			//since syntax is only slightly dynamic, we shouldn't need to do this more than once
			return flatCache;
		}
		Set<SchemeSyntax> possibilities = getPossible(context);
		Set<PossibleSyntax> children = getChildren(context);
		SortedMap<SchemeSyntax, List<PossibleSyntax>> r = new TreeMap<SchemeSyntax, List<PossibleSyntax>>();
		for(PossibleSyntax child : children){
			Map<SchemeSyntax, List<PossibleSyntax>> cm = child.flatten(context);
			for(List<PossibleSyntax> cl : cm.values()){
				cl.add(0, this);
			}
			r.putAll(cm);
		}
		for(SchemeSyntax synt : possibilities){
			if((synt instanceof ChildedSyntax) && (synt.getSymbol() != null)){
				continue;
			}
			List<PossibleSyntax> list = new LinkedList<PossibleSyntax>();
			list.add(this);
			r.put(synt, list);
		}
		flatCache = r;
		return r;
	}
	
	/**
	 * TODO: make this work with scope, which could easily get highly impractical
	 * 
	 * @param context
	 * @return a lookup table on schemeobjects that gives the chains of syntax for syntax with a naming symbol.
	 */
	public Map<String, SortedMap<SchemeSyntax, List<PossibleSyntax>>> flattenNamed(SchemeObject context){
		if(namedCache != null){
			return namedCache;
		}
		namedCache =
			new HashMap<String, SortedMap<SchemeSyntax, List<PossibleSyntax>>>();
		for(PossibleSyntax child : getChildren(context)){
			Map<String, SortedMap<SchemeSyntax, List<PossibleSyntax>>> c = child.flattenNamed(context);
			for(SortedMap<SchemeSyntax, List<PossibleSyntax>> cm : c.values()){
				for(List<PossibleSyntax> cl : cm.values()){
					cl.add(0, this);
				}
			}
			namedCache.putAll(c);
		}
		for(SchemeSyntax synt : getPossible(context)){
			if((synt instanceof ChildedSyntax) && (synt.getSymbol() != null)){
				List<PossibleSyntax> list = new LinkedList<PossibleSyntax>();
				list.add(this);
				if(!namedCache.containsKey(synt.getSymbol().getString())){
					namedCache.put(synt.getSymbol().getString(), new TreeMap<SchemeSyntax, List<PossibleSyntax>>());
				}
				namedCache.get(synt.getSymbol().getString()).put(synt, list);
			}
		}
		return namedCache;
	}
	
	public SortedMap<SchemeSyntax, List<PossibleSyntax>> flattenFull(SchemeObject context){
		if(fullCache != null){
			return fullCache;
		}
		SortedMap<SchemeSyntax, List<PossibleSyntax>> r = new TreeMap<SchemeSyntax, List<PossibleSyntax>>();
		r.putAll(flatten(context));
		for(SortedMap<SchemeSyntax, List<PossibleSyntax>> m : flattenNamed(context).values()){
			r.putAll(m);
		}
		return (fullCache = r);
	}
	
	/**
	 * for validation
	 * @param p
	 * @return
	 */
	public boolean match(PossibleSyntax p, SchemeObject context){
		return getChildren(context).contains(p);
	}

	/**
	 * This does not have priority problems, because it matches everything.  So the match list comes out
	 * flattened.  The only problem is that it loses the syntaxMatched objects.
	 * 
	 * TODO: clear the cache when anything relating to syntax must change.
	 * 
	 * This method is giving us trouble, because it's wiping the matches from children.  Would work better
	 * if we preserved matches in childed syntax.
	 * 
	 * We should probably do this by filtering the flat cache and symbol cache.
	 * 
	 * @return a SchemeSyntax list that matches the object given
	 */
	public SortedMap<SchemeSyntax, List<PossibleSyntax>> matchStructure(SchemeObject o){
		if((o.getMatch() != null) && (o.getMatch().cache != null)){
			if(o.getMatch().cache.containsKey(this)){
				return o.getMatch().cache.get(this);
			}
		}
		if(o.getDatum() == null){
			//if o is a placeholder, it should match any structure whatsoever, unless it's a symbol
			//in which case it will match identifiers and... nothing else
			//however, we don't necessarily want it to continue the match
			//we must either require uniqueness or specify deferred matching
			return this.flattenFull(o);
		}
		SortedMap<SchemeSyntax, List<PossibleSyntax>> r = new TreeMap<SchemeSyntax, List<PossibleSyntax>>();
		if((o instanceof SchemeList) && (((SchemeList) o).size() >= 1)){
			SortedMap<SchemeSyntax, List<PossibleSyntax>> named = flattenNamed(o).get(((SchemeList) o).get(0).getString());
			if(named != null){
				for(Map.Entry<SchemeSyntax, List<PossibleSyntax>> e : named.entrySet()){
					if(e.getKey().matchStructure(o)){
						r.put(e.getKey(), e.getValue());
					}
				}
			}
		}
		for(Map.Entry<SchemeSyntax, List<PossibleSyntax>> e : flatten(o).entrySet()){
			if(e.getKey().matchStructure(o)){
				r.put(e.getKey(), e.getValue());
				
			}
		}
		
		if(o.getMatch() == null){
			o.startMatch();
		}
		
		o.getMatch().cache.put(this, r);
		
		return r;
	}
	
	
	/**
	 * This method goes through children and possibilities and tells them that this
	 * syntax has a NAME.  
	 * @param s
	 */
	public void setSymbol(SchemeSymbol s){
		//System.out.println("caches are " + flatCache + " _ " + namedCache + " _ " + fullCache);
		//System.out.println(getClass());
		for(SchemeSyntax p : getPossible(s)){
			p.setSymbol(s);
		}
		
		for(PossibleSyntax ps : getChildren(s)){
			ps.setSymbol(s);
		}
	}

}