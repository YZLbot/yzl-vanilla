package top.tbpdt.vanilla

import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import top.tbpdt.configer.GlobalConfig
import top.tbpdt.utils.MessageUtils.checkMutePermission
import top.tbpdt.utils.MessageUtils.getPlainText
import top.tbpdt.utils.MessageUtils.getRemovedPrefixCommand
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.vanilla.configer.CensorConfig.enableCensor
import top.tbpdt.vanilla.configer.CensorConfig.muteTime
import top.tbpdt.vanilla.utils.CensorUtils.checkCensor
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Takeoff0518
 */
object ContentCensor : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.HIGH)
    suspend fun GroupMessageEvent.handle() {
        if (group.id !in enableCensor) return
        if (!checkMutePermission()) return
        val match = checkCensor(message.getPlainText()) // 脱敏处理
        if (match != null) {
            message.recall()
            delay(1000)
            if (group.id in muteTime && muteTime.getValue(group.id) > 0) {
                sender.mute(muteTime.getValue(group.id))
                group.sendMessage(
                    "群员 ${sender.nameCardOrNick}(${sender.id}) 触发消息正则审查\n" +
                            "已禁言 ${muteTime.getValue(group.id)} 秒！"
                )
            } else {
                group.sendMessage("群员 ${sender.nameCardOrNick}(${sender.id}) 触发消息正则审查")
            }
            if (group.owner.id in bot.friends) {
                group.owner.sendMessage(
                    "有群员触发了消息正则审查：\n" +
                            "群聊：${group.name}(${group.id})\n" +
                            "群员：${sender.nameCardOrNick}(${sender.id})\n" +
                            "命中：${match}\n" +
                            "时间：${SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date())}"
                )
                val recalledMessage = ForwardMessageBuilder(group)
                recalledMessage.add(this)
                group.owner.sendMessage(recalledMessage.build())
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun GroupMessageEvent.commandHandle() {
        val isPermissionOK = (sender.id in GlobalConfig.admin || sender.permission.level > 0)
        if (message.isCommand("censor time") && isPermissionOK) {
            val newMuteTime = message.getRemovedPrefixCommand("censor time").toIntOrNull()
            if (newMuteTime == null) {
                group.sendMessage("参数类型不匹配，应为 [整数]！")
                return
            }
            if (newMuteTime == 0) {
                muteTime.remove(group.id)
                group.sendMessage(At(sender) + "已清除触发敏感词检测的禁言~")
            } else {
                muteTime[group.id] = newMuteTime
                group.sendMessage(At(sender) + "已将触发敏感词检测的禁言时间改为 $newMuteTime 秒~")
            }
        } else if (message.isCommand("censor")) {
            if (isPermissionOK) {
                if (group.id in enableCensor) {
                    enableCensor.remove(group.id)
                    group.sendMessage(At(sender) + "已关闭该群的敏感词检测~")
                } else {
                    enableCensor.add(group.id)
                    group.sendMessage(
                        At(sender) + "已启用该群的敏感词检测~\n" +
                                "如果机器人不是群主或者管理员，敏感词检测不会工作哦~\n" +
                                "若想获取触发检测后撤回的信息内容，请群主添加机器人好友~\n" +
                                "敏感词审查词库：https://github.com/YZLbot/censor-dict"
                    )
                }
            } else {
                group.sendMessage(At(sender) + "该群的敏感词检测：${if (group.id in enableCensor) "启用" else "关闭"}")
            }
        }
    }
}