package com.github.notjamesm.memebot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;

import static java.lang.String.format;

public class AudioResultHandler implements AudioLoadResultHandler {
    private final AudioPlayer audioPlayer;
    private final AudioManager guildAudioManager;
    private final VoiceChannel voiceChannel;
    private final Logger logger;

    public AudioResultHandler(AudioPlayer audioPlayer, AudioManager guildAudioManager, VoiceChannel voiceChannel, Logger logger) {
        this.audioPlayer = audioPlayer;
        this.guildAudioManager = guildAudioManager;
        this.voiceChannel = voiceChannel;
        this.logger = logger;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        logger.info(format("Playing track '%s' in channel '%s'", track.getIdentifier(), voiceChannel.getName()));
        guildAudioManager.openAudioConnection(voiceChannel);
        audioPlayer.startTrack(track, true);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

    }

    @Override
    public void noMatches() {
        logger.info("!!!!! no results !!!!!!");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        logger.info("Failed to load file: ", exception);
    }
}
