package machination.webjava.client.scheme.biwa;

import com.google.gwt.core.client.JavaScriptObject;

public class BiwaDump extends JavaScriptObject {

	protected BiwaDump(){
		
	}
	
	public final native String dumpOPC(String code, BiwaInterpreter interpreter)/*-{
		return this.dump_opc(interpreter.compile(code));
	}-*/;
}
