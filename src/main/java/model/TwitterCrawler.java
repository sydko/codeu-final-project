package com.flatironschool.javacs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;


public class TwitterCrawler {
	// keeps track of where we started
	private final String source;
	
	// the index where the results go
	private JedisIndex index;
	
	// queue of URLs to be travelled through
	private Queue<String> queue = new LinkedList<String>();

	//@syd
	// array list of image URLs to be searched via vision for relevance
	private ArrayList<String> images = new List<String>();

	// fetcher used to get pages from Twitter
	final static TwitterFetcher wf = new TwitterFetcher();

	/**
	 * Constructor.
	 * 
	 * @param source
	 * @param index
	 */
	public TwitterCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 * 
	 * @return
	 */
	public int queueSize() {
		return queue.size();	
	}

	/**
	 * Returns the number of URLs in the arraylist of image ULRs.
	 * 
	 * @return
	 */
	public int numberOfImages() {
		return images.size();	
	}
	/**
	 * Gets a URL from the queue and indexes it.
	 * @param b 
	 * 
	 * @return Number of pages indexed.
	 * @throws IOException
	 */
	public String crawl(boolean testing) throws IOException {
		if (queue.isEmpty()) {
			return null;
		}
		String url = queue.poll();
		System.out.println("Crawling " + url);

		if (testing==false && index.isIndexed(url)) {
			System.out.println("Already indexed.");
			return null;
		}
		
		Elements paragraphs;
		if (testing) {
			paragraphs = wf.readTwitter(url);
		} else {
			paragraphs = wf.fetchTwitter(url);
		}
		index.indexPage(url, paragraphs);
		queueInternalLinks(paragraphs);
		addImages(paragraphs);	
		return url;
	}

	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
		for (Element paragraph: paragraphs) {
			queueInternalLinks(paragraph);
		}
	}

	/**
	 * Parses a paragraph and adds internal links to the queue.
	 * 
	 * @param paragraph
	 */
	private void queueInternalLinks(Element paragraph) {
		Elements elts = paragraph.select("a[href]");
		for (Element elt: elts) {
			String relURL = elt.attr("href");
			
			if (relURL.startsWith("/")) {
				String absURL = "https://www.twitter.com" + relURL;
				System.out.println(absURL);
				queue.offer(absURL);
			}
		}
	}

	/**
	* Adds image links from 
	*
	*/
	private void addImages(Element paragraph){
		//Elements images = paragraph.select("img");
		Elements images = paragraph.getElementByClassName("AdaptiveMedia-photoContainer js-adaptive-photo");

		for (Element image: images) {
			String imgURL = elt.attr("href");
			System.out.println("THIS IS THE IMAGE URL" + imgURL);
			images.add(imgURL);
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		// make a TwitterCrawler
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		String source = "https://twitter.com/BarackObama";
		TwitterCrawler wc = new TwitterCrawler(source, index);
		
		// for testing purposes, load up the queue
		Elements paragraphs = wf.fetchTwitter(source);
		wc.queueInternalLinks(paragraphs);

		// loop until we index a new page
		String res;
		do {
			res = wc.crawl(false);
		} while (res == null);
		
		Map<String, Integer> map = index.getCounts("the");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}
}
