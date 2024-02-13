import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import top.tbpdt.configer.MuteMeConfig
import top.tbpdt.utils.MessageUtils.checkMutePermission
import top.tbpdt.utils.MessageUtils.getPlainText

/**
 * @author Takeoff0518
 */
object MuteMe : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun GroupMessageEvent.handler() {
        if (message.getPlainText().contains("我好了")) {
            if (checkMutePermission()) {
                sender.mute(30)
                group.sendMessage("不许好，憋回去！")
            } else {
                group.sendMessage("好好好")
            }
        }
        if ((message.getPlainText().contains("muteme") || message.getPlainText()
                .contains("禁言自己")) && checkMutePermission()
        ) {
            val muteTime = (MuteMeConfig.minTime..MuteMeConfig.maxTime).random()
            sender.mute(muteTime)
            group.sendMessage(message.quote() + "好哒，已为您禁言了${muteTime}秒啦~")
        }
    }
}