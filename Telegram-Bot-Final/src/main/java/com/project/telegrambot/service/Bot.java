package com.project.telegrambot.service;

import com.project.telegrambot.entity.Lyrics;
import com.project.telegrambot.repository.LyricsRepository;
import com.project.telegrambot.music.TypeTextSong;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.project.telegrambot.config.BotConfig;
import com.project.telegrambot.entity.Music;
import com.project.telegrambot.entity.User;
import com.project.telegrambot.repository.MusicRepository;
import com.project.telegrambot.repository.UserRepository;
import com.project.telegrambot.music.SearchMusic;

import static com.project.telegrambot.music.TypeTextSong.*;

@Service
public class Bot extends TelegramLongPollingBot {

	private static final String HELP_TEXT = "Напиши название песни или исполнителя и получи трек! Или просто поделись треком из Shazam " +
			"А затем можешь получить текст к треку, просто выбери какой \nоригинал или перевод";
	private static final String SHAZAM = "Мое открытие на Shazam:";
	private static final String SHAZAM_URL = "https://www.shazam.com/track/";
	private static final String SOURCE_ROOT_URL ="https://z2.fm";

	private static final String EMPTY_LINE = "";
	private static final String SPACE_LINE = " ";
	private static final String DATE_PATTERN = "uuuu/MM/dd HH:mm:ss";
	private static final String TEXT = "text";
	private static final String AUTHOR_TEXT = "This App is created by \nMisha Prudnikov ©";
	private static final int PAGE_SIZE = 7;
	private static final String CURRENT = "current";
	private static final String PREVIOUS = "previous";
	private static final String NEXT = "next";
	private static final String FORWARD_SLASH = "/";
	private static final String LEFT_ARROW = "←";
	private static final String RIGHT_ARROW = "→";


	private ExecutorService executorService;
	private final Object lock = new Object();
	private final BotConfig botConfig;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MusicRepository musicRepository;
	@Autowired
	private LyricsRepository lyricsRepository;

	private static final Logger logger = Logger.getLogger(Bot.class);
	private Map<Long,MusicDTO> musicCash = new HashMap<>();

