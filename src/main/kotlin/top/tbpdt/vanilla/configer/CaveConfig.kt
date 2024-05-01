package top.tbpdt.vanilla.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object CaveConfig : AutoSavePluginConfig("CaveConfig") {
    @ValueDescription("是否启用图片上传功能")
    val enablePics: Boolean by value(true)

    @ValueDescription("是否启用回声洞投稿")
    val enableCaveAdd: Boolean by value(true)

    @ValueDescription("cave_id 黑名单")
    val caveBlackList: MutableSet<Int> by value()
}