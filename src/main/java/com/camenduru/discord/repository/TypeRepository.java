package com.camenduru.discord.repository;

import com.camenduru.discord.domain.Type;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Type entity.
 */
@Repository
public interface TypeRepository extends MongoRepository<Type, String> {
    @Query("{'type': ?0}")
    Type findByType(String type);

    @Query("{'is_default': true}")
    Type findByDefaultType();
}

