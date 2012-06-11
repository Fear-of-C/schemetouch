package machination.webjava.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

/**
 * Extends the string matchers by returning a fixed subset of those strings,
 * where subset is chosen by priority and
 * @author nick
 */
@Export
class PriorityMatcher implements Exportable{

    private int maxSize;

    //larger # corresponds to higher priority
    private final Map<String, Integer> priorities;
    private final List<String> items;

    private final PriorityQueue<String> sorter;

    public PriorityMatcher(int maxS, Map<String, Integer> prior){
        maxSize = maxS;
        items = new ArrayList<String>(maxSize);
        priorities = prior;
        sorter = new PriorityQueue<String>(maxSize + 1, new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return priorities.get(o1).compareTo(priorities.get(o2));
            }
        });
    }

    /**
     * Implements a clever way to get the items of maximum priority
     * in something like O(nlogn), depending on the priorities map
     * implementation.
     *
     * @param newItems
     */
    private void newMatch(List<String> newItems){
        items.clear();
        if(newItems.size() <= maxSize){
            items.addAll(newItems);
        }else{
            for(String item : newItems){
                sorter.add(item);
                if(sorter.size() > maxSize){
                    sorter.poll();
                    //should always remove the smallest element
                }
            }
        }
        items.addAll(sorter);
        Collections.sort(items);
        sorter.clear();
    }

    public String get(int index){
        return items.get(index);
    }

    public int size(){
        return items.size();
    }
}