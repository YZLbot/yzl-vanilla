package top.tbpdt.vanilla

import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import top.tbpdt.configer.AutoConfig
import top.tbpdt.configer.AutoConfig.botUnmutedMessage
import top.tbpdt.configer.AutoConfig.defaultNewMemberJoinMessage
import top.tbpdt.configer.AutoConfig.groupMuteAllRelease
import top.tbpdt.configer.AutoConfig.newMemberJoinMessage
import top.tbpdt.utils.MessageUtils.encodeToMiraiCode
import top.tbpdt.vanilla.PluginMain.logger

/**
 * @author Takeoff0518
 */
object AutoGroup : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MemberJoinEvent.handle() {
        if (member.id == bot.id) {
            return
        }
        if (groupId in newMemberJoinMessage) {
            group.sendMessage(At(member) + newMemberJoinMessage.getValue(groupId))
        } else if (defaultNewMemberJoinMessage.isNotEmpty()) {
            group.sendMessage(At(member) + defaultNewMemberJoinMessage.random())
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MemberLeaveEvent.Quit.handle() {
        if (AutoConfig.quitMessage.isEmpty()) return
        val msg = AutoConfig.quitMessage.replace("%主动%", "${member.nick}(${member.id})").deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MemberLeaveEvent.Kick.handle() {
        if (AutoConfig.kickMessage.isEmpty()) return
        val msg = AutoConfig.kickMessage.replace("%主动%", "[mirai:at:${operatorOrBot.id}]")
            .replace("%被动%", "${member.nick}(${member.id})").deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MemberMuteEvent.handle() {
        if (operatorOrBot == group.botAsMember) return
        val msg = AutoConfig.memberMutedMessage
            .encodeToMiraiCode(operatorOrBot, member)
            .deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MemberUnmuteEvent.handle() {
        if (operatorOrBot == group.botAsMember) return
        val msg = AutoConfig.memberUnmuteMessage
            .encodeToMiraiCode(operatorOrBot, member)
            .deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.LOWEST)
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

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun BotUnmuteEvent.handle() {
        delay(1000)
        val msg = botUnmutedMessage.encodeToMiraiCode(operator, true).deserializeMiraiCode()
        group.sendMessage(msg)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun BotJoinGroupEvent.handle() {
        delay(1000)
        group.sendMessage("我来啦~")
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMuteAllEvent.handle() {
        if (!new) {
            val msg = groupMuteAllRelease.encodeToMiraiCode(operatorOrBot, true).deserializeMiraiCode()
            group.sendMessage(msg)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MemberPermissionChangeEvent.handle() {
        val msg = when {
            origin.isOwner() || new.isOwner() -> PlainText("群主变了？？？")
            origin.isAdministrator() && !new.isOperator() -> At(member).plus(PlainText(" 的管理没了，好可惜"))
            else -> At(member).plus(PlainText(" 升职啦！"))
        }
        group.sendMessage(msg)
    }

}