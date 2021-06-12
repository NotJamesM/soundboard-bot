package com.github.notjamesm.soundboardbot;

import com.github.notjamesm.soundboardbot.audio.AudioPlayerSendHandler;
import com.github.notjamesm.soundboardbot.usecase.BotUtilsUseCase;
import com.github.notjamesm.soundboardbot.usecase.DirectoryWatchingUtility;
import com.github.notjamesm.soundboardbot.usecase.SoundboardUseCase;
import com.github.notjamesm.soundboardbot.usecase.TranslateUseCase;
import com.google.cloud.translate.TranslateOptions;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public class BotBuilder {

    public static final String TOKEN = "**********";

    public static void build() throws LoginException, IOException {
        final Logger logger = LoggerFactory.getLogger("BirthdayBot");

        final Path sounds = Path.of("sounds");
        final DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        final AudioPlayer player = playerManager.createPlayer();
        final SoundboardUseCase soundboardUseCase = new SoundboardUseCase(playerManager, player, new AudioPlayerSendHandler(player), new Random(), logger, sounds);
        soundboardUseCase.updateSoundMap();
        final DirectoryWatchingUtility directoryWatchingUtility = new DirectoryWatchingUtility(sounds, soundboardUseCase);
        directoryWatchingUtility.watch();
        final JDA jda = JDABuilder.createDefault(TOKEN).enableCache(CacheFlag.EMOTE).build();
        final BotUtilsUseCase botUtilsUseCase = new BotUtilsUseCase(logger);
        final TranslateUseCase translateUseCase = new TranslateUseCase(TranslateOptions.getDefaultInstance().getService());

        jda.addEventListener(new DiscordEventListener(soundboardUseCase, botUtilsUseCase, translateUseCase, logger));
    }
}
