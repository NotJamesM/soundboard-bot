package com.github.notjamesm.memebot.usecase;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TranslateUseCase {

    private final Translate service;

    public TranslateUseCase(Translate service) {
        this.service = service;
    }

    public void translate(MessageReceivedEvent event) {
        final String messageId = event.getMessageId();
        final MessageChannel channel = event.getChannel();

        final MessageHistory history = channel.getHistoryBefore(messageId, 1).complete();
        final String sourceString = history.getRetrievedHistory().get(0).getContentRaw();

        Translation translation = service.translate(sourceString);

        channel.sendMessage(translation.getTranslatedText()).queue();
    }
}
