package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Various utilities to generate sequences of random numbers
 * with or without replacement.
 * 
 * @author don_bacon
 *
 */
public class RandomGenerator {
	
	public static void main(String[] args) {
		List<Integer> destList =  new ArrayList<Integer>();
		List<Integer> destList2 =  new ArrayList<Integer>();
		RandomGenerator gen = new RandomGenerator();
		gen.randomIntegerList(destList, 20, 0, 19);
		for(Integer n : destList) {
			System.out.print(n + " ");
		}
		System.out.println("\n");
		gen.randomIntegerList(destList2, 20, 1, 20, true);
		for(Integer n : destList2) {
			System.out.print(n + " ");
		}
	}
	
	private ThreadLocalRandom random = null;
	
	public RandomGenerator() {
		random = ThreadLocalRandom.current();
	}
	public RandomGenerator(long seed) {
		random = ThreadLocalRandom.current();
		random.setSeed(seed);
	}
	
	public int randomInt(int low, int hi) {
		return random.nextInt(hi-low+1) + low;
	}
	
	public void randomIntegerList(List<Integer> destList, int size, int low, int hi) {
		randomIntegerList(destList, size, low, hi, false);
	}
	
	/**
	 * Simulates rolling a pair of dice.
	 * @return the total roll. If you want the value of both die, use rollNDice(int n)
	 */
	public int rollDice() {
		int roll = randomInt(1, 6) + randomInt(1,6);
		return roll;
	}
	
	/**
	 * Roll a bunch of dice
	 * @param n number of dice to roll
	 * @return int[] of individual die, where [0] is set to n, [1] is the roll on die #1 etc.
	 */
	public int[] rollNDice(int n) {
		int[] die = new int[n];
		die[0] = n;
		for(int i=0; i<n; i++) {
			die[i+1] = randomInt(1,6);
		}
		return die;
	}
	
	public void randomIntegerList(List<Integer> destList, int size, int low, int hi, boolean withReplacement) {

		int nadded = 0;
		do {
			int rint = randomInt(low, hi);
			Integer id = new Integer(rint);
			if(withReplacement) {		// don't care if it's already there
				destList.add(id);
				nadded++;
			}
			else {
				if(!destList.contains(id)) {
					destList.add(id);
					nadded++;
				}
			}
		} while(nadded < size);
	}
}
