package machination.webjava.trees.schemegrammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * This rewrites the Grammar class to use Javascript regex instead of the Java-based Scannner class.
 * It also relies on hard-coding to avoid all reflection.
 * 
 * 
 * @author nick
 *
 */
public class WebGrammar {
	
	protected WebGrammar(){
		named = new TreeSet<String>();
		syntaxMap = new TreeMap<String, PossibleSyntax>();
		varDecls = new TreeMap<String, Map<String, SyntaxRelationship>>();
		varDefs = new TreeMap<String, Map<String, SyntaxRelationship>>();
		childLists = new TreeMap<String, ChildedSyntax>();
	}
	private Map<String, Map<String, SyntaxRelationship>> varDecls;
	private Map<String, Map<String, SyntaxRelationship>> varDefs;

	
	public WebGrammar(String startString){
		this();
		Map<String, Set<String>> possibilityHierarchy= new HashMap<String, Set<String>>();
		Map<String, String> possibilitiesToProcess =
			new HashMap<String, String>();
		String[] grammarLines = startString.split("\\n");
		for(String line : grammarLines){
			line = line.trim();
			if(line.length() == 0){
				continue;
			}
			String[] nameImpl = line.split(":");
			//determine if this is a named syntax
			String name = nameImpl[0];
			if(name.startsWith("-")){
				name = name.substring(1);
			}else{
				named.add(name);
			}
			
			//break into possible and childed specifications
			String[] becomes = nameImpl[1].split("\\;");
			//System.out.println(line);
			//attempt to break into child possibilities
			String[] poss = becomes[0].split("\\|");
			String[] possible = poss[0].trim().split("\\s");
			//System.out.println("possible: " + Arrays.asList(possible) + " length " + possible.length);
			if((possible.length == 0) || ((possible.length == 1) && possible[0].trim().isEmpty())){
				//then there was nothing here, so we're ready to build a singleton
				//start scanning children soon
				possibilitiesToProcess.put(name, becomes[1]);
				syntaxMap.put(name, new PossibilitySyntax(name));
			}else if(possible[0].equals("_fromsyntax")){
				syntaxMap.put(name, new DynamicSyntax());
			}else if(possible[0].equals("_fromclass")){
				if(possible[1].equals("<Procedure>")){
					syntaxMap.put(name, new ProcedureSyntax());
					//possibilitiesToProcess.put(name, becomes[1]);
				}else if(possible[1].equals("<NilSyntax>")){
					syntaxMap.put(name, new NilSyntax());
				}else{
					throw new UnsupportedOperationException("Class " + possible[1] + " isn't implemented in web codebase");
				}
			}else if(possible[0].equals("_fromscope")){
				syntaxMap.put(name, new PossibilitySyntax(new Identifier(name)));
			}else if(possible[0].equals("_fromchars")){
				//System.out.println(Arrays.asList(possible));
				ArrayList<CharacterSyntax> cs = new ArrayList<CharacterSyntax>(possible.length/3);
				for(int i = 1; i < possible.length - 1; i+=3){
					String charName = possible[i];
					String charReg = possible[i+1].substring(1, possible[i+1].length() - 1);
					String charCount = possible[i+2];
					cs.add(new CharacterSyntax(charName, charReg, Integer.parseInt(charCount)));
				}
				syntaxMap.put(name, new PossibilitySyntax(new TokenSyntax(name, cs)));
			}else{
				//System.out.println("We have possible: " + becomes[0] + " - " + poss.length);
				//System.out.println(Arrays.asList(poss));
				//then we have a PossibilitySyntax that isn't a singleton
				syntaxMap.put(name, new PossibilitySyntax(name));
				for(String pChild : poss){
					pChild = pChild.trim();
					//System.out.println("possile child "  + name + " - " + pChild);
					if(pChild.startsWith("\"")){
						((PossibilitySyntax) syntaxMap.get(name)).addPossibility(
								new PredefSyntax(pChild.substring(1, pChild.length() - 1)));
					}
					else{
						if(!possibilityHierarchy.containsKey(name)){
							possibilityHierarchy.put(name, new HashSet<String>());
						}
						possibilityHierarchy.get(name).add(pChild);
					}
				}
			}
			//System.out.println("should have added " + name);
		}
		for(Map.Entry<String, String> entry : possibilitiesToProcess.entrySet()){
			parseChildList(entry.getKey(), entry.getValue());
		}
		//System.out.println(syntaxMap);
		for(Map.Entry<String, Set<String>> entry : possibilityHierarchy.entrySet()){
			//System.out.println("possible child accruing: " + entry);
			for(String pChild : entry.getValue()){
				((PossibilitySyntax) syntaxMap.get(entry.getKey())).addPossibilities(
					syntaxMap.get(pChild));
			}
		}
		//System.out.println(childLists);
		configureScopes();
		//System.out.println(varDecls);
	}
	
