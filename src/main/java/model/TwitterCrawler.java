package model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class TwitterCrawler {
	// keeps track of where we started
	private final String source;
	
	// queue of URLs to be travelled through
	private Queue<String> queue = new LinkedList<>();

	// keeps track of the URLs we've already visited
	private ArrayList<String> history;

	//@syd
	// array list of image URLs to be searched via vision for relevance
	private ArrayList<String> images;

	// fetcher used to get pages from Twitter
	final static TwitterFetcher wf = new TwitterFetcher();

	/**
	 * Constructor.
	 * 
	 * @param source
	 *
	 */
	public TwitterCrawler(String source) {
		this.source = source;
		history = new ArrayList<>();
		images = new ArrayList<>();
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

	public List<String> getImages() {
		return images;
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
	 * Gets a URL from the queue 
	 * @param testing to decide reading or fetching
	 * @param limit the limit of crawled links
	 * @throws IOException
	 */
	public void crawl(boolean testing, int limit) throws IOException {
		for (int i = 0; i < limit; i++) {
			if (queue.isEmpty()) {
				return;
			}
			String url = queue.poll();
			System.out.println("Crawling " + url);

			if (history.contains(url)) { //if the URL is already in the list of past urls
				System.out.println("Already visited.");
				continue;
			}

			Elements paragraphs;
			if (testing) {
				paragraphs = wf.readTwitter(url);
			} else {
				paragraphs = wf.fetchTwitter(url);
			}

			history.add(url); // add the URL to the list of places visited
			queueInternalLinks(paragraphs);
			addImages(paragraphs);
		}
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
//				System.out.println(absURL);
				queue.offer(absURL); //queue link
			} else {
				queue.offer(relURL); //queue link
			}
		}
	}

	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void addImages(Elements paragraphs) {

		for (Element paragraph: paragraphs) {
			Elements content = paragraph.getElementsByClass("AdaptiveMedia-singlePhoto");
			for(Element pic : content) {
				addImages(pic);
			}

		}
	}


	/**
	* Adds image links from 
	*
	*/
	private void addImages(Element paragraph){
		Elements image_elements = paragraph.getElementsByTag("img");
//		Elements image_elements = everything.getElementsByTag("img");

		for (Element image: image_elements) {
			String imgURL = image.attr("src");
			if (!images.contains(imgURL)){ //is this a unique image url?
				//yes add it to the image arraylist
				images.add(imgURL);	
			}
			
		} 
	}
	
	public static void main(String[] args) throws IOException {
		
		// make a TwitterCrawler
		String source = "https://twitter.com/BarackObama";
		TwitterCrawler wc = new TwitterCrawler(source);
		
		// for testing purposes, load up the queue
		Elements paragraphs = wf.fetchTwitter(source);
		wc.queueInternalLinks(paragraphs);
		
		
	}
}
 