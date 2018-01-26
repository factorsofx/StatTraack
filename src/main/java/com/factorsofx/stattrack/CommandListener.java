package com.factorsofx.stattrack;

import com.factorsofx.stattrack.command.*;
import com.factorsofx.stattrack.command.admin.StatisticsCommand;
import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandListener
{
    private PersistenceService persistenceService;
    private String cmdPrefix;

    private Map<String, BotCommand> commandMap;

    public CommandListener(PersistenceService persistenceService, String cmdPrefix)
    {
        this.persistenceService = persistenceService;
        this.cmdPrefix = cmdPrefix;


        commandMap = new HashMap<>();
        commandMap.put("optin", new OptIn(persistenceService));
        commandMap.put("optout", new OptOut(persistenceService));
        commandMap.put("channelhistory", new ChannelHistoryCommand(persistenceService));
        commandMap.put("userstats", new UserStatsCommand(persistenceService));
        commandMap.put("guildhistory", new FullGuildHistoryCommand(persistenceService));
        commandMap.put("userhistory", new UserHistoryCommand(persistenceService));
        commandMap.put("stats", new StatisticsCommand(persistenceService));
        commandMap.put("daystats", new DaysOfWeekStats(persistenceService));
    }

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event)
    {
        if(event.getMessage().getContentRaw().startsWith(cmdPrefix))
        {
            String[] words = event.getMessage().getContentRaw().split("\\s");
            BotCommand cmd = commandMap.get(words[0].substring(cmdPrefix.length()).toLowerCase());
            if(cmd != null)
            {
                try
                {
                    cmd.execute(event.getAuthor(), event.getTextChannel(), event.getMessage(), Arrays.copyOfRange(words, 1, words.length));
                }
                catch(Exception e)
                {
                    event.getTextChannel().sendMessage("Fuck! " + e).complete();
                    e.printStackTrace();
                }
            }
        }
    }
}
