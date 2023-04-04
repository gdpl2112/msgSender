package io.github.gdpl2112.msgSender;

import io.github.kloping.date.FrameUtils;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author github.kloping
 */
public class DefaultListenerHost extends SimpleListenerHost {
    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    int te = 5;
    Friend m;


    long fid = -1;
    long gid = -1;
    /**
     * 不为null时 开启会话
     */
    Contact session;

    public static final String FORMAT0 = "已经开启%s的会话\n在接下来发送的消息将转发至此\n如需停止会话,请回复:'%s'";
    public static final String FORMAT1 = "请输入需要进入会话的ID";

    private Map<Integer, MessageSource> hm = new HashMap<>();

    private void flushedTime() {
        te = 300;
    }

    @EventHandler
    public void onEvent(MessageRecallEvent.FriendRecall event) {
        int[] ids = event.getMessageInternalIds();
        MessageSource source = hm.get(ids[0]);
        if (source != null) {
            MessageSource.recall(source);
        }
    }

    @EventHandler
    public void onEvent(FriendMessageEvent event) {
        long sid = event.getSender().getId();
        if (sid == event.getBot().getId()) return;
        Config config = MsgSender.INSTANCE.config;
        long qid = config.getQid();
        if (sid == qid) {
            m = event.getFriend();
            String text = toText(event.getMessage());
            if (text.equals(config.getEnd())) {
                session = null;
                hm.clear();
                event.getSender().sendMessage("已经结束会话");
            } else if (session == null) {
                //如果不在会话
                if (text.isEmpty()) return;
                //判断是否为开启会话命令
                if (text.startsWith(config.getStart0()) || text.equals(config.getStart0())) {
                    String e0 = text.replace(config.getStart0(), "").trim();
                    Long q0 = -1L;
                    if (e0.isEmpty()) {
                        if (fid <= 0) event.getSender().sendMessage(FORMAT1);
                        else q0 = fid;
                    } else {
                        try {
                            q0 = Long.parseLong(e0);
                        } catch (Exception e) {
                            event.getSender().sendMessage(e.getMessage());
                        }
                    }
                    if (q0 > 0) {
                        session = event.getBot().getFriend(q0);
                        event.getSender().sendMessage(String.format(FORMAT0, q0, config.getEnd()));
                        flushedTime();
                    }
                }
                //判断是否为开启会话命令
                else if (text.startsWith(config.getStart1()) || text.equals(config.getStart1())) {
                    Long q0 = -1L;
                    String e0 = text.replace(config.getStart1(), "").trim();
                    if (e0.isEmpty()) {
                        if (gid <= 0) event.getSender().sendMessage(FORMAT1);
                        else q0 = gid;
                    } else {
                        try {
                            q0 = Long.parseLong(e0);
                        } catch (Exception e) {
                            event.getSender().sendMessage(e.getMessage());
                        }
                    }
                    if (q0 > 0) {
                        session = event.getBot().getGroup(q0);
                        event.getSender().sendMessage(String.format(FORMAT0, q0, config.getEnd()));
                        flushedTime();
                    }
                }
            } else {
                flushedTime();
                //在会话直接转发
                MessageChain message = event.getMessage();
                MessageSource source = (MessageSource) message.get(0);
                MessageReceipt mr = session.sendMessage(message);
                hm.put(source.getInternalIds()[0], mr.getSource());
            }
            return;
        } else if (qid > 0) {
            fid = sid;
            if (session == null || session.getId() != sid) {
                event.getBot().getFriend(qid).sendMessage(String.format(config.getTips(), event.getFriend().getRemark(), sid));
            }
            event.getBot().getFriend(qid).sendMessage(event.getMessage());
            flushedTime();
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
        if (session == null || session.getId() != event.getGroup().getId()) {
            if (!hasAt(event.getMessage(), event.getBot().getId())) return;
            gid = event.getGroup().getId();
            event.getBot().getFriend(qid).sendMessage(String.format(config.getTips0(), event.getSenderName(), event.getSender().getId(), event.getGroup().getId()));
            event.getBot().getFriend(qid).sendMessage(event.getMessage());
        } else if (session.getId() == event.getGroup().getId()) {
            MessageChainBuilder builder = new MessageChainBuilder();
            builder.append(event.getMessage()).append("\n").append(event.getSender().getNameCard()).append("(" + event.getSender().getId() + ")");
            event.getBot().getFriend(qid).sendMessage(builder.build());
            flushedTime();
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

    {
        FrameUtils.SERVICE.scheduleWithFixedDelay(() -> {
                    te--;
                    if (te <= 0) {
                        if (session != null) {
                            session = null;
                            m.sendMessage("已经自动结束会话");
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS
        );
    }

}
