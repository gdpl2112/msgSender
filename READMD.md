## 消息转发插件

具体功能: bot收到私聊或群at时转发至指定qid的工具

配置文件: ./conf/msgSender/config.json
```json
{
  "qid": -1,
  "tips": "您收到了来自%s(%s)的一条消息",
  "tips0": "%s(%s)在群%s提到了您"
}
```

更改配置后使用命令:
    
    /msgs reload

即可立刻生效