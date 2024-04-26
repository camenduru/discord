package com.camenduru.discord.repository;

import com.camenduru.discord.domain.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Job entity.
 */
@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    @Query("{'username': ?0}")
    Job findByUsername(String username);
}
