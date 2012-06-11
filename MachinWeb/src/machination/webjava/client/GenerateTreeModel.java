package machination.webjava.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import machination.webjava.client.scheme.biwa.BSString;
import machination.webjava.client.scheme.biwa.BiwaPair;
import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.Identifier;
import machination.webjava.trees.schemegrammar.MetaSyntax;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.TokenSyntax;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

/**
 * New implementation must:
 * Create meta-objects when there are more than 4 things to display.  We should probably attempt to
 * approximate a Huffman code, running this process bottom-up.  We should also do this 95% statically,
 * changing the breakdown only when new objects enter scope.  Since this case is isolated to identifiers,
 * it should not be a problem right now.
 * -we should do this alphabetically, which is actually rather simple.  We recursively go up a "tree" of
 * the many objects available, merging all things with the same starting prefix.  The only thing even
 * slightly tricky is using exactly the right # of prefix letters.  We can always delay it until the next
 * level of the merge, leaving 1 or 2 things as single objects.
 * 
 * Effectively auto-scroll the cellBrowser.  Since mobile browsers do not naturally form scrollpanes,
 * we cannot rely on this to just happen.  Furthermore, using scrollBars is annoying and unsexy.
 * One way to do this would be to actually rely on a CellTable.  This is especially true
 * if we are not using the cellBrowser as a legit browser.  Another would be to shift the root node
 * so that its' always relatively close to the leaves.
 */	
class GenerateTreeModel implements TreeViewModel{
	
	private final MachinationMain machinationMain;
	
