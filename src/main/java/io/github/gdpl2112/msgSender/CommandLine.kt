package io.github.gdpl2112.msgSender

import io.github.kloping.initialize.FileInitializeValue
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.java.JCompositeCommand

class CommandLine private constructor() : JCompositeCommand(MsgSender.INSTANCE, "msgs") {
    companion object {
        @JvmField
        val INSTANCE = CommandLine()
    }

    init {
        description = "MsgSender 命令"
    }


    @Description("重新加载配置")
    @SubCommand("reload")
    suspend fun CommandSender.msReload() {
        MsgSender.INSTANCE.config =
            FileInitializeValue.getValue("./conf/msgSender/config.json", MsgSender.INSTANCE.config, true)
        sendMessage(" Reloading the complete ")
    }

}