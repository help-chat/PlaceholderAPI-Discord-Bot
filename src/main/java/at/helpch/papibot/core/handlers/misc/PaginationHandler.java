package at.helpch.papibot.core.handlers.misc;

import at.helpch.papibot.core.handlers.GEvent;
import at.helpch.papibot.core.objects.enums.EventsEnum;
import at.helpch.papibot.core.objects.paginations.PageNotFoundException;
import at.helpch.papibot.core.objects.paginations.PaginationPage;
import at.helpch.papibot.core.objects.paginations.PaginationSet;
import com.google.inject.Singleton;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.HashMap;
import java.util.Map;

// ------------------------------
// Copyright (c) PiggyPiglet 2018
// https://www.piggypiglet.me
// ------------------------------
@Singleton
public final class PaginationHandler extends GEvent {
    @Getter private final Map<String, PaginationSet> paginations = new HashMap<>();

    public PaginationHandler() {
        super(EventsEnum.MESSAGE_REACTION_ADD, EventsEnum.MESSAGE_DELETE);
    }

    @Override
    protected void execute(GenericEvent event) {
        switch (EventsEnum.fromEvent(event)) {
            case MESSAGE_REACTION_ADD:
                GuildMessageReactionAddEvent e = (GuildMessageReactionAddEvent) event;
                if (!e.getMember().getUser().isBot()) {
                    String messageId = e.getMessageId();

                    if (paginations.containsKey(messageId)) {
                        MessageReaction.ReactionEmote reactionEmote = e.getReactionEmote();
                        PaginationPage newPage;

                        try {
                            newPage = paginations.get(messageId).getPage(reactionEmote.getName());
                        } catch (PageNotFoundException ex) {
                            try {
                                newPage = paginations.get(messageId).getPage(reactionEmote.getEmote());
                            } catch (Exception ignored) {
                                break;
                            }
                        }

                        if (newPage != null) {
                            Object newMessage = newPage.getMessage();
                            Message message = e.getChannel().retrieveMessageById(messageId).complete();

                            if (newMessage instanceof String) {
                                message.editMessage((String) newMessage).queue();
                            } else if (newMessage instanceof MessageEmbed) {
                                message.editMessage((MessageEmbed) newMessage).override(true).queue();
                            }

                            e.getReaction().removeReaction(e.getUser()).queue();
                        }
                    }
                }

                break;

            case MESSAGE_DELETE:
                paginations.remove(((GuildMessageDeleteEvent) event).getMessageId());

                break;
        }
    }
}
