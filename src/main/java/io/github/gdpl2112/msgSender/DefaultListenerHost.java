package io.github.gdpl2112.msgSender;

import io.github.kloping.date.FrameUtils;
import io.github.kloping.judge.Judge;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
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

    /**
     * 计时
     */
    int te = 5;
    //最近的一个好友传唤
    Contact m;

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
    private Map<String, Contact> tid2contact = new HashMap<>();

    @EventHandler
    public void onEvent(FriendMessageEvent event) {
        long sid = event.getSender().getId();
        if (sid == event.getBot().getId()) return;
        Config config = MsgSender.INSTANCE.config;
        if (Judge.isNotEmpty(config.getTid())) {
            Contact contact = isContact(event.getBot(), event.getSubject());
            if (contact != null) {
                String text = toText(event.getMessage());
                //如果不在会话
                if (text.isEmpty()) return;
                else if (text.equals(config.getEnd())) {
                    session = null;
                    hm.clear();
                    contact.sendMessage("已经结束会话");
                } else if (session == null) {
                    //判断是否为开启会话命令
                    commandLine0(config, contact, text, event.getBot(), event);
                } else {
                    //否则会话不是null直接转发到会话
                    flushedTime();
                    //在会话直接转发
                    MessageChain message = event.getMessage();
                    MessageSource source = (MessageSource) message.get(0);
                    MessageReceipt mr = session.sendMessage(message);
                    hm.put(source.getInternalIds()[0], mr.getSource());
                    m = event.getFriend();
                }
            } else {
                if (ManagerConf.INSTANCE.getStateByIdDefault(event.getSubject().getId(),config.getK())){
                    contact = isContact(event.getBot(), null);
                    fid = sid;
                    if (session == null || session.getId() != sid) {
                        contact.sendMessage(String.format(config.getTips(), getNickOrRemark(event.getFriend()), sid));
                    }
                    contact.sendMessage(event.getMessage());
                    flushedTime();
                }
            }
        }
    }

    private String getNickOrRemark(Friend friend) {
        return friend.getRemark().isEmpty() ? friend.getNick() : friend.getRemark();
    }

    @EventHandler
    public void onEvent(GroupTempMessageEvent event) {
        Contact contact = isContact(event.getBot(), null);
        if (contact == null) return;
        Config config = MsgSender.INSTANCE.config;
        contact.sendMessage(String.format(config.getTips(), event.getGroup().getName(), contact.getId()));
        contact.sendMessage(event.getMessage());
    }

    @EventHandler
    public void onEvent(GroupMessageEvent event) {
        Config config = MsgSender.INSTANCE.config;
        if (Judge.isNotEmpty(config.getTid())) {
            Contact contact = isContact(event.getBot(), event.getSubject());
            if (contact != null) {
                String text = toText(event.getMessage());
                //如果不在会话
                if (text.isEmpty()) return;
                else if (text.equals(config.getEnd())) {
                    session = null;
                    hm.clear();
                    contact.sendMessage("已经结束会话");
                } else if (session == null) {
                    //判断是否为开启会话命令
                    commandLine0(config, contact, text, event.getBot(), event);
                } else {
                    //否则会话不是null直接转发到会话
                    flushedTime();
                    //在会话直接转发
                    MessageChain message = event.getMessage();
                    MessageSource source = (MessageSource) message.get(0);
                    MessageReceipt mr = session.sendMessage(message);
                    hm.put(source.getInternalIds()[0], mr.getSource());
                    m = event.getSubject();
                }
            } else {
                contact = isContact(event.getBot(), null);
                if (session == null || session.getId() != event.getGroup().getId()) {
                    if (!hasAt(event.getMessage(), event.getBot().getId())) return;
                    gid = event.getGroup().getId();
                    contact.sendMessage(String.format(config.getTips0(), event.getSenderName(), event.getSender().getId(), event.getGroup().getId()));
                    contact.sendMessage(event.getMessage());
                } else if (session.getId() == event.getGroup().getId()) {
                    MessageChainBuilder builder = new MessageChainBuilder();
                    builder.append(event.getMessage())
                            .append("\n").append(event.getSender().getNameCard())
                            .append("(" + event.getSender().getId() + ")");
                    contact.sendMessage(builder.build());
                    flushedTime();
                }
            }
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

    private void commandLine0(Config config, Contact contact, String text, Bot bot, MessageEvent event) {
        if (text.startsWith(config.getStart0())) {
            String e0 = text.replace(config.getStart0(), "").trim();
            Long q0 = -1L;
            if (e0.isEmpty()) {
                if (fid <= 0) contact.sendMessage(FORMAT1);
                else q0 = fid;
            } else {
                try {
                    q0 = Long.parseLong(e0);
                } catch (Exception e) {
                    contact.sendMessage(e.getMessage());
                }
            }
            if (q0 > 0) {
                session = bot.getFriend(q0);
                contact.sendMessage(String.format(FORMAT0, q0, config.getEnd()));
                flushedTime();
            }
            //判断是否为开启会话命令
        } else if (text.startsWith(config.getStart1())) {
            Long q0 = -1L;
            String e0 = text.replace(config.getStart1(), "").trim();
            if (e0.isEmpty()) {
                if (gid <= 0) contact.sendMessage(FORMAT1);
                else q0 = gid;
            } else {
                try {
                    q0 = Long.parseLong(e0);
                } catch (Exception e) {
                    contact.sendMessage(e.getMessage());
                }
            }
            if (q0 > 0) {
                session = bot.getGroup(q0);
                contact.sendMessage(String.format(FORMAT0, q0, config.getEnd()));
                flushedTime();
            }
        }
    }

    /**
     * 判断是否
     *
     * @return
     */
    private synchronized Contact isContact(Bot bot, Contact contact) {
        Contact c0 = null;
        String tid = MsgSender.INSTANCE.config.getTid();
        if (tid2contact.containsKey(tid)){
            c0 = tid2contact.get(tid);
        }else {
            if (tid.startsWith("f")) {
                Long id = Long.valueOf(tid.substring(1));
                Friend friend = bot.getFriend(id);
                if (friend != null) {
                    c0 = friend;
                    tid2contact.put(tid, friend);
                } else return null;
            } else if (tid.startsWith("g")) {
                Long id = Long.valueOf(tid.substring(1));
                Group group = bot.getGroup(id);
                if (group != null) {
                    c0 = group;
                    tid2contact.put(tid, group);
                } else return null;
            } else {
                Long id = Long.valueOf(tid);
                Friend friend = bot.getFriend(id);
                if (friend != null) {
                    c0 = friend;
                    tid2contact.put(tid, friend);
                } else return null;
            }
        }
        if (contact == null) return c0;
        if (c0.getId() == contact.getId()) return contact;
        return null;
    }

}
