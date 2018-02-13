package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.MessageStatStore;
import com.factorsofx.stattrack.stat.MessageStat;
import com.google.common.collect.ClassToInstanceMap;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.time.OffsetDateTime;

public class StatTrackListener
{
    private MessageStatStore messageStatStore;
    private OffsetDateTime startupTime;

    public StatTrackListener(ClassToInstanceMap<Object> dependencies, OffsetDateTime startTime)
    {
        this.messageStatStore = dependencies.getInstance(MessageStatStore.class);
        this.startupTime = startTime;
    }

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event)
    {
        if(event.getMessage().getCreationTime().isAfter(startupTime))
        {
            messageStatStore.store(MessageStat.fromMessage(event.getMessage()));
        }
    }
}
