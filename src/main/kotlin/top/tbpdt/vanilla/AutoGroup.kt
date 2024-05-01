package top.tbpdt.vanilla

import kotlinx.coroutines.delay
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import top.tbpdt.vanilla.PluginMain.logger
import top.tbpdt.configer.AutoConfig
import top.tbpdt.utils.MessageUtils.encodeToMiraiCode

/**
 * @author Takeoff0518
 */
object AutoGroup : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun GroupTalkativeChangeEvent.handle() {
        if (previous.id == bot.id) {
            group.sendMessage("我的龙王被抢走了...")
            delay(2000)
            group.sendMessage(PlainText("呜呜呜...").plus(At(now)).plus(PlainText(" 你还我龙王！！！")))
            delay(3000)
            now.sendMessage("还给我还给我还给我还给我还给我")
        } else {
            group.sendMessage(At(previous) + PlainText(" 的龙王被") + At(now) + PlainText(" 抢走了，好可怜"))
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun MemberJoinEvent.handle() {
        if (AutoConfig.newMemberJoinMessage.isNotEmpty()) {
            group.sendMessage(AutoConfig.newMemberJoinMessage.random())
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun MemberLeaveEvent.Kick.handle() {
        if (AutoConfig.quitMessage.isEmpty()) return
        val msg = AutoConfig.kickMessage.encodeToMiraiCode(operatorOrBot, member).deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun MemberLeaveEvent.Quit.handle() {
        if (AutoConfig.quitMessage.isEmpty()) return
        val msg = AutoConfig.quitMessage.encodeToMiraiCode(member, true).deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun MemberMuteEvent.handle() {
        val msg = AutoConfig.memberMutedMessage
            .encodeToMiraiCode(operatorOrBot, member)
            .deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun MemberUnmuteEvent.handle() {
        val msg = AutoConfig.memberUnmuteMessage
            .encodeToMiraiCode(operatorOrBot, member)
            .deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun BotMuteEvent.handle() {
        try {
            for (msg in AutoConfig.botMutedMessage) {
                operator.sendMessage(msg)
                delay(1000)
            }
        } catch (e: Exception) {
            logger.error("$e 好像没法发送临时消息...")
        }
    }
}