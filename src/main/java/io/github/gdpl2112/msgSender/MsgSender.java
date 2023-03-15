package io.github.gdpl2112.msgSender;

import io.github.kloping.initialize.FileInitializeValue;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

/**
 * @author github-kloping
 * @date 2023-03-14
 */
public final class MsgSender extends JavaPlugin {
    public static final MsgSender INSTANCE = new MsgSender();

    private MsgSender() {
        super(new JvmPluginDescriptionBuilder("io.github.kloping.MsgSender", "1.2").name("MsgSender").info("消息转发").author("kloping").build());
    }

    public Config config = new Config();

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded!");
        config = FileInitializeValue.getValue("./conf/msgSender/config.json", config, true);
        CommandManager.INSTANCE.registerCommand(CommandLine.INSTANCE, true);
        GlobalEventChannel.INSTANCE.registerListenerHost(new DefaultListenerHost());
    }
}