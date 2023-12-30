package top.tbpdt

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.info
import top.tbpdt.Processor.Handler
import java.io.File

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.tbpdt.vanilla",
        name = "YZLbot-Vanilla",
        version = "2.0.0",
    ) {

        author("Takeoff0518")
    }
) {
    @OptIn(MiraiExperimentalApi::class)
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        Handler.register()
    }
    override fun onDisable() {

    }

}