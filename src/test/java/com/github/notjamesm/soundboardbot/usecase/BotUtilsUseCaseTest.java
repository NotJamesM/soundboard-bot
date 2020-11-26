package com.github.notjamesm.soundboardbot.usecase;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

class BotUtilsUseCaseTest {

    @Test
    void listSounds() {
        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        Message message = mock(Message.class);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("+list daf");
        BotUtilsUseCase botUtilsUseCase = new BotUtilsUseCase(LoggerFactory.getLogger(this.getClass()));
        botUtilsUseCase.listSounds(event);
    }

    @Test
    void listSoundsNoParameter() {
        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        Message message = mock(Message.class);
        when(event.getMessage()).thenReturn(message);
        MessageChannel channel = mock(MessageChannel.class);
        when(event.getChannel()).thenReturn(channel);
        MessageAction action = mock(MessageAction.class);
        when(channel.sendMessage(anyString())).thenReturn(action);
        when(message.getContentRaw()).thenReturn("+list");

        BotUtilsUseCase botUtilsUseCase = new BotUtilsUseCase(LoggerFactory.getLogger(this.getClass()));
        botUtilsUseCase.listSounds(event);
        verify(action).queue();

    }
}