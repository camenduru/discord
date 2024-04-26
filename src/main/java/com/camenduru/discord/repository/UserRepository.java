package com.camenduru.discord.repository;

import com.camenduru.discord.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the User entity.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    @Query("{'login': ?0}")
    User findByLogin(String login);
}
