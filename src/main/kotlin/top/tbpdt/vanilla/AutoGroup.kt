package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.message.data.At
import top.tbpdt.configer.AutoConfig.defaultNewMemberJoinMessage
import top.tbpdt.configer.AutoConfig.newMemberJoinMessage

/**
 * @author Takeoff0518
 */
object AutoGroup : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun MemberJoinEvent.handle() {
        if (groupId in newMemberJoinMessage) {
            group.sendMessage(At(member) + newMemberJoinMessage.getValue(groupId))
        } else if (defaultNewMemberJoinMessage.isNotEmpty()) {
            group.sendMessage(At(member) + defaultNewMemberJoinMessage.random())
        }
    }
}