package top.tbpdt.vanilla

import kotlinx.coroutines.delay
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import top.tbpdt.utils.MessageUtils.getPlainText

/**
 * @author Takeoff0518
 */
object EatDots : SimpleListenerHost() {
    private val indexList: List<String> =
        listOf("一", "两", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二", "十三", "十四", "十五")

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.onMessage() {
        val plainText = message.getPlainText().trim()
        val dotCount = if (plainText.isNotEmpty() && plainText.all { it == '。' }) plainText.length else -1
        if (dotCount == -1) return
        if (dotCount > 15) {
            group.sendMessage(message.quote() + "好多小句号诶……吃掉吃掉吃掉……")
            delay(1000)
            group.sendMessage("吃不了了~>_<~")
            return
        }
        val result = indexList[dotCount - 1] + "个小句号诶……" + "吃掉".repeat(dotCount) + "~"
        group.sendMessage(message.quote() + result)
    }
}