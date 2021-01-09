package com.github.notjamesm.soundboardbot;

import com.github.notjamesm.soundboardbot.usecase.BotUtilsUseCase;
import com.github.notjamesm.soundboardbot.usecase.SoundboardUseCase;
import com.github.notjamesm.soundboardbot.usecase.TranslateUseCase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

import java.nio.file.Path;
import java.util.List;

import static java.lang.String.format;

public class DiscordEventListener extends ListenerAdapter {

    private final SoundboardUseCase soundboardUseCase;
    private final BotUtilsUseCase botUtilsUseCase;
    private final TranslateUseCase translateUseCase;
    private final Logger logger;

    public DiscordEventListener(SoundboardUseCase soundboardUseCase, BotUtilsUseCase botUtilsUseCase, TranslateUseCase translateUseCase, Logger logger) {
        this.soundboardUseCase = soundboardUseCase;
        this.botUtilsUseCase = botUtilsUseCase;
        this.translateUseCase = translateUseCase;
        this.logger = logger;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (messageIsACommand(event)) {
            logger.info(format("Incoming command '%s', attempting to map.", event.getMessage().getContentRaw()));
            attemptToMapCommand(event);
        }
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        playIntro(event.getChannelJoined(), event.getEntity());
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        playIntro(event.getChannelJoined(), event.getEntity());
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {

    }

    private void attemptToMapCommand(@Nonnull MessageReceivedEvent event) {
        final String messageContent = event.getMessage().getContentRaw().toLowerCase();
        if (messageContent.contains("+sbt")) {
            soundboardUseCase.soundBoardRequestTargeted(event);
        } else if (messageContent.contains("+sb")) {
            soundboardUseCase.soundBoardRequest(event);
        } else if (messageContent.contains("+disconnect")) {
            soundboardUseCase.disconnect(event);
        } else if (messageContent.contains("+clean")) {
            botUtilsUseCase.clean(event);
        } else if (messageContent.contains("+help")) {
            botUtilsUseCase.help(event);
        } else if (messageContent.contains("+list")) {
            botUtilsUseCase.listSounds(event);
        } else if (messageContent.contains("+channelids")) {
            botUtilsUseCase.listChannelIds(event);
        } else if (messageContent.contains("+translate")) {
            translateUseCase.translate(event);
        } else {
            logger.info(format("Could not map command '%s'", messageContent));
        }
    }

    private boolean userHasIntro(String name) {
        final List<Path> paths = SoundboardUseCase.getSounds().get("intros");
        return paths.stream().map(Path::getFileName).map(z -> FilenameUtils.getBaseName(z.toString())).anyMatch(name::equals);
    }

    private void playIntro(VoiceChannel channelJoined, Member entity) {
        final String name = entity.getUser().getName().toLowerCase();
        if(!entity.getUser().isBot()) {
            if (userHasIntro(name)) {
                logger.info(format("User '%s' entered voice channel '%s', playing intro", name, channelJoined.getName()));
                soundboardUseCase.playIntro(channelJoined, name);
            } else {
                logger.info(format("User '%s' entered voice channel '%s', user does not have an intro", name, channelJoined.getName()));
            }
        }
    }

    private boolean messageIsACommand(@Nonnull MessageReceivedEvent event) {
        return !event.getAuthor().isBot() && event.getMessage().getContentRaw().startsWith("+");
    }


}
