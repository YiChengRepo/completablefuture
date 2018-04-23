package com.nurkiewicz.reactive.stackoverflow;

import com.google.common.base.Throwables;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpStackOverflowClient implements StackOverflowClient {

    private static final Logger log = LoggerFactory.getLogger(HttpStackOverflowClient.class);


	@Override
	public String mostRecentQuestionAbout(String tag) {
		return fetchTitleOnline(tag);
	}

	@Override
	public Document mostRecentQuestionsAbout(String tag) {
		try {
			return Jsoup.
					connect("http://stackoverflow.com/questions/tagged/" + tag).
					get();
		} catch (IOException e) {
		    log.error("io exception :" + e.getMessage());
			throw Throwables.propagate(e);
		}
	}

	private String fetchTitleOnline(String tag) {
		return mostRecentQuestionsAbout(tag).select("a.question-hyperlink").get(0).text();
	}

}
