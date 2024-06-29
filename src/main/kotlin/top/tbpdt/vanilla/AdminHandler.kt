package top.tbpdt.vanilla

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import top.tbpdt.configer.AutoConfig
import top.tbpdt.configer.EmojiConfig
import top.tbpdt.configer.GlobalConfig
import top.tbpdt.configer.MuteMeConfig
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.vanilla.PluginMain.reload
import top.tbpdt.vanilla.configer.CaveConfig

/**
 * @author Takeoff0518
 */
object AdminHandler : SimpleListenerHost() {

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun MessageEvent.reloadHandle() {
        if (sender.id !in GlobalConfig.admin) return
        if (message.isCommand("reload")) {
            try {
                AutoConfig.reload()
                EmojiConfig.reload()
                GlobalConfig.reload()
                CaveConfig.reload()
                MuteMeConfig.reload()
                subject.sendMessage("重载成功~")
            } catch (e: Exception) {
                subject.sendMessage("重载失败！\n$e")
            }
        }
    }
}