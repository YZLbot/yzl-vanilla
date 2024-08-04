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
import top.tbpdt.configer.GlobalConfig.joinGroupExperienceLimit
import top.tbpdt.configer.GlobalConfig.moneyLimit
import top.tbpdt.configer.GlobalConfig.newFriendExperienceLimit
import top.tbpdt.utils.AccountUtils
import top.tbpdt.utils.AccountUtils.queryExperience
import top.tbpdt.utils.AccountUtils.queryMoney
import top.tbpdt.vanilla.PluginMain.logger


/**
 * @author Takeoff0518
 */
object InviteProcessor : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun NewFriendRequestEvent.handle() {
        val textTemplate = """
            |添加者：${this.fromNick}(${this.fromId})
            |来自群：${if (this.fromGroupId == 0L) "未知" else fromGroupId}
        """.trimMargin()
        if (!autoAcceptNewFriendRequest) {
            bot.getGroup(GlobalConfig.group)?.sendMessage("由于配置禁用自动加好友，已回绝加好友请求~\n$textTemplate")
        }
        if (queryExperience(fromId) < newFriendExperienceLimit) {
            bot.getGroup(GlobalConfig.group)
                ?.sendMessage("由于经验限制 (${queryExperience(fromId)} < $newFriendExperienceLimit)，已回绝加好友请求~\n$textTemplate")
            return
        }
        delay((1000L..5000L).random())
        accept()
        val friend = bot.getFriend(fromId)
        if (friend != null) {
            friend.sendMessage("很高兴与你相识！\n加入饲养群 ${GlobalConfig.group} 以获取更多信息~")
        } else {
            logger.warning("由于好友为空，加群提醒发送失败！")
        }
        bot.getGroup(GlobalConfig.group)?.sendMessage("已同意加好友请求~\n$textTemplate")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun BotInvitedJoinGroupRequestEvent.handle() {
        val textTemplate = """
            |邀请者：${this.invitorNick}(${this.invitorId})
            |目标群：${this.groupName}(${this.groupId})
        """.trimMargin()
        if (!autoAcceptInvitedJoinGroupRequest) {
            bot.getGroup(GlobalConfig.group)?.sendMessage("由于配置禁用自动拉群，已回绝拉群请求~\n$textTemplate")
            return
        }
        if (queryExperience(invitorId) < joinGroupExperienceLimit) {
            bot.getGroup(GlobalConfig.group)
                ?.sendMessage("由于经验限制 (${queryExperience(invitorId)} < $joinGroupExperienceLimit)，已回绝拉群请求~\n$textTemplate")
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
//            delay((1000L..5000L).random())
//            val invitedGroup = bot.getGroup(groupId)!!
//            if (invitedGroup.members.size < groupMemberLimit) {
//                invitedGroup.quit()
//                AccountUtils.addMoney(invitorId, moneyLimit)
//                bot.getGroup(GlobalConfig.group)
//                    ?.sendMessage("由于群人数不足 (${invitedGroup.members.size} < $groupMemberLimit)，已自动退出群聊\n已返还消耗的 li~")
//            }
        } else if (invitorId in bot.friends) {
            bot.getFriend(invitorId)?.sendMessage("请在加入饲养群 ${GlobalConfig.group} 后再次尝试拉群~")
            bot.getGroup(GlobalConfig.group)?.sendMessage("由于未加饲养群，已回绝拉群请求~\n$textTemplate")
        }
    }
}