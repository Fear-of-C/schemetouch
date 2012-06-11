package machination.webjava.trees;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


/**
 * 
 * This is the class that determines how Scheme code displays in the tree structure.
 * It might be entirely obselete when we can hook the UI straight into the SchemeObject
 * system.
 * 
 * @author nick
 *
 */
@Export
public class TreeNode<T> implements Exportable, Comparable<TreeNode<T>>{
	
	public static final TreePoint ZERO = new TreePoint(0,0);
	
	protected int totalWidth = 1;
	protected int totalDepth = 1;
	//private TreeMap<TreePoint, TreeNode> locations;

	protected TreePoint absLoc;
	protected int childNum;
	
	protected TreeList<T> parent;
	protected T datum;
	
	public TreeNode(){
		absLoc = null;
	}
	
	public TreeNode<T> getNodeAt(TreePoint loc){
		return ZERO.equals(loc) ? this : null;
	}
	
	public boolean hasNodeAt(TreePoint loc){
		return getNodeAt(loc) != null;
	}
	
	public boolean isLeaf(){
		return true;
	}
	
	public boolean isRoot(){
		return parent == null;
	}
	
	public boolean isRequest(){
		return datum == null;
	}
	
	public String getDisplayedString(){
		return isRequest() ? datum.toString() : datum.toString();
	}
	
	public List<TreeNode<T>> getDisplayedChildren(){
		return Collections.emptyList();
	}
	
	public void computeTotals(){
		//nothing to do here
	}
	
	public void gotUpdated(){
		parent.gotUpdated();
	}
	
	protected Map<TreePoint, TreeNode<T>> getLocations(){
		return Collections.emptyMap(); 
	}

	@Override
	public int compareTo(TreeNode<T> o) {
		return new Integer(childNum).compareTo(new Integer(o.childNum));
	}
	

	@Override
	public String toString(){
		return "TreeNode at " + absLoc + " " + totalWidth + " wide " + totalDepth + " deep " + " containing " +
			datum + " link ";
	}
	
	public List<TreeNode<T>> preOrder(){
		LinkedList<TreeNode<T>> building = new LinkedList<TreeNode<T>>();
		building.add(this);
		for(TreeNode<T> child : getDisplayedChildren()){
			building.addAll(child.preOrder());
		}
		return building;
	}
	
	public List<TreeNode<T>> postOrder(){
		LinkedList<TreeNode<T>> building = new LinkedList<TreeNode<T>>();
		for(TreeNode<T> child : getDisplayedChildren()){
			building.addAll(child.preOrder());
		}
		building.add(this);
		return building;
	}

}
