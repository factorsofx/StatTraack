package com.factorsofx.stattrack.command.admin;

import com.factorsofx.stattrack.MessageUtils;
import com.factorsofx.stattrack.command.BotCommand;
import com.factorsofx.stattrack.command.RegisterCommand;
import com.factorsofx.stattrack.security.Permission;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptException;
import java.awt.*;
import java.io.InputStreamReader;

@RegisterCommand(value = {"eval", "jjs"}, permissions = {Permission.EXEC}, optExclusive = false)
public class EvalCommand implements BotCommand
{
    private NashornScriptEngine engine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine("--language=es6");

    private StringBuilderWriter writer = new StringBuilderWriter();

    public EvalCommand()
    {
        engine.getContext().setWriter(writer);

        NullInputStream input = new NullInputStream(0);
        engine.getContext().setReader(new InputStreamReader(input));
    }

    @Override
    public void execute(User user, TextChannel channel, Message message, String[] args)
    {
        engine.put("__jda", channel.getJDA());

        engine.put("__user", user);
        engine.put("__channel", channel);
        engine.put("__msg", message);

        String joined = StringUtils.join(args, " ");
        try
        {
            Object result = engine.eval(joined);
            if(result != null)
            {
                if(result instanceof ScriptObjectMirror || result instanceof String)
                {
                    channel.sendMessage("```" + result.toString() + "```").complete();
                }
                else if(result instanceof Number)
                {
                    channel.sendMessage("```" + ((Number)result).doubleValue() + "```").complete();
                }
                else
                {
                    channel.sendMessage(MessageUtils.getObjectEmbed(result, new Color(34, 153, 84))).complete();
                }
            }

            String content = writer.getBuilder().toString();
            writer.getBuilder().setLength(0);

            if(!content.isEmpty())
            {
                channel.sendMessage("```" + content + "```").complete();
            }
        }
        catch(ScriptException e)
        {
            channel.sendMessage(MessageUtils.getExceptionEmbed(e)).complete();
        }
    }
}
