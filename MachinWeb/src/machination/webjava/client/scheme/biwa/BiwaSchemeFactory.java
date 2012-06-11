package machination.webjava.client.scheme.biwa;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import machination.webjava.client.MachinationMain;
import machination.webjava.trees.scheme.DefaultSchemeEnv;
import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeFactory;
import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.PredefSyntax;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;
import machination.webjava.trees.schemegrammar.WebGrammar;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * SchemeFactory implemented in Biwa.
 * 
 * The first thing we may run into is getting the grammar loaded.
 * 
 * @author nick
 *
 */
public class BiwaSchemeFactory extends SchemeFactory{
	
	private WebGrammar grammar;
	private BiwaBaseEnv bEnv;
	private DefaultSchemeEnv env;
	
	public BiwaSchemeFactory(MachinationMain web) {
RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "data/schemegrammar6");
		
		final MachinationMain m = web;
		//System.out.println("Doing something");
		try {
			  builder.sendRequest(null, new RequestCallback() {
			    public void onError(Request request, Throwable exception) {
			       // Couldn't connect to server (could be timeout, SOP violation, etc.)
			    	System.out.println(exception.getMessage());
			    }

			    public void onResponseReceived(Request request, Response response) {
			      if (200 == response.getStatusCode()) {
			    	grabAvailable(response.getText(), m);
			      } else {
			        // Handle the error.  Can get the status text from response.getStatusText()
			    	  System.out.println(response.getStatusText());
			      }
			    }
			  });
			} catch (RequestException e) {
			  // Couldn't connect to server
				e.printStackTrace();
				System.out.println(e.getStackTrace());
		}
		
		
		//System.out.println("funcs: " + libFuncs.enumFuncNames());
	}
	
	public void grabAvailable(String gramString, MachinationMain web){
RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "data/availablebaselined2");
		
		final BiwaSchemeFactory f = this;
		final MachinationMain m = web;
		final String g = gramString;
		try {
			  builder.sendRequest(null, new RequestCallback() {
			    public void onError(Request request, Throwable exception) {
			       // Couldn't connect to server (could be timeout, SOP violation, etc.)
			    	System.out.println(exception.getMessage());
			    }

			    public void onResponseReceived(Request request, Response response) {
			      if (200 == response.getStatusCode()) {
			    	  String availableText = response.getText();
			    	  List<String> names = new ArrayList<String>(Arrays.asList(availableText.split("\\s")));

			    	grammar = new WebGrammar(g);
				  	setupBiwa();
			    	//want to have the grammar before we do this, as baseEnv uses some syntactic features
			  		bEnv = new BiwaBaseEnv(f, names);
					//after Biwa is set up, we can start making
					env = (DefaultSchemeEnv) subEnvironment(bEnv);
			  		for(String named : grammar.getNamedSyntax()){
			        	BiwaSchemeSymbol bs = new BiwaSchemeSymbol(null, f, env, named);
			        	//System.out.println(bs);
			        	//System.out.println(grammar.findSyntax(named).getPossible(bs));
			        	grammar.findSyntax(named).setSymbol(bs);
			        	env.putSyntax(bs, grammar.findSyntax(named));
			        }
			  		//System.out.println("Did load");
			  		//((BoolSyntax) getSyntax("boolean")).initialize(f);
					m.onConfigured(f);
			      } else {
			        // Handle the error.  Can get the status text from response.getStatusText()
			    	  System.out.println(response.getStatusText());
			      }
			    }
			  });
			} catch (RequestException e) {
			  // Couldn't connect to server
				e.printStackTrace();
				System.out.println(e.getStackTrace());
		}
	}

	public BiwaSchemeWrapper getScheme(){
		return biwaScheme;
	}
	
	private BiwaSchemeWrapper biwaScheme = null;
	private BiwaDump bsDump = null;
	private BiwaInterpreter bsi = null;
	
	/**
	 * Method adds placeholder objects whereever they are reasonable.  Recursively parses syntax to find out if
	 * we need them.
	 * @param root
	 */
	public void addNulls(SchemeObject root){
		
		if(root.isList()){
			SchemeList l = (SchemeList) root;
			for(Map.Entry<SyntaxRelationship, List<SchemeObject>> entry : root.getMatch().getRoleMatches().entrySet()){
				entry.getValue().add(holdSyntax(root, null, null, entry.getKey()));
			}
			for(SchemeObject child : l){
				addNulls(child);
			}
		}
	}
	
	public SchemeObject convertFromStandard(String code){
		SchemeObject root = parse(code);
		addNulls(root);
		return root;
	}
	
	/**
	 * Steps:
	 * 1) Remove nulls.
	 * 2) 
	 * @param code
	 */
	public void convertToStandard(String code){
		
	}
	
	/**
	 * It looks as if putting new javascript writes in here will break GWT.  If this is true, it prevents
	 * us from 1) directly initializing multifile Biwa from here 2) hooking Biwa's functionality from here.
	 * 
	 * Also inserts a function to help conversion to Java objects.  This is quite the ugly hack.
	 */
	private native void setupBiwa()/*-{
	  	this.@machination.webjava.client.scheme.biwa.BiwaSchemeFactory::biwaScheme = $doc.biwascheme;
		this.@machination.webjava.client.scheme.biwa.BiwaSchemeFactory::bsDump = $doc.bsdump;
		this.@machination.webjava.client.scheme.biwa.BiwaSchemeFactory::bsi = $doc.biwai;
		BiwaScheme = $doc.biwascheme;
		objConvert = function (obj){
			if((obj === true) || (obj === false)){
				return new Boolean(obj);
			}
			if(obj instanceof Boolean){
				return obj;
			}
			var numAttempt = new Number(obj);
			if(!isNaN(numAttempt)){
				return numAttempt;
			}
			
			
			
			return obj;
		}
		
		function SchemeBool(value){
			this.value = value;
			SchemeBool.prototype = {
				value: null,
				to_write: function(){
					if(this.value === false){
						return "#f";
					}
					return "#t";
				},
				toString: function(){
					return this.to_write();
				}
			}
		}
		var stupidity = new SchemeBool(1);
		BiwaScheme.false_literal = new SchemeBool(false);
		BiwaScheme.true_literal = new SchemeBool(true);
		BiwaScheme.bool_type = SchemeBool;
	}-*/;
	
	@Override
	public boolean isNil(SchemeObject obj) {
		if(obj.getDatum() == null){
			return false;
		}
		if(obj instanceof BSString){
			return false;
		}
		return getScheme().isNil((BiwaObject) obj.getDatum());
	}

	@Override
	public SchemeEnv getEnv() {
		return env;
	}

	@Override
	public SchemeObject lowerEnvironmentSymbol(SchemeObject parent,
			SchemeSymbol origin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesLiteral(SchemeObject o, String string) {
		return o.getString().equals(string);
	}

	@Override
	public SchemeObject makeLiteral(PredefSyntax literalSyntax) {
		if(literalSyntax.getName().equals("#f")){
			return new BiwaSchemeBool(null, this, null, getBoolean(false));
		}
		if(literalSyntax.getName().equals("#t")){
			//System.out.println("Should have a literal true - " + getBoolean(true));
			return new BiwaSchemeBool(null, this, null, getBoolean(true));
		}
		throw new UnsupportedOperationException("Trying to get literal from " + literalSyntax);
	}

	@Override
	public SchemeList makeEmptyList(SchemeObject parent) {
		return new BiwaSchemeList(parent, this, parent == null ? getEnv() : parent.getEnvironment());
	}

	@Override
	public PossibleSyntax getSyntax(String string) {
		//System.out.println("finding syntax " + string);
		return grammar.findSyntax(string);
	}
	
	@Override
	public SchemeObject fromObject(SchemeObject parent, Object body,
			SchemeEnv ke) {
		//System.out.println("boolean true is: " + getBoolean(true));
		if(body instanceof Boolean){
			if(Boolean.TRUE.equals(body)){
				return new BiwaSchemeBool(parent, this, ke, getBoolean(true));
			}if(Boolean.FALSE.equals(body)){
				return new BiwaSchemeBool(parent, this, ke, getBoolean(false));
			}
		}
		throw new UnsupportedOperationException("Can't convert Java" + body + " to Biwa");
	}
	
	/**
	 * @param val
	 * @return
	 */
	private native BiwaObject getBoolean(boolean val)/*-{
		if(val){
			return BiwaScheme.true_literal;
		}
		return BiwaScheme.false_literal;
	}-*/;
	
	public BiwaObject partialParse(String str){
		return biwaScheme.parse(str);
	}
	
	/**
	 * A hack since strings are not BiwaObjects
	 * @param str
	 * @return
	 */
	public String stringParse(String str){
		return biwaScheme.parseToString(str);
	}
	
	/**
	 * This actually implements our metaparsing system.  
	 * @param str
	 * @return
	 */
	public SchemeObject parse(String str){
		SchemeEnv newEnv = subEnvironment(env);
		BiwaObject parsed = partialParse("(" + str + ")");
		SchemeObject o = fromBiwaObject(parsed, null, newEnv);
		o.completeSyntax();
		return o;
	}
	
	public SchemeObject fromBiwaObject(BiwaObject o, SchemeObject parent, SchemeEnv newEnv){
		//System.out.println("objectifying " + o.to_write());
		if(o.to_write().equals("null") || o.to_write().equals("__unspecified")){
			return new SchemeObject(parent, this, newEnv);
		}
		if(o.to_write().startsWith("__unspecifieds")){
			return new SchemeSymbol(parent, this, newEnv);
		}
		if(o.isPair()){
			BiwaPair p = (BiwaPair) o;
			BiwaSchemeList l = new BiwaSchemeList(parent, this, newEnv, p);
			return l;
		}if(o.isSymbol()){
			return new BiwaSchemeSymbol(parent, this, newEnv, (BiwaSymbol) o);
		}if(o.isNumber()){
			return new BiwaSchemeNumber(parent, this, newEnv, ((BiwaNumber) o));
		}if(o.isBool()){
			return new BiwaSchemeBool(parent, this, newEnv, o);
		}if(getScheme().isNil(o)){
			BiwaSchemeList nilList = new BiwaSchemeList(parent, this, newEnv);
			return nilList;
		}
		throw new UnsupportedOperationException("Creating from unsupported Scheme " + o.to_write());
	}

	public String evaluate(String str) {
		if(bsi == null){
			throw new IllegalStateException("Null Interpreter");
		}
		return bsi.evaluate(str);
	}
	
	public String evaluate(SchemeObject obj){
		return evaluate(cloneWithoutPlaceholders(null, obj).getString());
	}
	
	public String evaluateBody(SchemeObject body){
		//System.out.println("Attempting to evaluate " + body + "\n" + body.getDescriptionRecursive());
		String bString = cloneWithoutPlaceholders(null, body).getString();
		bString = bString.substring(1, bString.length() - 1);
		//System.out.println("Did get body string " + bString);
		return evaluate(bString);
	}

	public String compile(String str){
		return bsDump.dumpOPC(str, bsi);
	}

	@Override
	public SchemeObject cloneObject(SchemeObject parent, SchemeObject o,
			SchemeEnv environment) {
		if((o instanceof SchemeSymbol) && (o.getDatum() == null)){
			if(((SchemeSymbol) o).id >= 0){
				return new SchemeSymbol(parent, this, environment, ((SchemeSymbol) o).id);
			}
			return new SchemeSymbol(parent, this, environment);
		}
		if(o.getDatum() == null){
			return new SchemeObject(parent, this, environment);
		}
		if(o instanceof BSString){
			return new BSString(parent, this, environment, (String) o.getDatum());
		}
		if(((BiwaObject) o.getDatum()).isBool()){
			return new BiwaSchemeBool(parent, this, environment, (BiwaObject) o.getDatum());
		}
		if(o.isList()){
			SchemeList newList = new BiwaSchemeList(parent, this, environment);
			((SchemeList) o).uncheckHead();
			Stack<SchemeObject> toTransfer = new Stack<SchemeObject>();
			for(SchemeObject child : ((SchemeList) o)){
				toTransfer.push(child);
			}
			while(!toTransfer.isEmpty()){
				newList.shift(cloneObject(newList, toTransfer.pop(), environment));
			}
			if(((SchemeList) o).getHead() != null){
				newList.shift(cloneObject(newList, ((SchemeList) o).getHead(), environment));
			}
			newList.setProper(((SchemeList) o).isProper());
			newList.setParent(parent);
			((SchemeList) o).checkHead();
			return newList;
		}
		//TODO: actually clone datums, rather than reparsing entire string
		return fromBiwaObject(partialParse(o.getString()), parent, environment);
	}
	
	/**
	 * Constructs a placeholder-free schemeobject based on what was passed in.  The intention
	 * of this is to obtain something that can be evaluated by the base interpreter.
	 * 
	 * This is supposed to get rid of placeholders.  Are nil values being mis-entered
	 * as placeholders?
	 * @param o
	 * @return
	 */
	public SchemeObject cloneWithoutPlaceholders(SchemeObject parent, SchemeObject o){
		if(o.isPlaceHolder()){
			//System.out.println("placeholder found: " + o + " nil? " + o.getFactory().isNil(o));
			return null;
		}
		if(o.isList()){
			SchemeList newList = new BiwaSchemeList(parent, this, o.getEnvironment());
			Stack<SchemeObject> toTransfer = new Stack<SchemeObject>();
			//System.out.println("cloning list: " + ((SchemeList) o).accessConstituents());
			for(SchemeObject child : ((SchemeList) o)){
				//System.out.println("placeholder removal on child " + child);
				toTransfer.push(cloneWithoutPlaceholders(parent, child));
			}
			while(!toTransfer.isEmpty()){
				if(toTransfer.peek() == null){
					toTransfer.pop();
					continue;
				}
				newList.shift(toTransfer.pop());
			}
			if(((SchemeList) o).getHead() != null){
				newList.shift(cloneObject(newList, ((SchemeList) o).getHead(), o.getEnvironment()));
			}
			newList.setProper(((SchemeList) o).isProper());
			newList.setParent(parent);
			return newList;
		}
		return cloneObject(parent, o, o.getEnvironment());
	}

	public SchemeList startBody() {
		SchemeList theBody = (SchemeList) getSyntax("body").getSingleton().getPossible(new SchemeObject(null, this, getEnv())).iterator().next();
		theBody.completeSyntax();
		return theBody;
	}

	@Override
	public SchemeObject fromString(SchemeObject owner, String literal) {
		return fromBiwaObject(partialParse(literal), owner.getParent(), owner.getEnvironment());
	}
}
