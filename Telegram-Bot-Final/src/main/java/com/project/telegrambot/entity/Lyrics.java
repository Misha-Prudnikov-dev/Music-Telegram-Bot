package com.project.telegrambot.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Lyrics")
public class Lyrics {
    @Id
    private String id;
    private String tittle;
    private String linkLyrics;
    private String typeText;
    private String searchLyricsDate;
    private Long chatId;

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

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public String getLinkLyrics() {
        return linkLyrics;
    }

    public void setLinkLyrics(String linkLyrics) {
        this.linkLyrics = linkLyrics;
    }

    public String getSearchLyricsDate() {
        return searchLyricsDate;
    }

    public void setSearchLyricsDate(String searchLyricsDate) {
        this.searchLyricsDate = searchLyricsDate;
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
        result = prime * result + ((linkLyrics == null) ? 0 : linkLyrics.hashCode());
        result = prime * result + ((searchLyricsDate == null) ? 0 : searchLyricsDate.hashCode());
        result = prime * result + ((tittle == null) ? 0 : tittle.hashCode());
        result = prime * result + ((typeText == null) ? 0 : typeText.hashCode());
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
        Lyrics other = (Lyrics) obj;
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
        if (linkLyrics == null) {
            if (other.linkLyrics != null)
                return false;
        } else if (!linkLyrics.equals(other.linkLyrics))
            return false;
        if (searchLyricsDate == null) {
            if (other.searchLyricsDate != null)
                return false;
        } else if (!searchLyricsDate.equals(other.searchLyricsDate))
            return false;
        if (tittle == null) {
            if (other.tittle != null)
                return false;
        } else if (!tittle.equals(other.tittle))
            return false;
        if (typeText == null) {
            if (other.typeText != null)
                return false;
        } else if (!typeText.equals(other.typeText))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Lyrics{" +
                "id='" + id + '\'' +
                ", tittle='" + tittle + '\'' +
                ", linkLyrics='" + linkLyrics + '\'' +
                ", typeText='" + typeText + '\'' +
                ", searchLyricsDate='" + searchLyricsDate + '\'' +
                ", chatId=" + chatId +
                '}';
    }
}
