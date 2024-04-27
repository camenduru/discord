package com.camenduru.discord.repository;

import com.camenduru.discord.domain.Detail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Detail entity.
 */
@Repository
public interface DetailRepository extends MongoRepository<Detail, String> {
    @Query("{'discord': ?0}")
    Detail findByDiscord(String discord);
}
