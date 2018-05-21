package org.dwbzen.text.util.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.text.util.RandomGenerator;

/**
 * Picks 1 or more items from a list stored in a text file
 * @author don_bacon
 *
 */
public class ListPicker {
	
	protected static final Logger log = LogManager.getLogger(ListPicker.class);
	private String filename;
	private int numToPick = 1;
	private RandomGenerator rand = null;
	private File file;
	private int seed = 0;		// user provided
	private List<String> pickList = new ArrayList<String>();
	public static final String prefixString = "*";
	
	public ListPicker(String fname, int n) {
		filename = fname;
		numToPick = n;
		initialize();
	}
	
	private void initialize() {
		if(filename != null) {
			file = new File(filename);
			if(!file.canRead()) {
				throw new RuntimeException("Cannot read file: " + filename);
			}
		}
		readfile(file);
		setRandomSeed();
	}
	
	private void setRandomSeed() {
		long lseed = (new Date()).getTime() + seed;
		rand = new RandomGenerator(lseed );
	}

	private void readfile(File f) {
		try(BufferedReader reader = new BufferedReader(new FileReader(f))) {
			//BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = null;
			while((line = reader.readLine()) != null) {
				if(line != null && line.length() > 1) {
					if(line.startsWith(prefixString) ) {
						int n = line.indexOf(prefixString);
						pickList.add(line.substring(n+(prefixString.length() + 1)));
					}
					else {
						pickList.add(line);
					}
				}
			}

		} catch (IOException e) {
			log.error("Cannot find/read file: " + e.toString());
		}
	}

	public String pickOne() {
		String s = pickList.get(rand.randomInt(0, pickList.size()-1));
		return s;
	}
	
	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}


	public int getNumToPick() {
		return numToPick;
	}


	public void setNumToPick(int numToPick) {
		this.numToPick = numToPick;
	}

	public RandomGenerator getRand() {
		return rand;
	}

	public void setRand(RandomGenerator rand) {
		this.rand = rand;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
		setRandomSeed();
	}

	public File getFile() {
		return file;
	}

	public List<String> getPickList() {
		return pickList;
	}

	public static void main(String [] args) throws IOException {
		int numToPick = 1;
		String filename = null;
		boolean interactive = false;
		ListPicker listPicker = null;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-n")) {
				numToPick = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-file")) {
				filename = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-i")) {
				interactive = true;
			}
		}
		if(filename != null) {
			listPicker = new ListPicker(filename, numToPick);
			if(interactive) {
				System.out.print("Enter a number: ");
				BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
				String numstring = rd.readLine();
				int seed = 0;
				try {
					seed = Integer.parseInt(numstring);
				} catch(NumberFormatException e) {
					System.err.println("Not a number, using 0");
				}
				listPicker.setSeed(seed);
			}
			for(int i=0; i<numToPick; i++) {
				System.out.println(listPicker.pickOne());
			}
		}
	}

}
