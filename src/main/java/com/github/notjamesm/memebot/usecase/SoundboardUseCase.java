package com.github.notjamesm.memebot.usecase;

import com.github.notjamesm.memebot.audio.AudioPlayerSendHandler;
import com.github.notjamesm.memebot.audio.AudioResultHandler;
import com.github.notjamesm.memebot.audio.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class SoundboardUseCase {

    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final AudioPlayerSendHandler handler;
    private final Random random;
    private final Logger logger;

    private static final Map<String, List<Path>> sounds;

    static {
        try {
            final Path soundsPath = Path.of("sounds");
            final List<Path> directories = Files.list(soundsPath).filter(Files::isDirectory).collect(toList());
            sounds = directories.stream().map(SoundboardUseCase::getSoundPaths).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Map<String, List<Path>> getSounds() {
        return sounds;
    }

    private static Pair<String, List<Path>> getSoundPaths(Path basePath) {
        try {
            final String key = FilenameUtils.getBaseName(basePath.getFileName().toString());
            final List<Path> pathList = Files.list(basePath).filter(path -> !Files.isDirectory(path)).collect(toList());
            return Pair.of(key, pathList);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public SoundboardUseCase(AudioPlayerManager playerManager, AudioPlayer player, AudioPlayerSendHandler handler, Random random, Logger logger) {
        this.playerManager = playerManager;
        this.player = player;
        this.handler = handler;
        this.random = random;
        this.logger = logger;
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public void disconnect(MessageReceivedEvent event) {
        final Guild guild = event.getGuild();
        AudioManager guildAudioManager = guild.getAudioManager();
        player.stopTrack();
        guildAudioManager.closeAudioConnection();
    }

    public void playIntro(VoiceChannel channelJoined, String member) {
        playSound(null, getCommandParameters("+sb intros " + member), channelJoined, channelJoined.getGuild());
    }

    public void soundBoardRequest(MessageReceivedEvent event) {
        final VoiceChannel voiceChannel = getVoiceChannel(event);
        if (validateRequest(event, voiceChannel)) {
            playSound(event.getChannel(), getCommandParameters(event.getMessage().getContentRaw().toLowerCase()), voiceChannel, event.getGuild());
        }
    }

    public void playSound(MessageChannel channel, List<String> commandParameters, VoiceChannel voiceChannel, Guild guild) {
        AudioManager guildAudioManager = guild.getAudioManager();
        TrackScheduler trackScheduler = new TrackScheduler(guildAudioManager);
        player.addListener(trackScheduler);
        guildAudioManager.setSendingHandler(handler);

        AudioSourceManagers.registerLocalSource(playerManager);
        final Optional<Path> identifier = determineSoundToPlay(commandParameters);
        if (identifier.isPresent()) {
            sendSoundPlayRequest(voiceChannel, guildAudioManager, identifier.get());
        } else {
            channel.sendMessage(format("Category not found for '%s'\nAvailable categories are '%s'",
                    commandParameters,
                    Arrays.toString(sounds.keySet().toArray()))).queue();
        }
    }

    private void sendSoundPlayRequest(VoiceChannel voiceChannel, AudioManager guildAudioManager, Path identifier) {
        playerManager.loadItem(identifier.toString(), new AudioResultHandler(player, guildAudioManager, voiceChannel, logger));
    }

    private Optional<Path> determineSoundToPlay(List<String> commandParameters) {
        if (commandParameters.size() < 2) {
            return Optional.ofNullable(randomFileFromCategory(randomCategory()));
        }
        final String soundCategory = commandParameters.get(1).toLowerCase();

        if (!sounds.containsKey(soundCategory)) {
            return Optional.empty();
        }

        if (commandParameters.size() > 2) {
            final String soundRequest = StringUtils.join(commandParameters, ' ', 2, commandParameters.size());
            return sounds.get(soundCategory).stream()
                    .filter(path -> getBaseName(path).contains(soundRequest))
                    .findFirst();
        } else {
            return Optional.ofNullable(randomFileFromCategory(soundCategory));
        }
    }

    private String getBaseName(Path path) {
        return FilenameUtils.getBaseName(path.getFileName().toString().replace('_', ' '));
    }

    private String randomCategory() {
        final List<String> categories = new ArrayList<>(sounds.keySet());
        categories.remove("intros");
        return categories.get(random.nextInt(categories.size()));
    }

    private Path randomFileFromCategory(String soundCategory) {
        final List<Path> pathList = sounds.get(soundCategory);
        return pathList.get(new Random().nextInt(pathList.size()));
    }

    private List<String> getCommandParameters(String contentRaw) {
        return Arrays.asList(contentRaw.split(" "));
    }

    private boolean validateRequest(MessageReceivedEvent event, VoiceChannel voiceChannel) {
        if (voiceChannel == null) {
            event.getMessage().getChannel().sendMessage("You must be in a voice channel to use this!").queue();
            return false;
        }

        if (player.getPlayingTrack() != null) {
            event.getMessage().getChannel().sendMessage("I'm already playing somewhere else, chill bruv...").queue();
            return false;
        }
        return true;
    }

    private VoiceChannel getVoiceChannel(MessageReceivedEvent event) {
        return event.getMember().getVoiceState().getChannel();
    }
}
