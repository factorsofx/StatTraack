package com.factorsofx.stattrack;

import com.factorsofx.stattrack.command.*;
import com.factorsofx.stattrack.command.admin.StatisticsCommand;
import com.factorsofx.stattrack.persist.PersistenceService;
import com.factorsofx.stattrack.security.Permission;
import com.factorsofx.stattrack.security.UserProfile;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandListener
{
    private PersistenceService persistenceService;
    private String cmdPrefix;

    private Map<String, Tuple2<BotCommand, RegisterCommand>> commandMap;

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    public CommandListener(PersistenceService persistenceService, String cmdPrefix)
    {
        this.persistenceService = persistenceService;
        this.cmdPrefix = cmdPrefix;

        commandMap = new HashMap<>();

        Reflections reflections = new Reflections("com.factorsofx.stattrack.command");
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(RegisterCommand.class);

        ClassToInstanceMap<Object> dependencies = ImmutableClassToInstanceMap.builder()
                .put(PersistenceService.class, persistenceService)
                .build();

        for(Class<?> annotatedClass : commandClasses)
        {
            Class<? extends BotCommand> commandClass = annotatedClass.asSubclass(BotCommand.class);
            Constructor<?>[] constructors = commandClass.getConstructors();
            if(constructors.length < 1 || constructors.length > 1)
            {
                throw new RuntimeException("Wrong amount of public constructors on @RegisterCommand annotated class");
            }

            Constructor<?> toUse = constructors[0];

            Class<?>[] paramClasses = toUse.getParameterTypes();
            Object[] params = new Object[paramClasses.length];
            for(int i = 0; i < paramClasses.length; i++)
            {
                params[i] = dependencies.get(paramClasses[i]);
            }

            try
            {
                Object instantiated = toUse.newInstance(params);
                BotCommand cmd = BotCommand.class.cast(instantiated);
                RegisterCommand registerAnnotation = annotatedClass.getAnnotation(RegisterCommand.class);
                Tuple2<BotCommand, RegisterCommand> cmdTuple = Tuple.tuple(cmd, registerAnnotation);
                for(String alias : registerAnnotation.value())
                {
                    commandMap.put(alias.toLowerCase(), cmdTuple);
                }

            }
            catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event)
    {
        if(event.getMessage().getContentRaw().startsWith(cmdPrefix))
        {
            String[] words = event.getMessage().getContentRaw().split("\\s+");
            Tuple2<BotCommand, RegisterCommand> cmd = commandMap.get(words[0].substring(cmdPrefix.length()).toLowerCase());

            if(cmd != null)
            {
                UserProfile profile = persistenceService.getUserProfile(event.getAuthor(), event.getGuild());

                if(profile.getPerms().contains(Permission.BANNED))
                {
                    log.info("Banned user " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " attempted to use command " + event.getMessage().getContentRaw());
                    return; // lolnope
                }

                for(Permission perm : cmd.v2.permissions())
                {
                    if(!profile.getPerms().contains(perm))
                    {
                        log.info("User " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " tried to use command" + event.getMessage().getContentRaw() + " but was missing permission " + perm);
                        event.getChannel().sendMessage("Missing permission: " + perm).complete();
                        return;
                    }
                }

                try
                {
                    cmd.v1.execute(event.getAuthor(), event.getTextChannel(), event.getMessage(), Arrays.copyOfRange(words, 1, words.length));
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
