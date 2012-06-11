package machination.webjava.client.scheme.biwa;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wraps a BiwaScheme object.
 * 
 * @author nick
 *
 */
public class BiwaInterpreter extends JavaScriptObject {

	protected BiwaInterpreter(){}
	
	public final native String compile(String str)/*-{
		return this.compile(str);
	}-*/;
	
	public final native String evaluate(String str)/*-{
		result = this.evaluate(str);
		if(result == undefined){
			return "undefined";
		}
		return result.toString();
	}-*/;
	
}
