package com.camenduru.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.camenduru.discord.domain.Detail;
import com.camenduru.discord.domain.Job;
import com.camenduru.discord.domain.Type;
import com.camenduru.discord.domain.enumeration.JobSource;
import com.camenduru.discord.domain.enumeration.JobStatus;
import com.camenduru.discord.repository.UserRepository;
import com.camenduru.discord.repository.DetailRepository;
import com.camenduru.discord.repository.TypeRepository;

import reactor.core.publisher.Mono;

@Component
public class InferenceCommand implements SlashCommand {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DetailRepository detailRepository;

    @Autowired
    private TypeRepository typeRepository;

    private static final JobStatus DEFAULT_STATUS = JobStatus.WAITING;
    private static final JobSource DEFAULT_SOURCE = JobSource.DISCORD;

    @Value("${camenduru.discord.default.type}")
    private String defaultType;

    @Value("${camenduru.discord.default.result}")
    private String defaultResult;

    @Value("${camenduru.discord.default.discord}")
    private String defaultDiscord;

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

        if (type == null || type.isEmpty()) {
            type = this.defaultType;
        }
        
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
        Type typeC = typeRepository.findByType(type);
        Detail detail = new Detail();
        if(sourceUsername.equals(defaultDiscord)){
            detail = detailRepository.findByLogin(sourceUsername);
        }
        else{
            detail = detailRepository.findByDiscord(sourceUsername);
        }
        job.setDate(date);
        job.setStatus(DEFAULT_STATUS);
        job.setSource(DEFAULT_SOURCE);
        job.setSourceId(sourceId);
        job.setSourceChannel(sourceChannel);
        job.setCommand(command);
        job.setType(typeC.getType());
        job.setAmount(typeC.getAmount());
        job.setResult(defaultResult);
        job.setDiscord(detail);
        job.setTotal(detail);
        job.setLogin(detail.getLogin());
        job.setUser(detail.getUser());
        System.out.println(job);
        mongoTemplate.save(job);
    }
}
