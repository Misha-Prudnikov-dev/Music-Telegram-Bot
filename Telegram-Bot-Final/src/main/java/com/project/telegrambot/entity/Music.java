package com.project.telegrambot.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Music")
public class Music {

	@Id
	private String id;
	private String tittle;
	private String linkDownload;
	private String searchMusicDate;
	private Long chatId;

	public Music() {

	}

	public Music(String id, String title, String linkDownload, Long chatId) {
		this.id = id;
		this.tittle = title;
		this.linkDownload = linkDownload;
		this.chatId = chatId;
	}

	public Music(String id, String title, String linkDownload, String searchMusicDate, Long chatId) {
		this.id = id;
		this.tittle = title;
		this.linkDownload = linkDownload;
		this.searchMusicDate = searchMusicDate;
		this.chatId = chatId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTittle() {
		return tittle;
	}

	public void setTittle(String tittle) {
		this.tittle = tittle;
	}

	public String getLinkDownload() {
		return linkDownload;
	}

	public void setLinkDownload(String linkDownload) {
		this.linkDownload = linkDownload;
	}

	public String getSearchMusicDate() {
		return searchMusicDate;
	}

	public void setSearchMusicDate(String searchMusicDate) {
		this.searchMusicDate = searchMusicDate;
	}

	public Long getChatId() {
		return chatId;
	}

	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chatId == null) ? 0 : chatId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((linkDownload == null) ? 0 : linkDownload.hashCode());
		result = prime * result + ((searchMusicDate == null) ? 0 : searchMusicDate.hashCode());
		result = prime * result + ((tittle == null) ? 0 : tittle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Music other = (Music) obj;
		if (chatId == null) {
			if (other.chatId != null)
				return false;
		} else if (!chatId.equals(other.chatId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (linkDownload == null) {
			if (other.linkDownload != null)
				return false;
		} else if (!linkDownload.equals(other.linkDownload))
			return false;
		if (searchMusicDate == null) {
			if (other.searchMusicDate != null)
				return false;
		} else if (!searchMusicDate.equals(other.searchMusicDate))
			return false;
		if (tittle == null) {
			if (other.tittle != null)
				return false;
		} else if (!tittle.equals(other.tittle))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Music [id=" + id + ", tittle=" + tittle + ", linkDownload=" + linkDownload + ", searchMusicDate="
				+ searchMusicDate + ", chatId=" + chatId + "]";
	}

}
