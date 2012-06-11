package machination.webjava.trees;


public class TreePoint implements Comparable<TreePoint>{
    public final int x;
    public final int y;

    public TreePoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int compareTo(TreePoint o) {
        int yResult = (new Integer(x)).compareTo(o.x);
        if(yResult == 0){
            return (new Integer(y)).compareTo(o.y);
        }
        return yResult;
    }

    public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
    public int hashCode(){
		//assuming 32-bit ints, this should work rather well
        return (x << 16) + (y & 0x00FF);
    }

    @Override
    public String toString(){
        return "(X: " + x + " Y: " + y +")";
    }
}
