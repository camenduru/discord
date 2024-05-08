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
import com.camenduru.discord.repository.DetailRepository;
import com.camenduru.discord.repository.TypeRepository;

import reactor.core.publisher.Mono;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

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

    @Value("${camenduru.discord.default.result}")
    private String discordDefaultResult;

    @Value("${camenduru.discord.default.result.suffix}")
    private String discordDefaultResultSuffix;

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
            type = typeRepository.findByDefaultType().getType();
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
            if(detail == null){
                detail = detailRepository.findByLogin(defaultDiscord);
            }
        }
        job.setDate(date);
        job.setStatus(DEFAULT_STATUS);
        job.setSource(DEFAULT_SOURCE);
        job.setSourceId(sourceId);
        job.setSourceChannel(sourceChannel);
        int width = 512;
        int height = 512;
        String jsonString = job.getCommand();
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            width = jsonObject.get("width").getAsInt();
            height = jsonObject.get("height").getAsInt();
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON syntax: " + e.getMessage());
        }
        job.setResult(discordDefaultResult + width + "x" + height + discordDefaultResultSuffix);
        job.setCommand(command);
        job.setType(typeC.getType());
        job.setAmount(typeC.getAmount());
        job.setDiscord(detail);
        job.setTotal(detail);
        job.setLogin(detail.getLogin());
        job.setUser(detail.getUser());
        System.out.println(job);
        mongoTemplate.save(job);
    }
}
