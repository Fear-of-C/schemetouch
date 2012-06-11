package machination.webjava.client.scheme.biwa;

import com.google.gwt.core.client.JavaScriptObject;



public class BiwaPair extends BiwaObject{

	protected BiwaPair(){}
	
	public final native Object getCar() /*-{
		return objConvert(this.car);
	}-*/;

	public final native Object getCdr() /*-{
		return objConvert(this.cdr);
	}-*/;

	public final native int size() /*-{
		return this.length;
	}-*/;
	
	public static final native BiwaPair makePair(Object car, Object cdr)/*-{
		return new BiwaScheme.Pair(car, cdr);
	}-*/;
	
	public static final native JavaScriptObject getNil()/*-{
		return BiwaScheme.nil;
	}-*/;


	public final native BiwaPair seekPosition(int before) /*-{
		var position = this;
		var i = 0;
		for(i = 0; i < before; i++){
			position = position.cdr;
		}
		return position;
	}-*/;
	
	public final void insertChild(int before, Object child) {
		insertChildAt(seekPosition(before), child);
	}
	
	private final native void insertChildAt(BiwaPair position, Object child) /*-{
		var next = position.car;
		position.cdr = new BiwaScheme.Pair(next, position.cdr);
		position.car = child;
		
	}-*/;
	
	public final native BiwaObject removeChild(int pos)/*-{
		var position = this;
		for(var i = 0; i < pos - 1; i++){
			position = position.cdr;
		}
		position.cdr = position.cdr.cdr;
	}-*/;
	

	public final void replaceChild(int pos, Object newChild) {
		replaceChildAt(seekPosition(pos), newChild);
	}
	
	private final native void replaceChildAt(BiwaPair position, Object newChild) /*-{
		var oldCar = position.car;
		position.car = newChild;
	}-*/;

	public final native void appendChild(Object child) /*-{
		this.concat(new BiwaScheme.Pair(child, BiwaScheme.nil));
	}-*/;

	public final native BiwaPair shift(Object child) /*-{
		return new BiwaScheme.Pair(child, this);
	}-*/;

	public final native void setCar(Object obj) /*-{
		this.car = obj;
	}-*/;

	public final native void setCdr(Object obj) /*-{
		this.cdr = obj;
	}-*/;

	public final native String getCarString() /*-{
		return "\"" + this.car.toString() + "\"";
	}-*/;


}
