import kotlinx.coroutines.delay
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import top.tbpdt.configer.GlobalConfig

/**
 * @author Takeoff0518
 */
object InviteProcessor : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun NewFriendRequestEvent.handle() {
        if (!GlobalConfig.autoAcceptNewFriendRequest) {
            bot.getGroup(GlobalConfig.group)?.sendMessage("按照配置，已回绝加好友请求：\n$this")
            return
        }
        delay((1000L..5000L).random())
        accept()
        bot.getGroup(GlobalConfig.group)?.sendMessage("已同意加好友请求：\n$this")
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun BotInvitedJoinGroupRequestEvent.handle() {
        if (!GlobalConfig.autoAcceptInvitedJoinGroupRequest) {
            bot.getGroup(GlobalConfig.group)?.sendMessage("按照配置，已回绝拉群请求：\n$this")
            return
        }
        val group = bot.getGroup(GlobalConfig.group)
        if (group != null && invitorId in group.members) {
            delay((1000L..5000L).random())
            accept()
            bot.getGroup(GlobalConfig.group)?.sendMessage("已同意拉群请求：\n$this")
        } else if (invitorId in bot.friends) {
            bot.getFriend(invitorId)?.sendMessage("请在加群${GlobalConfig.group} 后再次尝试拉群~")
            bot.getGroup(GlobalConfig.group)?.sendMessage("由于未加饲养群，已回绝拉群请求：\n$this")
        }
    }
}