package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.time.OffsetDateTime;

public class StatTrackListener
{
    private PersistenceService persistenceService;
    private OffsetDateTime startupTime;

    public StatTrackListener(PersistenceService persistenceService, OffsetDateTime startTime)
    {
        this.persistenceService = persistenceService;
        this.startupTime = startTime;
    }

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event)
    {
        if(event.getMessage().getCreationTime().isAfter(startupTime))
        {
            persistenceService.persistMessage(event.getMessage());
        }
    }
}
