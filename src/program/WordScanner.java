package program;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Scans the webpage using Jsoup parser and displays top ten frequent words and
 * word pairs.
 * 
 * @author Komal
 *
 */
public class WordScanner {
	List<String> visitedLinks = new ArrayList<String>();
	Map<String, Integer> wordCountMap = new HashMap<String, Integer>();
	Map<String, Integer> wordPairCountMap = new HashMap<String, Integer>();

	/**
	 * Connects to the url recursively upto 4th level and fetches top ten frequent
	 * words and word pairs.
	 * 
	 * @param urlString Url to be visited
	 * @param level     current expand level of the link
	 * @throws IOException
	 */
	public void readUrlContent(String urlString, int level) throws IOException {

		if (level > 4)
			return;

		// Connecting to the web page
		Connection conn = Jsoup.connect(urlString);
		System.out.println("\nlink : " + urlString);
		visitedLinks.add(urlString);
		// executing the get request
		Document doc = conn.get();
		// Retrieving the contents (body) of the web page
		String result = doc.body().text();
		// Removing punctuation from the string
		result = result.replaceAll("\\p{Punct}", "");
		System.out.println(result);

		// extract list of words from the string
		String[] words = result.split(" ");
		System.out.println("Top 10 Frequent words");
		getTopTenFrequentWords(words);
		System.out.println();
		System.out.println("Top 10 Frequent word pairs");
		getTopTenFrequentWordPairs(words);

		/*
		 * get the list of links and visit them recursively upto 4th level. A link will
		 * be visited just once.
		 */
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String linkString = link.attr("href");
			if (!visitedLinks.contains(linkString) && linkString.startsWith(urlString)) {
				// reset for each web page visited
				wordCountMap = new HashMap<String, Integer>();
				wordPairCountMap = new HashMap<String, Integer>();
				readUrlContent(linkString, ++level);

				level = 0;
			}
		}
	}

	/**
	 * Displays top ten frequent words by maintaining a map of word and its
	 * frequency.
	 * 
	 * @param words List of words to be searched
	 */
	private void getTopTenFrequentWords(String[] words) {

		for (String wordString : words) {
			String word = wordString.trim().toLowerCase();
			if (word.equals("") || word.matches("\\W+"))
				continue;
			Integer count = (wordCountMap.get(word)) == null ? 0 : wordCountMap.get(word);
			wordCountMap.put(word, count + 1);
		}

		List<Entry<String, Integer>> sortedEntries = new ArrayList<Entry<String, Integer>>(wordCountMap.entrySet());

		// sort on the frequency and retrieve top ten entries
		Collections.sort(sortedEntries, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		int count = 1;
		for (Entry<String, Integer> entry : sortedEntries) {
			if (count > 10)
				break;
			System.out.println(entry.getKey() + ", Frequency:" + entry.getValue());
			count++;
		}
	}

	/**
	 * Displays top ten frequent word pairs by maintaining a map of word pairs and
	 * their frequency.
	 * 
	 * @param words List of words to be searched
	 */
	private void getTopTenFrequentWordPairs(String[] words) {

		for (int index = 0; index < words.length - 1; index++) {
			// get the word pair
			String firstWord = words[index].trim().toLowerCase();
			String secondWord = words[index + 1].trim().toLowerCase();

			if (firstWord.equals("") || firstWord.matches("\\W+")|| secondWord.equals("") || secondWord.matches("\\W+"))
				continue;

			String wordPair = firstWord + " " + secondWord;
			Integer count = (wordPairCountMap.get(wordPair)) == null ? 0 : wordPairCountMap.get(wordPair);
			wordPairCountMap.put(wordPair, count + 1);
		}

		List<Entry<String, Integer>> sortedEntries = new ArrayList<Entry<String, Integer>>(wordPairCountMap.entrySet());

		// sort on the frequency and retrieve top ten entries
		Collections.sort(sortedEntries, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		int count = 1;
		for (Entry<String, Integer> entry : sortedEntries) {
			if (count > 10)
				break;
			System.out.println(entry.getKey() + ", Frequency:" + entry.getValue());
			count++;
		}

	}

	public static void main(String[] args){
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter a valid url: ");
		String urlString = scanner.nextLine();
		scanner.close();
		if (urlString.length() == 0)
			return;

		WordScanner wordScanner = new WordScanner();
		try {
			wordScanner.readUrlContent(urlString, 0);
		} catch (IOException e) {
			System.err.println("Exception in accesing the link: " + e.getMessage());
		}
	}
}
