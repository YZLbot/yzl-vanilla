package top.tbpdt

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.emptyMessageChain
import top.tbpdt.utils.CaveUtils
import top.tbpdt.utils.CaveUtils.loadComments
import top.tbpdt.utils.CaveUtils.updatePickCount
import top.tbpdt.utils.MessageUtils.getRemovedPrefixCommand
import top.tbpdt.utils.MessageUtils.isCommand
import java.text.SimpleDateFormat

/**
 * @author Takeoff0518
 */
object Cave : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.HIGH)
    suspend fun GroupMessageEvent.handle() {
        if (message.isCommand("ca")) {
            val text = message.getRemovedPrefixCommand("ca")
            if (text.isEmpty()) {
                group.sendMessage("不能添加空信息！")
                return
            }
            val id = CaveUtils.getCommentCount() + 1
            CaveUtils.saveComment(id, text, sender.id, sender.nick, group.id, group.name)
            group.sendMessage("回声洞 #${id} 添加成功~")
        }
        if (message.isCommand("cq") || message.isCommand(".捡")) {
            val randomId = (1..CaveUtils.getCommentCount()).random()
            val comment = loadComments(randomId)
            for (i in comment) {
                /*
                    回声洞 #233

                    逸一时误一世。

                    --洛雨辰~(1145141919)
                    at 23/01/16 9:15:20
                 */
                var result = emptyMessageChain()
                result += PlainText("回声洞 #${i.caveId}\n\n")
                result += i.text.deserializeMiraiCode()
                result += PlainText("\n\n--${i.senderNick}(${i.senderId})\nat ")
                result += PlainText(SimpleDateFormat("yy/MM/dd HH:mm:ss").format(i.date))
                group.sendMessage(result)
                updatePickCount(randomId)
            }
        }
        if (message.isCommand("ci")) {
            val id: Int
            try {
                id = message.getRemovedPrefixCommand("ci").toInt()
            } catch (e: NumberFormatException) {
                group.sendMessage("解析失败……参数是不是没有填数字或者是填的不是数字？")
                return
            }
            if (id !in 1..CaveUtils.getCommentCount()) {
                group.sendMessage("你所查询的回声洞不在范围里呢，现在共有${CaveUtils.getCommentCount()}条回声洞~")
                return
            }
            val comment = loadComments(id)
            for (i in comment) {
                var result = emptyMessageChain()
                result += PlainText("回声洞 #${i.caveId}\n\n")
                result += i.text.deserializeMiraiCode()
                result += PlainText("\n\n--${i.senderNick}(${i.senderId})\n")
                result += PlainText("from ${i.groupNick}(${i.groupId})\n")
                result += PlainText("已被捡起 ${i.pickCount} 次\n")
                result += PlainText("at " + SimpleDateFormat("yy/MM/dd HH:mm:ss").format(i.date))
                group.sendMessage(result)
                updatePickCount(id)
            }
        }
        if (message.isCommand("cf")) {
            val target = message.getRemovedPrefixCommand("cf")
            if (target.isEmpty()) {
                group.sendMessage("查询条件不能为空！")
                return
            }
            group.sendMessage("查询中，请稍后……")
            val comment = loadComments(target).sortedBy { it.caveId }
            group.sendMessage("共计：${comment.size}")
            val forwardResult = ForwardMessageBuilder(group)
            for (i in comment) {
                var result = emptyMessageChain()
                result += PlainText("回声洞 #${i.caveId}\n\n")
                result += i.text.deserializeMiraiCode()
                result += PlainText("\n\n--${i.senderNick}(${i.senderId})\n")
                result += PlainText("from ${i.groupNick}(${i.groupId})\n")
                result += PlainText("已被捡起 ${i.pickCount} 次\n")
                result += PlainText("at " + SimpleDateFormat("yy/MM/dd HH:mm:ss").format(i.date))
                updatePickCount(i.caveId)
                forwardResult.add(bot.id, "#" + i.caveId, result)
            }
            group.sendMessage(forwardResult.build())
        }
    }
}