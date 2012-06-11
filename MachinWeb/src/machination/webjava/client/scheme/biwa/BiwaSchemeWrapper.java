package machination.webjava.client.scheme.biwa;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class BiwaSchemeWrapper extends JavaScriptObject {

	protected BiwaSchemeWrapper(){}

	public final native boolean isDefined(String str)/*-{
		return (this.TopEnv[str] != null) ||
			(this.CoreEnv[str] != null);
	}-*/;
	
	/**
	 * Methoed designed to fill a map of symbols from the environment.
	 */
	public final native JsArrayString getSymbolNames()/*-{
		var array = [];
		for(var propertyName in this.TopEnv){
			array.push(propertyName);
		}
		for(var propertyName in this.CoreEnv){
			array.push(propertyName);
		}
		return array;
	}-*/;
	

	public final native boolean isNil(BiwaObject o)/*-{
		return this.isNil(o);
	}-*/;
	
	/**
	 * What's the dumbest way to find syntax in an environment?  Find all of the possible identifiers and ask
	 * which of them are syntactic!
	 * @return
	 */
	public final native JsArrayString getSyntaxNames()/*-{
		var array = [];
		for(var propertyName in this.TopEnv){
			if(this.TopEnv[propertyName] instanceof this.Syntax){
				array.push(propertyName);
			}
		}
		for(var propertyName in this.CoreEnv){
			if(this.CoreEnv[propertyName] instanceof this.Syntax){
				array.push(propertyName);
			}
		}
		return array;
	}-*/;
	
	public final native JsArrayString getObjectMapArray()/*-{
		var array = [];
		for(var propertyName in this.TopEnv){
			array.push(propertyName);
			array.push(this.TopEnv[propertyName].toString());
		}
		for(var propertyName in this.CoreEnv){
			array.push(propertyName);
			array.push(this.CoreEnv[propertyName].toString());
		}
		return array;
	}-*/;
	
	public final native String getDefiner()/*-{
		return this.define_syntax.toString();
	}-*/;
	
	public final native FuncWrapper getFuncs()/*-{
		return this.funcStore;
	}-*/;
	
	public final native BiwaSymbol makeSymbol(String name)/*-{
		return this.Sym(name);
	}-*/;
	
	public final native String parseToString(String str)/*-{
		var parser = new this.Parser(str);
		return parser.getObject();
	}-*/;
	
	public final native BiwaObject parse(String str)/*-{
		var parser = new this.Parser(str);
		return objConvert(parser.getObject());
	}-*/;
	
	public final native BiwaInterpreter newInterpreter()/*-{
		return new this.Interpreter();
	}-*/;
}
