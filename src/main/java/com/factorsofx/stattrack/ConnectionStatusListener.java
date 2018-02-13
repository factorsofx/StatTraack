package com.factorsofx.stattrack;

import com.factorsofx.stattrack.persist.MessageStatStore;
import com.google.common.collect.ClassToInstanceMap;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class ConnectionStatusListener
{
    private MessageStatStore msgStatStore;

    private static final Logger log = LoggerFactory.getLogger(ConnectionStatusListener.class);

    public ConnectionStatusListener(ClassToInstanceMap<Object> deps)
    {
        this.msgStatStore = deps.getInstance(MessageStatStore.class);
    }

    @SubscribeEvent
    public void onReconnect(ReconnectedEvent event)
    {
        log.info("Reconnected to discord, starting back-traversal");
        new MessageUpdater(msgStatStore, event.getJDA(), OffsetDateTime.now()).update();
    }
}
