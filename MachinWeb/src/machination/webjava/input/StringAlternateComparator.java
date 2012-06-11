package machination.webjava.input;


import java.util.Comparator;

/**
 * Overriden string comparator reverses the length-based ordering,
 * putting the longer string first.
 * @author nick
 */
public class StringAlternateComparator implements Comparator<String>{

    @Override
    public int compare(String o1, String o2) {
        if(o1.equals(o2)){
            return 0;
        }

        if(o2.startsWith(o1)){
            return 1;
        }else if(o1.startsWith(o2)){
            return -1;
        }
        return o1.compareTo(o2);
    }
}