	private void parseChildList(String name, String childString){
		PossibilitySyntax toAddTo = (PossibilitySyntax) syntaxMap.get(name);
		String[] childOpts = childString.split("\\|");
		for(String childList : childOpts){
			ChildedSyntax cs = new ChildedSyntax(name + childList.trim());
			childLists.put(cs.getName(), cs);
			childList = childList.replaceAll("\\]\\*\\s*\\[", "]*__[");
			//childList.replace
			childList = childList.replaceAll("\\]\\s*\\[", "]__[");
			//replacing splittable whitespace with underscores
			String[] childKeys = childList.split("__");
			//we need to get the bracketed things out
			for(String child : childKeys){
				int num = 1;
				child = child.trim();
				
				if(child.endsWith("*")){
					num = -1;
					child = child.substring(0, child.length() -1);
				}
				
				//System.out.println(name + " child: " + child);
				child = child.substring(1, child.length() - 1);

				
				String[] childParams = child.split("\\s");
				//System.out.println(Arrays.asList(childParams));
				//if(childParams[0].equals(anObject))
				String childName = childParams[childParams.length - 1];
				String childLink = childParams[childParams.length - 2];
				SyntaxRelationship rel = null;
				//this is a definite item
				if(childName.matches("\\\".*?\\\"")){
					PossibleSyntax childS = new PossibilitySyntax(
							new TokenSyntax(childName, childName.substring(1, childName.length() - 1)));
					//System.out.println("definite: " + childS.getSingleton() + " - " + childName.substring(1, childName.length() - 1));
					syntaxMap.put(childName, childS);
					rel =  new SyntaxRelationship(
							cs, childS, childName, num);
					cs.addChild(rel);
					continue;
				}
				else{
					if(syntaxMap.get(childName) == null){
						throw new IllegalStateException(name + " has child " + childName + " which is not mapped to anything.");
					}
					rel = new SyntaxRelationship(cs, syntaxMap.get(childName), childLink, num);
				}
				cs.addChild(rel);
				
				
				

				if(!varDecls.containsKey(cs.getName())){
					varDecls.put(cs.getName(), new HashMap<String, SyntaxRelationship>());
				}
				
				if(!varDefs.containsKey(cs.getName())){
					varDefs.put(cs.getName(), new HashMap<String, SyntaxRelationship>());
				}
				
				Map<String, SyntaxRelationship> dec = varDecls.get(cs.getName());
				Map<String, SyntaxRelationship> def = varDefs.get(cs.getName());
				for(int i = 0; i < childParams.length; i++){
					if(childParams[i].equals("_declscope")){
						cs.addOutScope(childParams[i+1]);
						dec.put(childParams[i+1], rel);
						//System.out.println("found declscope for " + name);
						i++;
					}
					if(childParams[i].equals("_enforce")){
						//then we need to make sure anything of that role is enforced strictly
						//might handle later
						i++;
					}
					if(childParams[i].equals("_declare")){
						//System.out.println("found declare for " + name);
						dec.put(childParams[i+1], rel);
						i++;
					}
					if(childParams[i].equals("_scope")){
						cs.putNewScope(rel, childParams[i+1]);
						i++;
					}
					if(childParams[i].equals("_by")){
						//need something that can become a list
						def.put(childParams[i+1], rel);
						i++;
					}
				}
			}
			toAddTo.addPossibility(cs);
		}
	}
	
