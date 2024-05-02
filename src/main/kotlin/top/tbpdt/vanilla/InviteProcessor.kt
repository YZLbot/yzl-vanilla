package top.tbpdt.vanilla

import kotlinx.coroutines.delay
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import top.tbpdt.configer.GlobalConfig
import top.tbpdt.configer.GlobalConfig.autoAcceptInvitedJoinGroupRequest
import top.tbpdt.configer.GlobalConfig.autoAcceptNewFriendRequest
import top.tbpdt.configer.GlobalConfig.experienceLimit
import top.tbpdt.configer.GlobalConfig.moneyLimit
import top.tbpdt.utils.AccountUtils
import top.tbpdt.utils.AccountUtils.queryExperience
import top.tbpdt.utils.AccountUtils.queryMoney


/**
 * @author Takeoff0518
 */
object InviteProcessor : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun NewFriendRequestEvent.handle() {
        val textTemplate = """
            |添加者：${this.fromNick}(${this.fromId})
            |来自群：${if (this.fromGroupId == 0L) "未知" else fromGroupId}
        """.trimMargin()
        if (!autoAcceptNewFriendRequest) {
            bot.getGroup(GlobalConfig.group)?.sendMessage("按照配置，已回绝加好友请求~\n$textTemplate")
        }
        if (queryExperience(fromId) < experienceLimit) {
            bot.getGroup(GlobalConfig.group)
                ?.sendMessage("由于经验限制 (${queryExperience(fromId)} < $experienceLimit)，已回绝加好友请求~\n$textTemplate")
            return
        }
        delay((1000L..5000L).random())
        accept()
        bot.getGroup(GlobalConfig.group)?.sendMessage("已同意加好友请求~\n$textTemplate")
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun BotInvitedJoinGroupRequestEvent.handle() {
        val textTemplate = """
            |邀请者：${this.invitorNick}(${this.invitorId})
            |目标群：${this.groupName}(${this.groupId})
        """.trimMargin()
        if (!autoAcceptInvitedJoinGroupRequest) {
            bot.getGroup(GlobalConfig.group)?.sendMessage("按照配置，已回绝拉群请求~\n$textTemplate")
            return
        }
        if (queryExperience(invitorId) < experienceLimit) {
            bot.getGroup(GlobalConfig.group)
                ?.sendMessage("由于经验限制 (${queryExperience(invitorId)} < $experienceLimit)，已回绝拉群请求~\n$textTemplate")
            return
        }
        if (queryMoney(invitorId) < moneyLimit) {
            bot.getGroup(GlobalConfig.group)
                ?.sendMessage("由于经济限制 (${queryMoney(invitorId)} < $moneyLimit)，已回绝拉群请求~\n$textTemplate")
            return
        }
        AccountUtils.addMoney(invitorId, -moneyLimit)
        val group = bot.getGroup(GlobalConfig.group)
        if (group != null && invitorId in group.members) {
            delay((1000L..5000L).random())
            accept()
            bot.getGroup(GlobalConfig.group)?.sendMessage("已同意拉群请求，消耗 $moneyLimit li~\n$textTemplate")
        } else if (invitorId in bot.friends) {
            bot.getFriend(invitorId)?.sendMessage("请在加群 ${GlobalConfig.group} 后再次尝试拉群~")
            bot.getGroup(GlobalConfig.group)?.sendMessage("由于未加饲养群，已回绝拉群请求~\n$textTemplate")
        }
    }
}