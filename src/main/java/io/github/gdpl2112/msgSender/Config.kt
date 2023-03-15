package io.github.gdpl2112.msgSender

data class Config(
    var qid: Long = -1,
    var tips: String = "您收到了来自%s(%s)的一条消息",
    var tips0: String = "%s(%s)在群%s提到了您",
)
