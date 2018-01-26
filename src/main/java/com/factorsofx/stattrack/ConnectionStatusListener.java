package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.PersistenceService;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class ConnectionStatusListener
{
    private PersistenceService persistenceService;

    private static final Logger log = LoggerFactory.getLogger(ConnectionStatusListener.class);

    public ConnectionStatusListener(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    @SubscribeEvent
    public void onReconnect(ReconnectedEvent event)
    {
        log.info("Reconnected to discord, starting back-traversal");
        new MessageUpdater(persistenceService, event.getJDA(), OffsetDateTime.now()).update();
    }
}
