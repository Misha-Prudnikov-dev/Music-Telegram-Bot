package com.project.telegrambot.music;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import com.project.telegrambot.entity.Music;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class SearchMusic {

	private static final String SOURCE_URL = //source;
	private static final String SOURCE_ROOT_URL ="https://z2.fm";
    private static final String GOOGLE_URL = "https://www.google.com/search?q=site:";
    private static final String[] LIST_SOURCE_FOR_TEXT = {"amalgama-lab.com","nikkur.ru","genius.com"};
    private static final String SOURCE_SECOND_URL = //source;
    private static AtomicLong genId = new AtomicLong();

    public static List<Music> getMusicListFirst(String query) throws IOException {

		List<Music> musicList = new ArrayList<>();

		Document doc = Jsoup.connect(SOURCE_URL + URLEncoder.encode(query,"UTF-8")).header("Content-Type","text/html; charset=UTF-8")
				.userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(4500)
				.header("Cookie", "ZvcurrentVolume=100; zvAuth=1; zvLang=0; ZvcurrentVolume=100; notice=11").get();

		Elements links = doc.select("div.song.song-xl");

		for (Element link : links.subList(0, Math.min(7, links.size()))) {

			String linkDownload = link.select("span[data-url]").first().attr("data-url");
			String title = link.select("span[data-title]").first().attr("data-title");
			
			Music music = new Music();
            music.setId(String.valueOf(genId.getAndIncrement()));
			music.setTittle(title);
			music.setLinkDownload(SOURCE_ROOT_URL +linkDownload);

			musicList.add(music);
		}
		return musicList;
	}
    public static List<Music> getMusicListSecond(String query) throws IOException {

        List<Music> musicList = new ArrayList<>();
        Document doc  = Jsoup.connect(SOURCE_SECOND_URL)
                    .data("q",query)
                    .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(4500)
                    .get();

        Elements links = doc.getElementsByTag("item");
        for (Element link : links.subList(0, Math.min(7, links.size()))) {

            String linkDownload = link.select("div[data-id]").first().attr("data-id");
            String title = link.select("span.artist_name").text();

            Music music = new Music();
            music.setId(String.valueOf(genId.getAndIncrement()));
            music.setTittle(title);
            music.setLinkDownload(linkDownload);

            musicList.add(music);
        }
        return musicList;
    }
    public static List<StringBuilder> getTextOfSong(String titleSong,String titleSongFromUser, TypeTextSong typeTextSong) {

        WebDriverManager.firefoxdriver().setup();
        WebDriver driver = new FirefoxDriver();
        List<StringBuilder> text = new ArrayList<>();
        List<WebElement> webElements = null;

        String source = searchTextOfSongGoogle(driver, titleSong);

        if (source==null){
            source = searchTextOfSongGoogle(driver, titleSongFromUser);
        }

        switch (typeTextSong.toString()){
            case "ORIGINAL":{
                switch (source) {
                    case "amalgama-lab.com": {
                        webElements = getOriginalTextAmalgama(driver);
                        break;
                    }
                    case "genius.com": {
                        webElements = getOriginalTextGenius(driver);
                        break;
                    }
                }
                text = processText(webElements,false);
                break;
            }
            case "TRANSLATION": {
                switch (source) {
                    case "amalgama-lab.com": {
                        webElements = getOriginalAndTranslationTextAmalgama(driver);
                        break;
                    }
                    case "nikkur.ru": {
                        webElements = getOriginalAndTranslationTextNikkur(driver);
                        break;
                    }
                    default:  {
                        webElements = getOriginalTextGenius(driver);
                        break;
                    }
                }
                text = processText(webElements,true);
                break;
            }
        }
        driver.quit();
        return text;
    }
    public static String searchTextOfSongGoogle(WebDriver driver, String titleSong) {
        for (String source : LIST_SOURCE_FOR_TEXT) {
            String url = GOOGLE_URL + source + " " + titleSong.replace("&","");
            driver.get(url);
            WebElement element = null;

            try {
                element = driver.findElement(By.xpath("//a[contains(@jsname, 'UWckNb') and self::a]"));
            } catch (NoSuchElementException e) {
                continue;
            }
            String link = element.getAttribute("href");
            driver.get(link);
            return source;
        }
        return null;
    }
    public static List<StringBuilder> processText(List<WebElement> textSongElements, boolean isTwoText) {
        List<StringBuilder> textSong = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        textSong.add(sb);

        int count=0;
        for (WebElement webEl : textSongElements) {

            if (isTwoText && count%2==0) {
                sb.append(webEl.getText()).append("\n").append("\n");
            }else {
                sb.append(webEl.getText()).append("\n");
            }

            if (sb.length() > 2900 && Pattern.compile("(\\R\\s*){1}").matcher(sb).find()) {
                sb = new StringBuilder();
                textSong.add(sb);
            }

            if (Pattern.compile("(\\R\\s*){3}").matcher(sb).find()) {
                break;
            }
        }
        return textSong;
    }
    private static List<WebElement> getOriginalTextAmalgama(WebDriver driver) {
        WebElement titleElement = driver.findElement(By.xpath("//h2[contains(@class, 'original') and self::h2]"));
        List<WebElement> textSongElements = driver.findElements(By.xpath("//div[contains(@class, 'original') and self::div]"));
        textSongElements.add(0, titleElement);
        return textSongElements;
    }
    private static List<WebElement> getOriginalTextGenius(WebDriver driver) {
        return driver.findElements(By.xpath("//div[contains(@class,'Lyrics__Container-sc-1ynbvzw-1 kUgSbL') and self::div]"));
    }
    private static List<WebElement> getTranslationTextAmalgama(WebDriver driver) {
        WebElement titleElement = driver.findElement(By.xpath("//h2[contains(@class, 'translate few') and self::h2]"));
        List<WebElement> textSongElements = driver.findElements(By.xpath("//div[contains(@class, 'translate') and self::div]"));
        textSongElements.add(0, titleElement);
        return textSongElements;
    }
    private static List<WebElement> getOriginalAndTranslationTextAmalgama(WebDriver driver) {
        List<WebElement> textSongElements = driver.findElements(By.xpath("//div[contains(@class, 'string_container') and self::div]"));
        return textSongElements;
    }
    private static List<WebElement> getOriginalAndTranslationTextNikkur(WebDriver driver){
        List<WebElement> textSongElements = driver.findElements(By.tagName("tbody"));
        return textSongElements;
    }
}


