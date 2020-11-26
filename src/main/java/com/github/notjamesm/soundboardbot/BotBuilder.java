package com.github.notjamesm.soundboardbot;

import com.github.notjamesm.soundboardbot.audio.AudioPlayerSendHandler;
import com.github.notjamesm.soundboardbot.usecase.BotUtilsUseCase;
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
import java.util.Random;

public class BotBuilder {

    public static final String TOKEN = "Nzc0NzA1NDQyMDc2NjIyODQ4.X6bqqg.whryulfB_M7kwjbKXXohwHkDX0w";

    public static void build() throws LoginException {
        final Logger logger = LoggerFactory.getLogger("BirthdayBot");

        final DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        final AudioPlayer player = playerManager.createPlayer();
        final SoundboardUseCase soundboardUseCase = new SoundboardUseCase(playerManager, player, new AudioPlayerSendHandler(player), new Random(), logger);
        final JDA jda = JDABuilder.createDefault(TOKEN).enableCache(CacheFlag.EMOTE).build();
        final BotUtilsUseCase botUtilsUseCase = new BotUtilsUseCase(logger);
        final TranslateUseCase translateUseCase = new TranslateUseCase(TranslateOptions.getDefaultInstance().getService());

        jda.addEventListener(new DiscordEventListener(soundboardUseCase, botUtilsUseCase, translateUseCase, logger));
    }
}
