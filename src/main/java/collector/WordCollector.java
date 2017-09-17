package collector;

/**
 * @deprecated use WordrCollector, SentenceProducer in util.cp package
 * 
 * Analyzes word collections (Strings delimited by white space) and collects statistics that
 * can be used by a generation (Producer) class.
 * For a given string (inline or from a file/stream), this examines all the word collections
 * of a given length 2 to n (n <= 5), and records the #instances of each word that
 * follows that word collection. Sentences formed left to right advancing  1 Word each iteration.
 * 
 * Definitions:
 * A Word is a non-empty String (length >0) treated as a unit.
 * A Sentence consists of Word(s) delimited by white space (matches Pattern \s+, [ \t\n\x0B\f\r] )
 * A Word consist of one or more word characters, 0 or more dashes, 0 or more single quotes.
 * i.e. Pattern [a-zA-Z_0-9-']
 */
public class WordCollector {

}
