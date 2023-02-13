package com.project.telegrambot.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.project.telegrambot.entity.Music;

public class SearchMusic {

	private static final String SOURCE_URL = "https://z2.fm/mp3/search?keywords=";
	private static final String SOURCE_ROOT_URL ="https://z2.fm";

	public static List<Music> getMusicList(String query) throws IOException {

		List<Music> musicList = new ArrayList<>();

		Document doc = Jsoup.connect(SOURCE_URL + query)
				.userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(5000)
				.header("Cookie", "ZvcurrentVolume=100; zvAuth=1; zvLang=0; ZvcurrentVolume=100; notice=11").get();

		Elements links = doc.select("div.song.song-xl");

		for (Element link : links.subList(0, Math.min(7, links.size()))) {

			String linkDownload = link.select("span[data-url]").first().attr("data-url");
			String title = link.select("span[data-title]").first().attr("data-title");

			
			Music music = new Music();
			music.setTittle(title);
			music.setLinkDownload(SOURCE_ROOT_URL +linkDownload);

			musicList.add(music);

		}

		return musicList;
	}
}
