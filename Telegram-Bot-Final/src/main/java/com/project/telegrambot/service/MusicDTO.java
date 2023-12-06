package com.project.telegrambot.service;

import com.project.telegrambot.entity.Music;

import java.util.List;

public class MusicDTO {

    private String prevMusic;
    private String titleSongFromUser;
    private List<Music> musicList;
    private int currentPage;

    public String getPrevMusic() {
        return prevMusic;
    }

    public void setPrevMusic(String prevMusic) {
        this.prevMusic = prevMusic;
    }

    public String getTitleSongFromUser() {
        return titleSongFromUser;
    }

    public void setTitleSongFromUser(String titleSongFromUser) {
        this.titleSongFromUser = titleSongFromUser;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public int getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + currentPage;
        result = prime * result + ((musicList == null) ? 0 : musicList.hashCode());
        result = prime * result + ((prevMusic == null) ? 0 : prevMusic.hashCode());
        result = prime * result + ((titleSongFromUser == null) ? 0 : titleSongFromUser.hashCode());
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
        MusicDTO other = (MusicDTO) obj;
        if (currentPage != other.currentPage)
            return false;
        if (musicList == null) {
            if (other.musicList != null)
                return false;
        } else if (!musicList.equals(other.musicList))
            return false;
        if (prevMusic == null) {
            if (other.prevMusic != null)
                return false;
        } else if (!prevMusic.equals(other.prevMusic))
            return false;
        if (titleSongFromUser == null) {
            if (other.titleSongFromUser != null)
                return false;
        } else if (!titleSongFromUser.equals(other.titleSongFromUser))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MusicDTO{" +
                "prevMusic='" + prevMusic + '\'' +
                ", titleSongFromUser='" + titleSongFromUser + '\'' +
                ", musicList=" + musicList +
                ", currentPage=" + currentPage +
                '}';
    }
}
