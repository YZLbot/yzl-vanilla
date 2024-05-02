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

    @ValueDescription("单次投稿花费经济")
    val addCost: Int by value(2)

    @ValueDescription("查询返还经济概率(%)")
    val queryRewardProbability: Int by value(2)

    @ValueDescription("查询返还经济")
    val queryRewardMoney: Int by value(1)

    @ValueDescription("冷却时间(ms)")
    val CDTime: Long by value(40000L)

    @ValueDescription("cave_id 黑名单")
    val caveBlackList: MutableSet<Int> by value()
}