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


    val PATH = "./conf/msgSender/config.json";

    @Description("重新加载配置")
    @SubCommand("reload")
    suspend fun CommandSender.msReload() {
        MsgSender.INSTANCE.config =
            FileInitializeValue.getValue(PATH, MsgSender.INSTANCE.config, true)
        sendMessage(" Reloading the complete ")
    }

    @Description("设置全部开关")
    @SubCommand("reverse")
    suspend fun CommandSender.reverse() {
        MsgSender.INSTANCE.config.k = !MsgSender.INSTANCE.config.k
        FileInitializeValue.putValues(PATH,MsgSender.INSTANCE.config)
        sendMessage("now k:"+MsgSender.INSTANCE.config.k)
    }

    @Description("设置开")
    @SubCommand("open")
    suspend fun CommandSender.open(@Name("id") id:Long) {
        ManagerConf.INSTANCE.setStateById(id,true)
        sendMessage("opened id "+id)
    }

    @Description("设置关")
    @SubCommand("close")
    suspend fun CommandSender.close(@Name("id") id:Long) {
        ManagerConf.INSTANCE.setStateById(id,false)
        sendMessage("closed id "+id)
    }

}