	private Cell<String> defaultCell = new AbstractCell<String>(){

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context,
			String v, SafeHtmlBuilder sb) {
			
			Object value = null;
			sb.appendHtmlConstant("<p>");
			if(syntaxByName.containsKey(v)){
				value = syntaxByName.get(v);
				
				if(value instanceof PossibleSyntax){
					sb.appendHtmlConstant("<em>");
	            }
	            sb.appendEscaped(((MetaSyntax<?>) value).getName());
	            if(value instanceof PossibleSyntax){
	            	sb.appendHtmlConstant("</em>");
	            }
	            return;
			}else if((currentObjs != null) && currentObjs.containsKey(v)){
				//then it's in the object list
				value = currentObjs.get(v);
				sb.appendEscaped(v);
			}else{
				sb.appendEscaped(downToLeaf(v));
			}
			sb.appendHtmlConstant("</p>");
		}
		
	};
    
    private void replaceObject(SchemeObject n){
    	SchemeObject original = this.machinationMain.treeSelect.getSelectedObject();
		this.machinationMain.treeSelect.setSelected(original, false);
    	n = this.machinationMain.fact.cloneObject(original.getParent(), n, original.getEnvironment());
    	if((original.getRole().num == -1) && (original.getDatum() == null)){
    		original.dupe();
    	}

		//((SchemeList) original.getParent()).checkClone(original, n);
		original.replace(n);
		((SchemeTreeModel) this.machinationMain.cellTree.getTreeViewModel()).refresh();
		
		
		//TODO: figure out a non-stupid way to do this
		LinkedList<SchemeObject> oList = new LinkedList<SchemeObject>();
		SchemeObject current = n;
		while(current.getParent() != null){
			oList.add(0, current);
			current = current.getParent();
		}
		TreeNode node  = this.machinationMain.cellTree.getRootTreeNode();
		for(SchemeObject childO : oList){
			for(int i = 0; i < node.getChildCount(); i++){
				if(node.getChildValue(i) == childO){
					node = node.setChildOpen(i, true);
					break;
				}
			}
		}
		
		sm.setSelected(sm.getSelectedObject(), false);
		this.machinationMain.pp.hide();
    }
    
    private PossibleSyntax root = null;

    private SelectionChangeEvent.Handler handler = new SelectionChangeEvent.Handler(){

		/**
		 * Let's register this with the treeSelction, so that we can can update the list of values.
		 * 
		 * We only update the 1st point - the rest should cascade naturally.
		 */
		@Override
		public void onSelectionChange(SelectionChangeEvent event) {
			SchemeObject newObject = GenerateTreeModel.this.machinationMain.treeSelect.getSelectedObject();
			if(newObject == null){
				root = null;
				return;
			}
			root = newObject.getSyntaxStack().get(0);
			//now we gotta update the root list

			expCode(root);
			/*
			 * Interacts poorly with singleton codings.  May attempt to dump token entries into the root
			 * list.
			 * 
			 */
			NodeInfo<String> info = (NodeInfo<String>) codes.get(root.getName());
			if(info.getCell() instanceof TextInputCell){
				baseP.setList(Collections.singletonList(root.getName()));
			}else{
				baseP.setList(((ListDataProvider<String>) info.getProvidesKey()).getList());
			}
			//now we try to scroll all the way back over
			//GenerateTreeModel.this.machinationMain.cellBrowser.getRootTreeNode().setChildOpen(0, true);
			GenerateTreeModel.this.machinationMain.cellBrowser.getElement().setScrollLeft(0);
			//System.out.println(body.getDescriptionRecursive());
		}

    	
    };
    
    /**
     * Make one for every token syntax.
     * @author nick
     *
     */
    private class TokenEntry implements ValueUpdater<String>{

    	private TokenSyntax s;
    	
		public TokenEntry(TokenSyntax syntax) {
			s = syntax;
		}

		@Override
		public void update(String value) {

			SchemeObject n = null;
			
			if(s.equals(GenerateTreeModel.this.machinationMain.fact.getSyntax("string").getSingleton())){
				value = '"' + value + '"';
				if(s.matchString(value)){
					n = new BSString(null, null, null, GenerateTreeModel.this.machinationMain.fact.stringParse(value));
					replaceObject(n);
				}
			}else if(s.matchString(value)){
					n = GenerateTreeModel.this.machinationMain.fact.fromBiwaObject(
						((BiwaPair) GenerateTreeModel.this.machinationMain.fact.partialParse(value)),
						null, null);
				replaceObject(n);
				//System.out.println(((SchemeList) n.getParent()).accessConstituents());
			}else{
				GenerateTreeModel.this.machinationMain.textBox.setText("Error: invalid string.");
				GenerateTreeModel.this.machinationMain.textBox.setFocus(true);
				//TODO: actually apply alert on field being edited
			}
		}
    }
    
    private SelectionChangeEvent.Handler nObj = new SelectionChangeEvent.Handler(){

		@Override
		public void onSelectionChange(SelectionChangeEvent event) {
			//System.out.println("Selection became " + sm.getSelectedObject());
			//System.out.println("We know about syntax with Objects " + objsByName.keySet());

			if(objsByName.containsKey(sm.getSelectedObject())){
				//System.out.println("Should be selcting for syntax " + sm.getSelectedObject());
				currentObjs = objsByName.get(sm.getSelectedObject());
			}else if(syntaxByName.containsKey(sm.getSelectedObject()) && !((currentObjs != null) && currentObjs.containsKey(sm.getSelectedObject()))){
				//System.out.println("Nullifying selection having encountered " + sm.getSelectedObject());
				currentObjs = null;
				//only 1 syntax at a time
			}
			
			if(isLeaf(sm.getSelectedObject())){
				SchemeObject context = machinationMain.treeSelect.getSelectedObject();
				context = context == null ? machinationMain.body : context;
				//System.out.println("Should replace to " + getObjectForString(sm.getSelectedObject(), context));
				replaceObject(getObjectForString(sm.getSelectedObject(), context));
			}
		}
    	
    };
    
    private SingleSelectionModel<String> sm = new SingleSelectionModel<String>();
    
    private DefaultNodeInfo<String> defaultNodeInfo;
    
    public GenerateTreeModel(MachinationMain machinationMain){
    	this.machinationMain = machinationMain;
		this.machinationMain.treeSelect.addSelectionChangeHandler(handler);
    	sm.addSelectionChangeHandler(nObj);
    	defaultNodeInfo = new DefaultNodeInfo<String>(new ListDataProvider<String>(), defaultCell, sm, null);
    }
    
    private ListDataProvider<String> textThingy = new ListDataProvider<String>(Collections.singletonList(""));
    private HashMap<TokenSyntax, TokenEntry> tokens = new HashMap<TokenSyntax, TokenEntry>();
    
    //how deep we can go before we have scrolling problems.  we might be able to ignore this if we use a scrolling library
    //and/or if we allow some fast resets
    //private int maxDepth;
	//4 is a good number for subitizing.  Not as obnoxious as 3, but more obnoxious than 5.
	public static final int SELECT_COUNT = 4;
    
	
	/**
	 * Caches exponential coding of everything.
	 * 
	 * On the other hand, we're definitely not ready to replace the syntax tree, since that has semantic
	 * meaning which doesn't exist here.  So what we're actually doing is augmenting those cases in
	 * which the syntax tree gives us too many answers.  This is information-theoretically sub-optimal,
	 * but we probably don't care too much about that, because we don't have weighted options anyway.
	 * 
	 * So we dynamically grab the list, Huffman code it bottom-up (since we're not redefining the levels
	 * anyway), and then cache that.
	 * 
	 * This whole deal could be much easier if we coded everything with strings.  On one hand, we'd lose
	 * the convenience and immediacy of being able to grab the syntax and go straight from it.  On the other
	 * hand, our data providers will have a single type (String) with a unified lookup table to find syntax.
	 * 
	 * Another option would be to return Object when we're not sure, and case on strings.  This has the advantage
	 * of avoiding a casing and/or multiple hashtables when determining the type of syntax that corresponds to a string.
	 * Unfortunately, this means we need separate maps for PossibleSyntax, SchemeSyntax and other Strings.
	 * 
	 * The problem with "codes" is that there is quite a bit of path dependence, especially if we're just taking the
	 * 1st couple letters of things.  This could be treated by a solution that also handles the problem of dynamic
	 * syntax (such as identifiers).  We could
	 * -have maps of maps
	 * -create a composite string that includes parent and/or child syntax, then pare it down at display time
	 * -use another map to isolate displayed string from the naming string
	 * 
	 * For now, prefixes seem the simplest option.  We will simply split at display time.
	 */
	private Map<String, MetaSyntax<?>> syntaxByName = new HashMap<String, MetaSyntax<?>>();
	private Set<MetaSyntax<?>> alreadyProcessed = new HashSet<MetaSyntax<?>>();
	private Map<String, NodeInfo<?>> codes = new HashMap<String, NodeInfo<?>>();
	
	private void processBreakDown(Map<String,List<String>> bro, Map<String, NodeInfo<?>> codes){
		for(Map.Entry<String, List<String>> e : bro.entrySet()){
			codes.put(e.getKey(), new DefaultNodeInfo<String>(new ListDataProvider<String>(e.getValue()), defaultCell, sm, null));
		}
	}
	
	private String downToLeaf(String str){
		String[] split = str.split(":");
		return split[split.length - 1];
	}
	
	/**
	 * Breaks down a flat list into a tree of strings
	 * @return
	 */
	private Map<String, List<String>> breakDown(String prefix, List<String> flat, int max){
		Map<String, List<String>> broken = new TreeMap<String, List<String>>();
		//System.out.println("Breaking " + prefix + " into " + flat);
		List<String> current = new ArrayList<String>(flat);
		Collections.sort(current);
		while(current.size() > max){
			List<String> prefs = new ArrayList<String>(current.size()/max);
			for(int i = 0; i < current.size(); i+=max){
				int listEnd = i + max;
				listEnd = listEnd > current.size() ? current.size() : listEnd;
				List<String> here = current.subList(i, listEnd);
				/*
				 * We need to get the longest common prefix string, methinks.
				 * If no such thing exists, we can use dashes for inclusion.
				 */

				String firstVal = downToLeaf(here.get(0));
				String[] firstVals = firstVal.split("--");
				firstVal = firstVals[0];
				

				String lastVal = downToLeaf(here.get(here.size() - 1));
				String[] lastVals = lastVal.split("--");
				lastVal = lastVals[lastVals.length - 1];
				
				String preString = prefix + ':' + firstVal + " -- " + lastVal;

				prefs.add(preString);
				broken.put(preString, here);
			}
			current = prefs;
		}
		broken.put(prefix, current);
		return broken;
	}
	
	/**
	 * Exponentially codes children of the given value.
	 * @param value
	 */
	private void expCode(MetaSyntax<?> value){
		//we want to always update the object cache to reflect the availability of the current syntax
		//
		
		//System.out.println("expCode " + value + " c " + value.getClass());
		
		if(alreadyProcessed.contains(value)){
			return;
		}else{
			alreadyProcessed.add(value);
		}
		
		if(value instanceof PossibleSyntax){
			List<String> possibilities = new ArrayList<String>();
			PossibleSyntax p = (PossibleSyntax) value;
			
			for(SchemeSyntax s : p.getPossible(machinationMain.treeSelect.getSelectedObject())){
				possibilities.add(s.getName());
				syntaxByName.put(s.getName(), s);
				//System.out.println("possible name: " + s.getName());
			}
			for(PossibleSyntax s : p.getChildren(machinationMain.treeSelect.getSelectedObject())){
				possibilities.add(s.getName());
				syntaxByName.put(s.getName(), s);
				//System.out.println("pchild name: " + s.getName());
			}
			if(possibilities.size() == 1){
				//then we're dealing with a singleton and should fold
				//if names are the same, then we should have already overwritten the syntaxByName entry for this
				//definitely make sure that we don't try to put both in the map, otherwise one will overload
				//System.out.println("Passing through singleton possible syntax " + value + " to " + syntaxByName.get(possibilities.get(0)));
				codes.put(p.getName(), getNodeInfo(possibilities.get(0)));
				return;
			}
			//System.out.println("Possibilities " + possibilities);
			processBreakDown(breakDown(p.getName(), possibilities, SELECT_COUNT), codes);
			
		}
		
		if(value instanceof SchemeSyntax){
			SchemeSyntax syntax = (SchemeSyntax) value;
			//System.out.println("Exp code SchemeSyntax " + syntax + " named " + syntax.getName());
			if(syntax instanceof TokenSyntax){	
				if(!((TokenSyntax) syntax).isSingleton()){
					//then we need to put up a text editor
					//System.out.println("Coding non-singleton token " + syntax.getName());
					if(!tokens.containsKey(syntax)){
						tokens.put((TokenSyntax) syntax, new TokenEntry((TokenSyntax) syntax));
					}
					codes.put(syntax.getName(), new DefaultNodeInfo<String>(textThingy, new TextInputCell(), null, tokens.get(syntax)));
					return;
				}
				//we should put in the text editor if and only if this token syntax is not a predef (non-singleton)
				//otherwise, it will attempt to match a singleton all the way down to another undefined object
				//if it is a singleton/predef, then it should match as would a typical singleton syntax
			}
			if(!objsByName.containsKey(syntax)){
				objsByName.put(syntax.getName(), new HashMap<String, SchemeObject>());
			}
			Map<String, SchemeObject> objectByString = objsByName.get(syntax.getName());
			List<String> available = new ArrayList<String>(syntax.getPossible(machinationMain.treeSelect.getSelectedObject()).size());
			for(SchemeObject o : syntax.getPossible(machinationMain.treeSelect.getSelectedObject())){
				available.add(o.getString());
				objectByString.put(o.getString(), o);
			}
			if(available.size() == 1){
				//singleton syntax - also fold this
				//one problem is auto-folding syntax that has environmental additions, such as procedure
				SchemeObject obj = syntax.getPossible(machinationMain.treeSelect.getSelectedObject()).iterator().next();
				codes.put(syntax.getName(), getNodeInfo(obj.getString()));
				//singleton syntax have their own very tiny object maps
				//but will we ever be asked to code these things?
				objsByName.put(syntax.getName(), Collections.singletonMap(syntax.getName(), obj));
				return;
			}
	        processBreakDown(breakDown(syntax.getName(), available, SELECT_COUNT), codes);
		}
	}
	
	private ListDataProvider<String> baseP = new ListDataProvider<String>();
	private Set<SchemeSymbol> baseSymbols = null;
	
	/*
	 * Now the problem is that we are trying to match scheme objects by string
	 * This means that when several objects have the same string representation (such as with identifier/procedure)
	 * we get into tons of trouble.  They should not, however, have the same string representation, because
	 * one of them is automatically wrapped in a list...
	 * 
	 * This also prevents us from effectively masking identifiers.
	 * Syntax is supposed to be path-independent, which is why we don't have a problem there.
	 * 
	 * Instead of using the object map, we should try to use syntax to find/generate objects.  This really should not be hard -
	 * we can actually keep a map of all the objects available to the current syntax.
	 * 
	 * The real solution might be to stop relying on strings for everything.  But that creates a proliferation of maps due to
	 * the typed nature of codes and prevents full intermixing.  So we hack for now.
	 */
	private Map<String, NodeInfo<?>> identCodes = new TreeMap<String, NodeInfo<?>>();
	//stores objects that are base identifiers
	
	private Map<String, Map<String, SchemeObject>> objsByName = new HashMap<String, Map<String, SchemeObject>>();
	
	private Map<String, SchemeObject> currentObjs = null;
	
	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		
		//System.out.println("Attempting to code " + value + " with " + codes.get(value));
		
		if((value != null) && !(value instanceof String)){
			throw new UnsupportedOperationException("Only strings should exist in cellBrower");
		}
		
		if(value == null){
			if((machinationMain.body == null) || (machinationMain.treeSelect.getSelectedObject() == null)){
				//then we haven't yet set up scheme
				baseP.getList().add("no data yet");
				return new DefaultNodeInfo<String>(baseP, defaultCell, sm, null);
			}
			//we are coding the root syntax, which might not actually exist in our codes
			PossibleSyntax root = machinationMain.treeSelect.getSelectedObject().getSyntaxStack().get(0);
			//System.out.println("Starting from " + root.getName());
			syntaxByName.put(root.getName(), root);
			expCode(root);
			return codes.get(root.getName());
		}
		
		if(syntaxByName.containsKey(value)){
			MetaSyntax<?> s  = syntaxByName.get(value);
			if(s.getName().equals("identifier")){
				if(baseSymbols == null){
					//then find the base environment
					SchemeEnv e = machinationMain.treeSelect.getSelectedObject().getEnvironment();
					while(e.getParent() != null){
						e = e.getParent();
					}
					baseSymbols = e.enumerateSymbols();
					ArrayList<String> baseIdent = new ArrayList<String>(baseSymbols.size());
					Map<String, SchemeObject> baseObjects = new HashMap<String, SchemeObject>();
					for(SchemeSymbol sym : baseSymbols){
						baseIdent.add(sym.getString());
						baseObjects.put(sym.getString(), sym);
					}
					objsByName.put("base", baseObjects);
					//these are permanent (mostly), so this should be relatively safe
					processBreakDown(breakDown("base", baseIdent, SELECT_COUNT), codes);
				}
				if(s instanceof PossibleSyntax){
					s = ((PossibleSyntax) s).getSingleton();
				}
				//then we do something special with scopes and stuff
				Set<SchemeSymbol> syms = ((Identifier) s).getPossible(machinationMain.treeSelect.getSelectedObject());
				ArrayList<String> symList = new ArrayList<String>(syms.size() - baseSymbols.size());
				//assuming the map is linked, we can break on the 1st base symbol and save a shitton of time
				if(!objsByName.containsKey("identifier")){
					objsByName.put("identifier", new HashMap<String, SchemeObject>());
				}
				Map<String, SchemeObject> objectByString = objsByName.get("identifier");
				objectByString.clear();
				for(SchemeSymbol sym : syms){
					if(baseSymbols.contains(sym)){
						break;
					}
					objectByString.put(sym.getString(), sym);
					symList.add(sym.getString());
				}
				symList.add("base");
				identCodes.clear();
				Map<String, List<String>> brokenIdent = breakDown(s.getName(), symList, SELECT_COUNT);
				processBreakDown(brokenIdent, identCodes);
			}else{
				expCode(s);
			}
		}
		
		if(!((currentObjs != null) && currentObjs.containsKey(value)) && objsByName.containsKey(value)){
			currentObjs = objsByName.get(value);
		}
				
		if(codes.containsKey(value)){
			return codes.get(value);
		}
		
		if((identCodes != null) && identCodes.containsKey(value)){
			return identCodes.get(value);
		}
		
		//otherwise, we might have an object
		if((currentObjs != null) && currentObjs.containsKey(value)){
			return defaultNodeInfo;
		}
		
		//System.out.println("co " + currentObjs);
		throw new IllegalStateException("Lacking way to code " + value + " of class " + value.getClass());
	}

	/**
	 * Could be quite the subtle question.
	 */
	@Override
	public boolean isLeaf(Object value) {
		/*
		 * Something is a leaf node if:
		 * 1) it's an object
		 * 2) it's a singleton syntax that maps to an object
		 * 3) it's a chain of singletons mapping all the way down to object level
		 */
		if(value == null){
			return false;
		}
		
		if(value.equals(textThingy.getList().get(0))){
			return true;
		}
		SchemeObject context = machinationMain.treeSelect.getSelectedObject();
		context = context == null ? machinationMain.body : context;
		return getObjectForString(value.toString(), context) != null;
	}

	public SchemeObject getObjectForString(String str, SchemeObject context){
		if((currentObjs != null) && currentObjs.containsKey(str)){
			return currentObjs.get(str);
		}
		
		if(syntaxByName.containsKey(str)){
			Object s = syntaxByName.get(str);
			while(s instanceof MetaSyntax<?>){
				if(s instanceof PossibleSyntax){
					s = ((PossibleSyntax) s).getSingleton();
				}else if(s instanceof SchemeSyntax){
					Set<? extends SchemeObject> p = ((SchemeSyntax) s).getPossible(context);
					if(((SchemeSyntax ) s) instanceof TokenSyntax){
						if(!((TokenSyntax) s).isSingleton()){
							return null;
						}
					}
					if(p.size() == 1){
						s = p.iterator().next();
					}else{
						s = null;
					}
				}
			}
			if(s instanceof SchemeObject){
				return (SchemeObject) s;
			}
		}
		
		return null;
	}
}