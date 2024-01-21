package top.tbpdt

import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.registerTo
import net.mamoe.mirai.utils.info
import top.tbpdt.configer.AutoConfig
import top.tbpdt.configer.EmojiConfig
import top.tbpdt.configer.GlobalConfig
import top.tbpdt.utils.CaveUtils
import top.tbpdt.utils.LogStrImage

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.tbpdt.vanilla",
        name = "YZLbot-Vanilla",
        version = "2.0.1",
    ) {

        author("Takeoff0518")
    }
) {
    override fun onEnable() {
        logger.info { LogStrImage.STRIMG_YZLBOT }
        logger.info { "GitHub 地址：https://github.com/YZLbot/yzl-vanilla"}
        logger.info { "正在加载配置..." }
        AutoConfig.reload()
        EmojiConfig.reload()
        GlobalConfig.reload()
        logger.info { "正在注册监听器到全局..." }
        EmojiFetch.registerTo(globalEventChannel())
        AdminHandler.registerTo(globalEventChannel())
        Cave.registerTo(globalEventChannel())
        logger.info { "正在加载数据库" }
        CaveUtils.initCaveDB()
    }

    override fun onDisable() {
        logger.info { "正在注销监听器..." }
        EmojiFetch.cancel()
        AdminHandler.cancel()
        Cave.cancel()
        logger.info { "禁用成功！" }
    }

}