package machination.webjava.input;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


//may use web worker threads: http://bradkellett.com/p/javascript-worker-threads/
//this can allay fears about javascript being too slow, at least for now

/*
 * How important is it that this runs all client side?
 * What could be gained from running server side?
 * 
 * 
 */

/**
 * Class that handles getting all possible matches for a certain substring.
 * Uses suffix tables.
 * 
 * @author nick
*/
@Export
public class SuffixIndex implements Exportable{

   private String matchString;

   private final SortedMap<String, Set<String>> shortFirst;
   private final SortedMap<String, Set<String>> shortLast;

   private String[] current;
   private int size;

   public SuffixIndex(Collection<String> poss){
       shortFirst = new TreeMap<String, Set<String>>();
       for(String item : poss){
           for(int subI = 0; subI < item.length(); subI++){
               String suffix = item.substring(subI);
               if(!shortFirst.containsKey(suffix)){
                   shortFirst.put(suffix, new TreeSet<String>());
               }
               shortFirst.get(suffix).add(item);
           }
       }

       shortLast = new TreeMap<String, Set<String>>(new StringAlternateComparator());
       shortLast.putAll(shortFirst);

       current = new String[poss.size()];

       updateMatchString("");
   }
   
   /**
    *
    * Called to update the prefix string which the list model uses
    * to determine all possible completions.  Runs in O(logn^2) in
    * total strings contained and O(nlogn) in true matches.
    *
    * @param newMatch
    * @return whether or not there are any matching strings
    */
   public boolean updateMatchString(String newMatch){
       if(newMatch.equals(matchString)){
           return size > 0;
       }
       matchString = newMatch;
       //this.fireIntervalRemoved(this, 0, getSize());
       String key1 = shortFirst.tailMap(newMatch).firstKey();
       String key2 = shortLast.headMap(newMatch).lastKey();
       //System.out.println("Keys: " + key1 +"," + key2);
       if((key1 == null) || (key2 == null) || key1.compareTo(key2) > 0){
           size = 0;
           return false;
       }
       
       //needs to sort as well as set
       TreeSet<String> tempStorage = new TreeSet<String>();
       for(Set<String> valueSet : shortFirst.subMap(key1, key2).values()){
           tempStorage.addAll(valueSet);
       }
       current = tempStorage.toArray(current);
       size = tempStorage.size();

       //this.fireIntervalAdded(this, 0, getSize());
       return true;
   }

   public int getSize() {
       return size;
   }

   public Object getElementAt(int index) {
       if(index >= size){
           throw new IndexOutOfBoundsException("" + index);
       }
       return current[index];
   }

}
