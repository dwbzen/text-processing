package org.dwbzen.text.junit;

import java.text.BreakIterator;

import org.junit.Test;

import junit.framework.TestCase;

public class BookTest extends TestCase {

//	@Test
//	public void testVerse() {
//		String text = "No part of this publication may be reproduced , stored in a. retrieval system , or transmitted in any form or by. any means—electronic , mechanical , photocopying , recording . or other—without the written permission of the publisher\n";
//		Book book = new Book(text);
//		Sentence sentence = null;
//		Word word = null;
//		int n = 0;
//		while((sentence = book.get())!= null) {
//			System.out.println((++n) + ". " + sentence.toString());
//			System.out.println("  #words: " + sentence.size());
//			while((word = sentence.get()) != null) {
//				System.out.println("  " + word.toString());
//			}
//		}
//
//	}
	
	@Test
	public void testBoundary() {
		// Sentence break iterator looks for Upper Case after the period
		String stringToExamine = "No part of this publication may be reproduced , stored in a . Retrieval system , or transmitted in any form or by. Any means—electronic , mechanical , photocopying , recording . Or other—without the written permission of the publisher.";
        BreakIterator boundary = BreakIterator.getSentenceInstance();
        boundary.setText(stringToExamine);
        printEachForward(boundary, stringToExamine);
	}

	 public static void printEachForward(BreakIterator boundary, String source) {
	     int start = boundary.first();
	     for (int end = boundary.next();
	          end != BreakIterator.DONE;
	          start = end, end = boundary.next()) {
	          System.out.println("boundary: " + source.substring(start,end));
	     }
	 }
}
