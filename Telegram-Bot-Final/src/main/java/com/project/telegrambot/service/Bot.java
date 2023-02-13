package com.project.telegrambot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.project.telegrambot.config.BotConfig;
import com.project.telegrambot.entity.Music;
import com.project.telegrambot.entity.User;
import com.project.telegrambot.repository.MusicRepository;
import com.project.telegrambot.repository.UserRepository;
import com.project.telegrambot.util.SearchMusic;

@Service
public class Bot extends TelegramLongPollingBot {

	private static final String HELP_TEXT = "Напиши название песни или исполнителя и получи трек! Или просто поделись треком из Shazam";
	private static final String SHAZAM = "Мое открытие на Shazam:";
	private static final String SHAZAM_URL = "https://www.shazam.com/track/";
	private static final String EMPTY_LINE = "";
	private static final String DATE_PATTERN = "uuuu/MM/dd HH:mm:ss";

	private final BotConfig botConfig;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private MusicRepository musicRepository;

	private static final Logger logger = Logger.getLogger(Bot.class);

	private String oldMessage;

	private List<Music> musicList;

	public Bot(BotConfig botConfig) {
		this.botConfig = botConfig;
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


		oldMessage = message.getText();
		String messageText = message.getText();
		long chatId = message.getChatId();


		if (messageText.startsWith(SHAZAM)) {

			messageText = messageText.replaceAll(SHAZAM, EMPTY_LINE);

			int index = messageText.indexOf(SHAZAM_URL);

			messageText = messageText.substring(0, index);

			getMusicListFirstSource(chatId, messageText);

		} else {

			switch (messageText) {

			case "/start":

				registerUser(message);
				break;

			case "/help":

				sendMessage(new SendMessage(String.valueOf(chatId), HELP_TEXT));
				break;

			default:

				getMusicListFirstSource(chatId, messageText);

			}
		}

	}

	private void handleCallback(CallbackQuery callbackQuery) {

		String callbackData = callbackQuery.getData();
		long messageId = callbackQuery.getMessage().getMessageId();
		long chatId = callbackQuery.getMessage().getChatId();

		Music music = musicList.stream().filter(m -> callbackData.equals(m.getLinkDownload())).findAny().orElse(null);


		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
		music.setSearchMusicDate(dtf.format(LocalDateTime.now()));

		
		music.setChatId(chatId);

		musicRepository.save(music);

		SendAudio sendAudio = new SendAudio();
		sendAudio.setChatId(chatId + "");
		sendAudio.setAudio(new InputFile(music.getLinkDownload()));
		sendAudio(sendAudio);

	}

	private void registerUser(Message message) {

		var chatId = message.getChatId();
		var chat = message.getChat();

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);

		User user = new User();

		user.setChatId(chatId);
		user.setFirstName(chat.getFirstName());
		user.setLastName(chat.getLastName());
		user.setUserName(chat.getUserName());
		user.setRegistrationDate(dtf.format(LocalDateTime.now()));

		userRepository.save(user);

		logger.info("user saved");
		
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(HELP_TEXT);

		sendMessage(sendMessage);


	}

	private void getMusicListFirstSource(long chatId, String query) {

		try {

			musicList = SearchMusic.getMusicList(query);

		} catch (IOException e) {
			logger.error("Error Search Music", e);
		}

		logger.info("Search Music Successfully");

		InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

		for (Music music : musicList) {

			rowsInLine.add(Arrays.asList(InlineKeyboardButton.builder().text(music.getTittle())
					.callbackData(music.getLinkDownload()).build()));
		}
		markupInLine.setKeyboard(rowsInLine);

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(query);
		sendMessage.setReplyMarkup(markupInLine);

		sendMessage(sendMessage);

	}

	private void sendMessage(SendMessage sendMessage) {

		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error("Error message wasn not sent ", e);
		}

	}

	private void sendAudio(SendAudio sendAudio) {

		try {
			execute(sendAudio);
		} catch (TelegramApiException e) {
			logger.error("Error audio wasn not sent ", e);

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
}
