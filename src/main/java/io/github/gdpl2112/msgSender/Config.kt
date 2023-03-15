package io.github.gdpl2112.msgSender

data class Config(
    var qid: Long = -1,
    var tips: String = "您收到了来自%s(%s)的一条消息",
    var tips0: String = "%s(%s)在群%s提到了您",
    var start0: String = "开启会话",
    var start1: String = "开启群会话",
    var end: String = "结束会话",
)
