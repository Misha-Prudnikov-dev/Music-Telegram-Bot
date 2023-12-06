package com.project.telegrambot.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.telegrambot.entity.Music;

@Repository
public interface MusicRepository extends MongoRepository<Music, String> {

	@Query(value = "{'chatId': ?0}")
	List<Music> queryMusicByUserId(@Param("id") Long id);
}
