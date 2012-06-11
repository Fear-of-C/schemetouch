package machination.webjava.client.scheme.biwa;

import com.google.gwt.core.client.JavaScriptObject;


/**
 * Overlay types looking good!
 * 
 * @author nick
 *
 */
public class BiwaObject extends JavaScriptObject{
	
	protected BiwaObject(){}
	
	public final native boolean isPair() /*-{
		return this instanceof BiwaScheme.Pair;
	}-*/;

	public final native boolean isSymbol() /*-{
		return this instanceof BiwaScheme.Symbol;
	}-*/;

	public final native boolean isNumber() /*-{
		return this instanceof Number;
	}-*/;

	public final native boolean isBool() /*-{
		return (this === BiwaScheme.true_literal) || (this === BiwaScheme.false_literal) || (this instanceof Boolean);
	}-*/;
	
	public final native boolean isChar()/*-{
		return this instanceof BiwaScheme.Char;
	}-*/;
	

	public final native String to_write() /*-{
		var numAttempt = new Number(this);
			if(!isNaN(numAttempt)){
				return numAttempt.toString();
			}
		return this.to_write();
	}-*/;
}
