package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import top.tbpdt.configer.GlobalConfig.admin
import top.tbpdt.utils.MessageUtils.getRemovedPrefixCommand
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.vanilla.PluginMain.save
import top.tbpdt.vanilla.configer.BlacklistConfig
import top.tbpdt.vanilla.configer.BlacklistConfig.groupBlacklist
import top.tbpdt.vanilla.configer.BlacklistConfig.userBlacklist

/**
 * @author Takeoff0518
 */
object Blacklist : SimpleListenerHost() {

    /*
        onCommand() 与 onBlacklist() 互换顺序，
        前者就会在同一个群启用黑名单后再移除黑名单时哑火。
        可是为啥啊QAQ
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun MessageEvent.onCommand() {
        if (message.isCommand("blackg")) {
            val groupId = message.getRemovedPrefixCommand("blackg").toLongOrNull()
            if (!((this is GroupMessageEvent &&
                        sender.permission.level >= group.botPermission.level &&
                        groupId == group.id) ||
                        sender.id in admin)
            ) {
                return
            }
            if (groupId == null) {
                subject.sendMessage("参数类型不匹配，应为 [长整型]！")
                return
            }
            if (groupId in groupBlacklist) {
                groupBlacklist.remove(groupId)
                BlacklistConfig.save()
                subject.sendMessage("已移除群 $groupId 的黑名单！")
            } else {
                groupBlacklist.add(groupId)
                BlacklistConfig.save()
                subject.sendMessage("已添加群 $groupId 的黑名单！")
            }
        }
        if (message.isCommand("blacku")) {
            if (sender.id !in admin) {
                return
            }
            val userId = message.getRemovedPrefixCommand("blacku").toLongOrNull()
            if (userId == null) {
                subject.sendMessage("参数类型不匹配，应为 [长整型]！")
                return
            }
            if (userId in userBlacklist) {
                userBlacklist.remove(userId)
                BlacklistConfig.save()
                subject.sendMessage("已移除用户 $userId 的黑名单！")
            } else {
                userBlacklist.add(userId)
                BlacklistConfig.save()
                subject.sendMessage("已添加用户 $userId 的黑名单！")
            }
        }
        if (message.isCommand("blackl")) {
            if (sender.id !in admin) {
                return
            }
            val result = "共有 ${userBlacklist.size} 个用户黑名单：\n$userBlacklist\n" +
                    "共有 ${groupBlacklist.size} 个群黑名单：\n$groupBlacklist"
            subject.sendMessage(result)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun MessageEvent.onBlacklist() {
        if (this is GroupMessageEvent) {
            if (((sender.id in admin && !message.isCommand(""))) || message.isCommand("censor")) {
                return
            }
            if (group.id in groupBlacklist) {
                intercept()
            }
        } else {
            if (sender.id in userBlacklist) {
                intercept()
            }
        }
    }
}