package top.tbpdt

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.emptyMessageChain
import top.tbpdt.configer.GlobalConfig
import top.tbpdt.utils.CaveUtils
import top.tbpdt.utils.CaveUtils.loadComments
import top.tbpdt.utils.MessageUtils.getPlainText
import java.text.SimpleDateFormat

/**
 * @author Takeoff0518
 */
object Cave : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.HIGH)
    suspend fun GroupMessageEvent.handle() {
        if (message.getPlainText().startsWith("${GlobalConfig.commandPrefix}.ca")) {
            val text = message.serializeToMiraiCode().removePrefix(".ca").trim()

            val id = CaveUtils.getCommentCount() + 1
            CaveUtils.saveComment(id, text, sender.id, sender.nameCard)
            group.sendMessage("回声洞 #${id} 添加成功~")
        }
        if (message.getPlainText().startsWith("${GlobalConfig.commandPrefix}.cq")) {
            val randomId = (1..CaveUtils.getCommentCount()).random()
            val comment = loadComments(randomId)
            for (i in comment) {
                /*
                    回声洞 #233(9098a19)

                    逸一时误一世。

                    --洛雨辰~(1145141919)
                    at 23/01/16 9:15:20
                 */
                var result = emptyMessageChain()
                result += PlainText("回声洞 #${i.caveId}(${i.sha256})\n\n")
                result += i.text.deserializeMiraiCode()
                result += PlainText("\n\n--${i.senderNick}(${i.senderId})\nat ")
                result += PlainText(SimpleDateFormat("yy/MM/dd HH:mm:ss").format(i.date))
                group.sendMessage(result)
            }
        }
    }
}