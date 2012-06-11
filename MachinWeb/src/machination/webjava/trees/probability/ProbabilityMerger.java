package machination.webjava.trees.probability;

/**
 * Merges different sources of probability data.
 * 
 * 1) The deltas of user input.
 * 2) The probabilities of the tree.
 * Future:
 * 3) Conceptual data from user input.
 * 4) Comments - we can mix these with code probabilities and build a separate
 * engine for comment creation.
 * 5) Variable names - we can also make a separate engine for creating new vars.
 * 
 * Like the rest of the program, should decide primarily based on statistics.
 * 
 * Each source of probabilities should have
 * 1) a logarithmic way to specify probabilities from only that.
 * 2) an O(n) way to rank n given items.
 * which allows the merger to find the most specific source, and then augment that data by
 * all of the others.  This process should be fast as long as there aren't too many likely
 * options.  We may think about what to do when there are many likely options, and when this happens.
 * 
 * @author nick
 *
 */
public class ProbabilityMerger {

	
}
