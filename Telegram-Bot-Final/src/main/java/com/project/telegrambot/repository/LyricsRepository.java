package com.project.telegrambot.repository;

import com.project.telegrambot.entity.Lyrics;
import com.project.telegrambot.entity.Music;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LyricsRepository extends MongoRepository<Lyrics, String> {
}
