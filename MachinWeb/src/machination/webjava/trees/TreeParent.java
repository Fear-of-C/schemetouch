package machination.webjava.trees;

import java.util.List;

public interface TreeParent<T, L>{
	public void insertChild(L list, int before, T child);
	
	public T removeChild(L list, int after);
	
	public T replaceChild(L list, int pos, T newChild);
	
	public void appendChild(L list, T child);
	
	public List<T> getChildren();
}
