package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.emptyMessageChain
import top.tbpdt.configer.EmojiConfig
import top.tbpdt.utils.MessageUtils.isCommand

/**
 * @author Takeoff0518
 */
object EmojiFetch : SimpleListenerHost() {

    private val onWaiting = mutableSetOf<Long>()

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun GroupMessageEvent.commandHandle() {
        if (!EmojiConfig.enable || !message.isCommand("getimg")) return
        onWaiting.add(sender.id)
        group.sendMessage("请输入你要获取的表情")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun GroupMessageEvent.handle() {
        if (sender.id !in onWaiting) return
        if (EmojiConfig.enable && message.isCommand("getimg")) return
        var result = emptyMessageChain() + PlainText("获取到的表情如下：\n")
        var imageCnt = 0
        for (i in message.filterIsInstance<Image>()) {
            result += i
            result += PlainText("URL: ${i.queryUrl()}\n--------")
            imageCnt++
        }
        if (imageCnt == 0) {
            group.sendMessage("未能获取到表情！")
        } else {
            result += PlainText("\n共计：$imageCnt\n")
            group.sendMessage(result)
        }
        onWaiting.remove(sender.id)
    }

    /*
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun GroupMessageEvent.handleQuote() {
        if (!EmojiConfig.enable || !message.isCommand("getimg")) return
        var result = emptyMessageChain() + PlainText("获取到的表情如下：\n")
        var imageCnt = 0
        for (i in source.originalMessage.filterIsInstance<Image>()) {
            result += i
            result += PlainText("URL: ${i.queryUrl()}\n--------")
            imageCnt++
        }
        if (imageCnt == 0) {
            group.sendMessage("未能获取到表情！")
        } else {
            result += PlainText("\n共计：$imageCnt\n")
            group.sendMessage(result)
        }
    }
     */
}