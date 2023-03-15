package io.github.gdpl2112.msgSender;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author github.kloping
 */
public class DefaultListenerHost extends SimpleListenerHost {
    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    long fid = -1;
    long gid = -1;
    Contact session;

    public static final String FORMAT0 = "已经开启%s的会话\n在接下来发送的消息将转发至此\n如需停止会话,请回复:'%s'";
    public static final String FORMAT1 = "请输入需要进入会话的ID";

    @EventHandler
    public void onEvent(FriendMessageEvent event) {
        long sid = event.getSender().getId();
        if (sid == event.getBot().getId()) return;
        Config config = MsgSender.INSTANCE.config;
        long qid = config.getQid();
        if (sid == qid) {
            String text = toText(event.getMessage());
            if (text.equals(config.getEnd())) {
                session = null;
                event.getSender().sendMessage("已经结束会话");
            } else if (session == null) {
                if (text.isEmpty()) return;
                if (text.startsWith(config.getStart0()) || text.equals(config.getStart0())) {
                    String e0 = text.replace(config.getStart0(), "").trim();
                    if (e0.isEmpty()) {
                        if (fid <= 0) {
                            session = event.getBot().getFriend(fid);
                            event.getSender().sendMessage(String.format(FORMAT0, fid, config.getEnd()));
                        } else {
                            event.getSender().sendMessage(FORMAT1);
                        }
                    } else {
                        try {
                            Long q0 = Long.parseLong(e0);
                            session = event.getBot().getFriend(q0);
                            event.getSender().sendMessage(String.format(FORMAT0, q0, config.getEnd()));
                        } catch (Exception e) {
                            event.getSender().sendMessage(e.getMessage());
                        }
                    }
                } else if (text.startsWith(config.getStart1()) || text.equals(config.getStart1())) {
                    String e0 = text.replace(config.getStart1(), "").trim();
                    if (e0.isEmpty()) {
                        if (gid <= 0) {
                            event.getSender().sendMessage(FORMAT1);
                        } else {
                            session = event.getBot().getGroup(gid);
                            event.getSender().sendMessage(String.format(FORMAT0, gid, config.getEnd()));
                        }
                    } else {
                        try {
                            Long q0 = Long.parseLong(e0);
                            session = event.getBot().getGroup(q0);
                            event.getSender().sendMessage(String.format(FORMAT0, q0, config.getEnd()));
                        } catch (Exception e) {
                            event.getSender().sendMessage(e.getMessage());
                        }
                    }
                }
            } else {
                session.sendMessage(event.getMessage());
            }
            return;
        } else if (qid > 0) {
            fid = sid;
            if (session == null || session.getId() != sid) {
                event.getBot().getFriend(qid).sendMessage(String.format(config.getTips(), event.getFriend().getRemark(), sid));
            }
            event.getBot().getFriend(qid).sendMessage(event.getMessage());
        }
    }

    @EventHandler
    public void onEvent(StrangerMessageEvent event) {
        method9(event.getSender().getId(), event.getBot(), event.getSender().getRemark(), event.getMessage(), event);
    }

    private void method9(long id, Bot bot, String remark, MessageChain message, MessageEvent event) {
        if (id == bot.getId()) return;
        Config config = MsgSender.INSTANCE.config;
        long qid = config.getQid();
        if (qid > 0) {
            bot.getFriend(qid).sendMessage(String.format(config.getTips(), remark, id));
            bot.getFriend(qid).sendMessage(message);
        }
    }

    @EventHandler
    public void onEvent(GroupTempMessageEvent event) {
        method9(event.getSender().getId(), event.getBot(), event.getSender().getRemark(), event.getMessage(), event);
    }

    @EventHandler
    public void onEvent(GroupMessageEvent event) {
        Config config = MsgSender.INSTANCE.config;
        long qid = config.getQid();
        if (qid < 0) return;
        if (session == null) {
            if (!hasAt(event.getMessage(), event.getBot().getId())) return;
            gid = event.getGroup().getId();
            event.getBot().getFriend(qid).sendMessage(String.format(config.getTips0(), event.getSenderName(), event.getSender().getId(), event.getGroup().getId()));
            event.getBot().getFriend(qid).sendMessage(event.getMessage());
        } else if (session.getId() == event.getGroup().getId()) {
            MessageChainBuilder builder = new MessageChainBuilder();
            builder.append(event.getMessage()).append("\n")
                    .append(event.getSender().getNameCard())
                    .append("(" + event.getSender().getId() + ")");
            event.getBot().getFriend(qid).sendMessage(builder.build());
        }
    }

    public boolean hasAt(MessageChain chain, long qid) {
        for (SingleMessage singleMessage : chain) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                if (qid == at.getTarget()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toText(MessageChain chain) {
        var text = "";
        for (SingleMessage singleMessage : chain) {
            if (singleMessage instanceof PlainText) {
                text = text + ((PlainText) singleMessage).getContent().trim();
            } else if (singleMessage instanceof At) {
                text = text + ((At) singleMessage).getTarget();
            }
        }
        return text.trim();
    }
}
