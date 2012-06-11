package machination.webjava.client.scheme.biwa;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class FuncWrapper extends JavaScriptObject {

	protected FuncWrapper(){}
	
	public final native int getMaxArgs(String funcName)/*-{
		if(!this.storage[funcName].max){
			return -1;
		}
		return this.storage[funcName].max;
	}-*/;
	
	public final native int getMinArgs(String funcName)/*-{
		if(!this.storage[funcName].min){
			return 0;
		}
		return this.storage[funcName].min;
	}-*/;
	
	public final native JsArrayString enumFuncNames()/*-{
		var array = [];
		for(var propertyName in this.storage){
			array.push(propertyName);
		}
		return array;
	}-*/;
}
