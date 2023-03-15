package io.github.gdpl2112.msgSender;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;

/**
 * @author github.kloping
 */
public class DefaultListenerHost extends SimpleListenerHost {
    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @EventHandler
    public void onEvent(FriendMessageEvent event) {
        if (event.getSender().getId() == event.getBot().getId()) return;
        Config config = MsgSender.INSTANCE.config;
        long qid = config.getQid();
        if (qid > 0) {
            event.getBot().getFriend(qid).sendMessage(
                    String.format(config.getTips(), event.getFriend().getRemark(), event.getFriend().getId())
            );
            event.getBot().getFriend(qid).sendMessage(event.getMessage());
        }
    }

    @EventHandler
    public void onEvent(GroupMessageEvent event) {
        boolean k = false;
        for (SingleMessage singleMessage : event.getMessage()) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                if (event.getBot().getId() == at.getTarget()) {
                    k = true;
                }
            }
        }
        if (!k) return;
        Config config = MsgSender.INSTANCE.config;
        long qid = config.getQid();
        if (qid > 0) {
            event.getBot().getFriend(qid).sendMessage(
                    String.format(config.getTips0(), event.getSenderName(), event.getSender().getId(), event.getGroup().getId())
            );
            event.getBot().getFriend(qid).sendMessage(event.getMessage());
        }
    }


}