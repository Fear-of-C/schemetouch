package machination.webjava.client.scheme.biwa;

public class BiwaSymbol extends BiwaObject{

	protected BiwaSymbol(){}
	
	public final native String getAsString()/*-{
		return this.name;
	}-*/;
}
