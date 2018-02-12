package com.factorsofx.stattrack;

import com.google.common.base.Predicates;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

public class MessageUtils
{
    public static void doIfConfirmed(String confirmation, TextChannel channel, User user, Runnable action)
    {
        Message confirmMsg = channel.sendMessage(new EmbedBuilder()
                .setTitle("Confirmation")
                .setColor(Color.ORANGE)
                .setDescription(confirmation)
                .build()).complete();
        confirmMsg.addReaction(EmojiManager.getForAlias("white_check_mark").getUnicode()).complete();
        confirmMsg.addReaction(EmojiManager.getForAlias("negative_squared_cross_mark").getUnicode()).complete();

        channel.getJDA().addEventListener(new ConfirmationReactListener(confirmMsg, user, channel, action));
    }

    private static class ConfirmationReactListener
    {
        private long watching;
        private User confirmer;
        private TextChannel channel;
        private Runnable toDo;
        private Instant created;

        public ConfirmationReactListener(Message watching, User confirmer, TextChannel channel, Runnable toDo)
        {
            this.watching = watching.getIdLong();
            this.confirmer = confirmer;
            this.channel = channel;
            this.toDo = toDo;
            this.created = Instant.now();
        }

        @SubscribeEvent
        public void onReact(GuildMessageReactionAddEvent evt)
        {
            if(created.plus(2, MINUTES).isBefore(Instant.now()))
            {
                evt.getJDA().removeEventListener(this);
                return;
            }

            if(evt.getUser() == confirmer && evt.getMessageIdLong() == watching)
            {
                if(evt.getReaction().getReactionEmote().getName().equalsIgnoreCase(EmojiManager.getForAlias("white_check_mark").getUnicode()))
                {
                    evt.getJDA().removeEventListener(this);
                    evt.getChannel().deleteMessageById(watching).reason("user answered").complete();

                    try
                    {
                        toDo.run();
                    }
                    catch(Exception e)
                    {
                        evt.getChannel().sendMessage(getExceptionEmbed(e)).complete();
                        e.printStackTrace();
                    }
                }
                else if(evt.getReaction().getReactionEmote().getName().equalsIgnoreCase(EmojiManager.getForAlias("negative_squared_cross_mark").getUnicode()))
                {
                    evt.getJDA().removeEventListener(this);
                    evt.getChannel().deleteMessageById(watching).reason("user answered").complete();
                }
            }
        }

        @Override
        public String toString()
        {
            return "ConfirmationReactListener{" +
                    "watching=" + watching +
                    ", confirmer=" + confirmer +
                    ", channel=" + channel +
                    ", toDo=" + toDo +
                    '}';
        }
    }

    public static MessageEmbed getExceptionEmbed(Throwable t)
    {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(t.getClass().getTypeName())
                .setDescription(t.getMessage())
                .setTimestamp(Instant.now())
                .build();
    }

    public static <T> MessageEmbed getObjectEmbed(T obj, Color color)
    {
        if(obj == null)
        {
            return new EmbedBuilder()
                    .setTitle("null")
                    .setColor(Color.RED)
                    .setDescription("null")
                    .setTimestamp(Instant.now())
                    .build();
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(color);
        builder.setTitle(obj.getClass().getTypeName());
        builder.setDescription(obj.toString());
        builder.setTimestamp(Instant.now());

        for(int i = 0; i < obj.getClass().getDeclaredFields().length && i < 20; i++)
        {
            try
            {
                Field field = obj.getClass().getDeclaredFields()[i];
                field.setAccessible(true);
                builder.addField(field.getName(), Objects.toString(field.get(obj)), true);
                field.setAccessible(true);
            }
            catch(IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }

        return builder.build();
    }

    public static MessageEmbed getErrorEmbed(String errMsg)
    {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .setTitle("Error!")
                .setDescription(errMsg)
                .build();
    }

    public static List<String> getStringParsedArgs(String[] args)
    {
        // String parsing
        String argsStr = StringUtils.join(args, " ");

        Pattern patt = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
        Matcher matcher = patt.matcher(argsStr);
        List<String> argsList = new ArrayList<>();
        while(matcher.find())
        {
            String match = matcher.group(1);
            if(match.startsWith("\""))
            {
                match = match.substring(1, match.length() - 1);
            }
            argsList.add(match);
        }

        return argsList;
    }

    public static OffsetDateTime getDateFromTime(String timeString)
    {
        Parser dateParser = new Parser();
        List<DateGroup> beforeDates = dateParser.parse(timeString);
        if(beforeDates.size() != 1 || beforeDates.get(0).getDates().size() != 1)
        {
            return null;
        }

        return OffsetDateTime.ofInstant(beforeDates.get(0).getDates().get(0).toInstant(), ZoneOffset.of("Z"));
    }

    public static long getIdFromUserMention(String mention)
    {
        Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
        Matcher matcher = mentionPattern.matcher(mention);
        if(matcher.find())
        {
            return Long.parseLong(matcher.group(1));
        }
        else
        {
            throw new IllegalArgumentException("No mention found in text");
        }
    }

    public static long getIdFromMentionOrName(String mentionOrName, JDA jda)
    {
        try
        {
            return getIdFromUserMention(mentionOrName);
        }
        catch(IllegalArgumentException e)
        {
            List<User> users = jda.getUsersByName(mentionOrName, true);
            if(users.size() != 1) throw new IllegalArgumentException("Zero or more than one users found with given name");
            return users.get(0).getIdLong();
        }
    }
}
