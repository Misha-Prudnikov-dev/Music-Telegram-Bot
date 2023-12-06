package com.project.telegrambot.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.telegrambot.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, Long>{
	
 
}
