package com.github.notjamesm.soundboardbot.usecase;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class BotUtilsUseCase {

    private final Logger logger;

    public BotUtilsUseCase(Logger logger) {
        this.logger = logger;
    }

    public void clean(MessageReceivedEvent event) {
        final String[] name = event.getMessage().getContentRaw().split(" ");
        if (name.length > 1) {
            final List<Emote> daf = event.getGuild().getEmotesByName("daf", true);
            event.getChannel().sendMessage(format("Go on give %s a good cleaning ;) %s", name[1], daf.get(0).getAsMention())).queue();
        }

        final MessageHistory messageHistory = event.getChannel().getHistoryBefore(event.getMessageId(), 20).complete();
        messageHistory.getRetrievedHistory().stream().filter(this::shouldDelete).forEach(message -> message.delete().queue());
        event.getMessage().delete().queue();
    }

    public void listChannelIds(MessageReceivedEvent event) {
        final List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannels();
        final List<String> message = voiceChannels.stream().map(voiceChannel -> format("%s - %s", voiceChannel.getName(), voiceChannel.getId())).collect(toList());

        event.getChannel().sendMessage(StringUtils.join(message, "\n")).queue();
    }

    public void listSounds(MessageReceivedEvent event) {
        final String[] argument = event.getMessage().getContentRaw().toLowerCase().split(" ");
        final MessageChannel channel = event.getChannel();
        if (argument.length < 2) {
            channel.sendMessage("You must specify a valid sound category!").queue();
            return;
        }

        final Map<String, List<Path>> sounds = SoundboardUseCase.getSounds();
        final String soundCategory = argument[1];
        if (!sounds.containsKey(soundCategory)) {
            channel.sendMessage(format("Could not find sound category of '%s'.", soundCategory)).queue();
            return;
        }

        final List<String> collect = sounds.get(soundCategory).stream()
                .sorted()
                .map(this::getSoundName)
                .collect(toList());

        String message = StringUtils.join(collect, '\n');
        channel.sendMessage(message).queue();
    }

    public void help(MessageReceivedEvent event) {
        event.getChannel().sendMessage(helpMessage()).queue();
    }

    private boolean shouldDelete(Message message) {
        return message.getContentRaw().matches("\\+\\w.*") || message.getAuthor().getId().equals("774705442076622848");
    }

    private String getSoundName(Path x) {
        return FilenameUtils.getBaseName(x.getFileName().toString()).replace('_', ' ');
    }

    private String helpMessage() {
        final List<String> collect = SoundboardUseCase.getSounds().keySet().stream().map(s -> format("+sb %s", s)).collect(toList());
        return "Available commands are:\n+sb\n+clean\n+list <category+sb >\n" + StringUtils.join(collect, '\n');
    }
}