	public Bot(BotConfig botConfig) {
		this.botConfig = botConfig;
		List<BotCommand> listofCommands = new ArrayList<>();
		listofCommands.add(new BotCommand("/help", "info how to use this bot"));
		listofCommands.add(new BotCommand("/author", "get author this app"));
		this.executorService = Executors.newFixedThreadPool(5);
		try {
			this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
		} catch (TelegramApiException e) {
			logger.error("Error setting bot's command list: " + e.getMessage());
		}
	}
	public Map<Long,MusicDTO> getMusicCash() {
		synchronized (lock) {
			return musicCash;
		}
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasCallbackQuery()) {
			handleCallback(update.getCallbackQuery());
		} else if (update.hasMessage()) {
			handleMessage(update.getMessage());
		}
	}

	private void handleMessage(Message message) {

		executorService.submit( ()->{

		String messageText = message.getText();
		long chatId = message.getChatId();

		if (messageText.contains(SHAZAM_URL)) {

			int index = messageText.indexOf(SHAZAM_URL);
			messageText = messageText.substring(0, index).trim();
			getMusicList(chatId, messageText);
		} else {

			switch (messageText) {

			case "/start":
				registerUser(message);
				break;
			case "/help":
				sendMessage(new SendMessage(String.valueOf(chatId), HELP_TEXT));
				break;
			case "/author":
				sendMessage(new SendMessage(String.valueOf(chatId), AUTHOR_TEXT));
				break;
			default:
				getMusicList(chatId, messageText);
			}
		}
		});
	}

	private void handleCallback(CallbackQuery callbackQuery) {

		executorService.submit(() -> {

		String callbackData = callbackQuery.getData();
		long messageId = callbackQuery.getMessage().getMessageId();
		long chatId = callbackQuery.getMessage().getChatId();
		int pageOffset = -1;

		List<StringBuilder> text = null;
		TypeTextSong typeTextSong = null;

		switch (callbackData){
			case "previous":
				updateMusicPage(chatId,messageId,pageOffset);
				break;
			case "next":
				updateMusicPage(chatId,messageId,Math.abs(pageOffset));
				break;
			case "ORIGINAL":
			case "TRANSLATION":
				typeTextSong = TypeTextSong.valueOf(callbackData);
				MusicDTO musicDTO = getMusicCash().get(chatId);
				text = SearchMusic.getTextOfSong(musicDTO.getPrevMusic(),musicDTO.getTitleSongFromUser(), typeTextSong);
				saveLyrics(chatId,musicDTO.getPrevMusic(),callbackData);
				break;
			default:{

				List<Music> musicList = getMusicCash().get(chatId).getMusicList();

				Music music = musicList.stream()
						.filter(m -> callbackData.equals(m.getId()))
						.findAny()
						.orElse(null);

				DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
				music.setSearchMusicDate(dtf.format(LocalDateTime.now()));
				music.setId(null);
				music.setChatId(chatId);
				musicRepository.save(music);

				SendAudio sendAudio = new SendAudio();
				sendAudio.setChatId(chatId + "");
				sendAudio.setAudio(new InputFile(music.getLinkDownload()));

				InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
				List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

				createButtonsForLyrics(rowsInLine);
				markupInLine.setKeyboard(rowsInLine);
				getMusicCash().get(chatId).setPrevMusic(music.getTittle());
				sendAudio.setReplyMarkup(markupInLine);
				sendAudio(sendAudio);

			}
		}
			if (text != null) {
				for (StringBuilder sb : text) {
					SendMessage sendMessage = new SendMessage();
					sendMessage.setChatId(chatId);
					sendMessage.setText(sb.toString());
					sendMessage(sendMessage);
				}
			}
		});
		}
	private void registerUser(Message message) {

		long chatId = message.getChatId();
		Chat chat = message.getChat();
		long userId = message.getFrom().getId();

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
		User user = new User();

		user.setChatId(chatId);
		user.setFirstName(chat.getFirstName());
		user.setLastName(chat.getLastName());
		user.setUserName(chat.getUserName());
		user.setRegistrationDate(dtf.format(LocalDateTime.now()));

		getUserProfilePhoto(chatId);
		
		userRepository.save(user);
		logger.info("user saved");

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(HELP_TEXT);
		sendMessage(sendMessage);
	}

	private void getUserProfilePhoto(long chatId) {

		try {
			GetUserProfilePhotos getUserProfilePhotos = new GetUserProfilePhotos();
			getUserProfilePhotos.setUserId(chatId);
			getUserProfilePhotos.setLimit(1);

			UserProfilePhotos userProfilePhotos;
			userProfilePhotos = execute(getUserProfilePhotos);

			if (!userProfilePhotos.getPhotos().isEmpty()) {

				PhotoSize photo = userProfilePhotos.getPhotos().get(0).stream()
						.max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);

				if (photo != null) {
					String fileUrl = getFilePath(photo);
					downloadPhoto(fileUrl, chatId+".jpg");
					logger.info("user photo saved");
				}
			}
		} catch (TelegramApiException e) {
			logger.error("user doesn`t have photo",e);
		}
	}

	private String getFilePath(PhotoSize photo) {
		GetFile getFile = new GetFile();
		getFile.setFileId(photo.getFileId());
		try {
			org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
			return file.getFilePath();
		} catch (TelegramApiException e) {
			logger.error("not have file path",e);
			return null;
		}
	}

	private void downloadPhoto(String filePath, String fileName) {
	
		try {
			URL url = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath);
			InputStream inputStream = url.openStream();
			OutputStream outputStream = new FileOutputStream(fileName);

			byte[] buffer = new byte[2048]; 
			int length;

			while ((length = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}

			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			logger.error("not download photo",e);
		}
	}
	private void saveLyrics(long chatId, String title,String callbackData){
		Lyrics lyrics = new Lyrics();
		lyrics.setTittle(title);
		lyrics.setChatId(chatId);
		lyrics.setTypeText(callbackData);
		lyrics.setSearchLyricsDate(DateTimeFormatter.ofPattern(DATE_PATTERN).format(LocalDateTime.now()));
		lyricsRepository.save(lyrics);
		logger.info("lyrics was saved");
	}

	private void getMusicList(long chatId, String query) {
		int currentPage=1;
		List<Music> musicList = null;
        MusicDTO musicDTO = new MusicDTO();

			CompletableFuture<List<Music>> firstFuture = CompletableFuture.supplyAsync(() -> {
				try {
					return SearchMusic.getMusicListFirst(query);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			CompletableFuture<List<Music>> secondFuture = CompletableFuture.supplyAsync(() -> {
				try {
					return SearchMusic.getMusicListSecond(query);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

			CompletableFuture<List<Music>> combinedFuture = firstFuture.thenCombine(secondFuture, (list1, list2) -> {
				list1.addAll(list2);
				return list1;
			});
			 musicList = combinedFuture.join();

			musicDTO.setMusicList(musicList);
			musicDTO.setTitleSongFromUser(query);
			musicDTO.setCurrentPage(currentPage);

			getMusicCash().put(chatId,musicDTO);

		logger.info("Search Music Successfully");

		List<Music> musicOnPage = getMusicForPage(musicList,currentPage);

		InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

		createListButtonsMusic(rowsInLine,musicOnPage);
		createButtonsForPage(rowsInLine,musicList,chatId);
		markupInLine.setKeyboard(rowsInLine);

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(query);
		sendMessage.setReplyMarkup(markupInLine);

		sendMessage(sendMessage);

	}
	private List<Music> getMusicForPage(List<Music> musicList,int page){
		return musicList.stream().skip((page-1)*PAGE_SIZE).limit(PAGE_SIZE).collect(Collectors.toList());
	}
	private int getTotalPages(List<Music> musicList) {
		return (int) Math.ceil((double) musicList.size() / PAGE_SIZE);
	}
	private void createListButtonsMusic(List<List<InlineKeyboardButton>> rowsInLine,List<Music> music){
		for (Music m : music) {
			rowsInLine.add(Arrays.asList(
					InlineKeyboardButton.builder().text(m.getTittle()).callbackData(m.getId()).build()
			));
		}

	}
	private void createButtonsForPage(List<List<InlineKeyboardButton>> rowsInLine,List<Music> musicList, long chatId){
		int totalPage = getTotalPages(musicList);
		List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();

		int CURRENT_PAGE = getMusicCash().get(chatId).getCurrentPage();

		if (totalPage==1){
			inlineKeyboardButtons.add(InlineKeyboardButton.builder().text(CURRENT_PAGE + FORWARD_SLASH + totalPage).callbackData(CURRENT).build());
		} else if (CURRENT_PAGE==1){
			inlineKeyboardButtons.add(InlineKeyboardButton.builder().text(CURRENT_PAGE + FORWARD_SLASH + totalPage).callbackData(CURRENT).build());
			inlineKeyboardButtons.add(InlineKeyboardButton.builder().text(RIGHT_ARROW).callbackData(NEXT).build());
		} else if (CURRENT_PAGE==totalPage) {
			inlineKeyboardButtons.add(	InlineKeyboardButton.builder().text(LEFT_ARROW).callbackData(PREVIOUS).build());
			inlineKeyboardButtons.add(InlineKeyboardButton.builder().text(CURRENT_PAGE + FORWARD_SLASH + totalPage).callbackData(CURRENT).build());
		}else {
			inlineKeyboardButtons.add(	InlineKeyboardButton.builder().text(LEFT_ARROW).callbackData(PREVIOUS).build());
			inlineKeyboardButtons.add(InlineKeyboardButton.builder().text(CURRENT_PAGE + FORWARD_SLASH + totalPage).callbackData(CURRENT).build());
			inlineKeyboardButtons.add(InlineKeyboardButton.builder().text(RIGHT_ARROW).callbackData(NEXT).build());
		}
		rowsInLine.add(inlineKeyboardButtons);
	}
	private void createButtonsForLyrics(List<List<InlineKeyboardButton>> rowsInLine){
		rowsInLine.add(Arrays.asList(
				InlineKeyboardButton.builder().text(ORIGINAL.toString()).callbackData(ORIGINAL.toString()).build(),
				InlineKeyboardButton.builder().text(TRANSLATION.toString()).callbackData(TRANSLATION.toString()).build()));

	}
	private void updateMusicPage(long chatId, long messageId, int pageOffset) {

		MusicDTO musicDTO = getMusicCash().get(chatId);
		musicDTO.setCurrentPage(musicDTO.getCurrentPage()+pageOffset);
		List<Music> musicOnPage = getMusicForPage(musicDTO.getMusicList(), musicDTO.getCurrentPage());

		InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

		createListButtonsMusic(rowsInLine,musicOnPage);
		createButtonsForPage(rowsInLine,musicDTO.getMusicList(),chatId);
		markupInLine.setKeyboard(rowsInLine);

		EditMessageText editMessageText = new EditMessageText();
		editMessageText.setChatId(chatId);
		editMessageText.setMessageId((int) messageId);
		editMessageText.setText(musicDTO.getTitleSongFromUser());
		editMessageText.setReplyMarkup(markupInLine);

		sendEditMessage(editMessageText);

	}
	private void sendMessage(SendMessage sendMessage) {

		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error("Error message was not sent ", e);
		}

	}
	private void sendEditMessage(EditMessageText editMessageText) {

		try {
			execute(editMessageText);
		} catch (TelegramApiException e) {
			logger.error("Error edit message was not sent ", e);
		}

	}

	private void sendAudio(SendAudio sendAudio) {

		try {
			execute(sendAudio);
		} catch (TelegramApiException e) {
			logger.error("Error audio was not sent ", e);

		}

	}

	@Override
	public String getBotUsername() {
		return botConfig.getBotName();
	}

	@Override
	public String getBotToken() {
		return botConfig.getToken();
	}
	public void stop() {
		executorService.shutdown();
	}
}
