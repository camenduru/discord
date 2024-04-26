package com.camenduru.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.camenduru.discord.domain.Job;
import com.camenduru.discord.domain.enumeration.JobSource;
import com.camenduru.discord.domain.enumeration.JobStatus;
import com.camenduru.discord.repository.JobRepository;

import reactor.core.publisher.Mono;

@Component
public class InferenceCommand implements SlashCommand {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JobRepository jobRepository;

    private static final JobStatus DEFAULT_STATUS = JobStatus.WAITING;
    private static final JobSource DEFAULT_SOURCE = JobSource.DISCORD;

    @Value("${type}")
    private String type;

    @Override
    public String getName() {
        return "inference";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String command = event.getOption("command")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get();

        String type = event.getOption("type")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse(null);
        
        String sourceChannel = event.getInteraction().getChannelId().asString();
        String sourceId = event.getInteraction().getUser().getId().asString();
        String sourceUsername = event.getInteraction().getUser().getUsername();
        Instant date = Instant.now();

        saveDiscordJob(date, sourceId, sourceChannel, sourceUsername, command, type);
            
        return  event.reply()
            .withEphemeral(false)
            .withContent("Job added to the queue.");
    }

    public void saveDiscordJob(Instant date, String sourceId, String sourceChannel, String sourceUsername, String command, String type) {
        Job job = new Job();
        job.setDate(date);
        job.setStatus(DEFAULT_STATUS);
        job.setSource(DEFAULT_SOURCE);
        job.setSourceId(sourceId);
        job.setSourceChannel(sourceChannel);
        job.setSourceUsername(sourceUsername);
        job.setCommand(command);
        job.setType(type);
        job.setAmount("0");
        job.setTotal("0");
        job.setResult("null");
        job.setUser(jobRepository.findByUsername(sourceUsername).getUser());
        System.out.println(job);
        mongoTemplate.save(job);
    }
}
