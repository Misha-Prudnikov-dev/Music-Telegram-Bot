package com.project.telegrambot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.project.telegrambot.service.Bot;

@Component
public class BotInitializer {

	@Autowired
	Bot telegramBot;

	@EventListener({ ContextRefreshedEvent.class })
	public void init() {

		try {

			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

			telegramBotsApi.registerBot(telegramBot);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
