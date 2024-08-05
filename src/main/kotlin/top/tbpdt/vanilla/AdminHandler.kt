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
import top.tbpdt.vanilla.PluginMain.save
import top.tbpdt.vanilla.configer.AutoPicsConfig
import top.tbpdt.vanilla.configer.CaveConfig
import top.tbpdt.vanilla.configer.CensorConfig

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
                CensorConfig.reload()
                AutoPicsConfig.reload()
                AutoPics.initPaths()
                subject.sendMessage("重载配置成功~")
            } catch (e: Exception) {
                subject.sendMessage("重载配置失败！\n$e")
            }
        }
        if (message.isCommand("save")) {
            try {
                AutoConfig.save()
                EmojiConfig.save()
                GlobalConfig.save()
                CaveConfig.save()
                MuteMeConfig.save()
                CensorConfig.save()
                AutoPicsConfig.save()
                subject.sendMessage("强行保存配置成功~")
            } catch (e: Exception) {
                subject.sendMessage("强行保存配置失败！\n$e")
            }
        }
    }
}