	private List<SyntaxRelationship> configureScopeRecursive(SyntaxRelationship c, String name, Set<SchemeSyntax> seen){
		//if this is not the case, then search down the path that declared it
		if(c.role.equals(name)){
			LinkedList<SyntaxRelationship> r = new LinkedList<SyntaxRelationship>();
			r.add(c);
			return r;
		}
		for(SchemeSyntax s : c.child.getPossible(null)){
			if(seen.contains(s)){
				continue;
			}
			seen.add(s);
			if(s instanceof ChildedSyntax){
				for(SyntaxRelationship rel : ((ChildedSyntax) s).getChildren()){
					List<SyntaxRelationship> cl = configureScopeRecursive(rel, name, seen);
					if(cl != null){
						cl.add(0, c);
						return cl;
					}
				}
			}
		}
		return null;
	}
	
	private List<SyntaxRelationship> findDefRecursive(ChildedSyntax parent, String name){
		if(varDefs.get(parent.getName()).containsKey(name)){
			//then it was defined with a "by" - this gives us the definition right here!
			LinkedList<SyntaxRelationship> theList = new LinkedList<SyntaxRelationship>();
			SyntaxRelationship rel = varDefs.get(parent.getName()).get(name);
			theList.add(rel);
			return theList;
		}
		//otherwise, go down the declaring path
		for(SyntaxRelationship rel : parent.getChildren()){
			if(rel.role.equals(name)){
				for(SchemeSyntax s : rel.child.getPossible(null)){
					if(s instanceof ChildedSyntax){
						List<SyntaxRelationship> definingList = findDefRecursive((ChildedSyntax) s, name);
						if(definingList != null){
							definingList.add(rel);
							return definingList;
						}
					}
				}
			}
		}
		return null;
	}
	
	public void configureScopes(){
		//System.out.println(varDecls);
		for(Map.Entry<String, ChildedSyntax> entry : childLists.entrySet()){
			String parentName = entry.getKey();
			Map<String, SyntaxRelationship> dec = varDecls.get(parentName);
			//if no "by" defined, assume it comes from that child
			//System.out.println("looking for declaration " + parentName + " - " + dec);
			if(dec == null|| dec.isEmpty()){
				continue;
			}
			
			/*
			 * Need to search down to find the child that actually take the
			 * correct role.
			 */
			Map<String, List<SyntaxRelationship>> decChain = new HashMap<String, List<SyntaxRelationship>>();
			Map<String, List<SyntaxRelationship>> defChain = new HashMap<String, List<SyntaxRelationship>>();
			for(Map.Entry<String, SyntaxRelationship> e : dec.entrySet()){
				decChain.put(e.getKey(), configureScopeRecursive(e.getValue(), e.getKey(), new HashSet<SchemeSyntax>()));
				defChain.put(e.getKey(), findDefRecursive(entry.getValue(), e.getKey()));
			}
			entry.getValue().configureVarDecls(decChain, defChain);
			//System.out.println(entry.getKey() + "-------" + defChain);
		}
	}
	
	private Set<String> named;

	private Map<String, ChildedSyntax> childLists;
	private Map<String, PossibleSyntax> syntaxMap;
	
	public Map<String, PossibleSyntax> getAllSyntax(){
		return syntaxMap;
	}
	
	public Set<String> getNamedSyntax(){
		return named;
	}
	
	public PossibleSyntax findSyntax(String s){
		return syntaxMap.get(s);
	}
}
