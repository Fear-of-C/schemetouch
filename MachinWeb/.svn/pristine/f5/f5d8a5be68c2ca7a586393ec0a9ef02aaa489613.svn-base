package machination.webjava.trees;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * The basic class that holds TreeNodes in an actual tree structure and can
 * compute location tables based on them.  Might be worth refactoring to
 * 1) have a tree system truly independent of display
 * 2) allow things like Scheme's improper list and vectors to live in this.
 * Actually, we should probably just alter this class to generalize for
 * both of the above cases.  With the whole "datum" deal, this class seems
 * to be handling more display than any other function.
 * 
 * This class is AMAZING at the moment - because it ONLY holds logic about
 * the displayed tree and does nothing at all with any sort of underlying tree.
 * We would almost eliminate datums entirely, but these classes need them to
 * survive in a world where the display does depend on what's getting held.
 * It's really the data that can live without this display tree.
 * 
 * @author nick
 *
 * @param <T>
 */
public class TreeList<T> extends TreeNode<T> implements Comparator<TreeNode<T>>{
	
	protected TreeSet<TreeNode<T>> displayed;
	protected TreeSet<TreeNode<T>> nondisplayed;
	
	protected Map<TreePoint, TreeNode<T>> locations;
	
	public TreeList(){
		locations = new HashMap<TreePoint, TreeNode<T>>();
		displayed = new TreeSet<TreeNode<T>>(this);
		nondisplayed = new TreeSet<TreeNode<T>>(this);
	}
	
	@Override
	protected Map<TreePoint, TreeNode<T>> getLocations() {
		return locations;
	}
	
	@Override
	public void computeTotals() {
		List<? extends TreeNode<T>> children = getDisplayedChildren();
        totalWidth = 1;
        totalDepth = 1;
        if(children != null && !children.isEmpty()){
            for (TreeNode<T> child : children){

                int candidateDepth = 1 + child.totalDepth;
                totalDepth = totalDepth < candidateDepth ? candidateDepth : totalDepth;

                for(Map.Entry<TreePoint, TreeNode<T>> childLoc : child.getLocations().entrySet()){
                    TreePoint childPoint = childLoc.getKey();
                    locations.put(new TreePoint(childPoint.x + totalWidth, childPoint.y + 1), childLoc.getValue());
                }
                totalWidth += child.totalWidth;
            }
        }
        locations.put(new TreePoint(totalWidth/2, 0), this);
	}
	
	@Override
	public List<TreeNode<T>> getDisplayedChildren() {
		return new ArrayList<TreeNode<T>>(displayed);
	}
	
	@Override
	public String getDisplayedString() {
		// TODO Auto-generated method stub
		return super.getDisplayedString();
	}
	
	@Override
	public TreeNode<T> getNodeAt(TreePoint loc) {
		return locations.get(loc);
	}
	
	@Override
	public void gotUpdated() {
		computeTotals();
		super.gotUpdated();
	}
	
	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return displayed.isEmpty();
	}
	
	@Override
	public boolean isRequest() {
		if(!super.isRequest()){
			return false;
		}
		for(TreeNode<T> child : displayed){
			if(!child.isRequest()){
				return false;
			}
		}
		return nondisplayed.isEmpty();
	}
	
	/**
	 * O(pos) execution.  Could be upgraded to run in O(logn)
	 * with the addition of another TreeMap.
	 * 
	 * @param pos
	 * @return
	 */
	public TreeNode<T> getChildByPosition(int pos){
		Iterator<TreeNode<T>> it = displayed.iterator();
		for(int i = 0; i < pos; i++){
			it.next();
		}
		return it.next();
	}
	
	/**
	 * 
	 * Executes in O(logn) of displayed children.
	 * 
	 * @param child
	 * @return the child's position relative to other displayed non-requests
	 */
	public int getPositionByChild(TreeNode<T> child){
		return displayed.headSet(child).size();
	}
	
	@Override
	public String toString(){
		StringBuilder connector = new StringBuilder();
		connector.append("TreeList w/ info: " + super.toString() + '\n');
		connector.append("Non-displayed");
		for (TreeNode<T> item : nondisplayed){
			connector.append("\tNon-displayed: " + item + "\n");
		}
		for (TreeNode <T> item : displayed){
			connector.append("\tDisplayed: " + item + "\n");
		}
		return connector.toString();
	}

	@Override
	public int compare(TreeNode<T> arg0, TreeNode<T> arg1) {
		// TODO Auto-generated method stub
		return 0;
	}
}
