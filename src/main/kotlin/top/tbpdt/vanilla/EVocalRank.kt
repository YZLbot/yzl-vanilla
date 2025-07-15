package top.tbpdt.vanilla

/**
 * @author Takeoff0518
 */


import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.emptyMessageChain
import top.tbpdt.utils.MessageUtils.getRemovedPrefixCommand
import top.tbpdt.utils.MessageUtils.isCommand
import top.tbpdt.vanilla.utils.EVocalRackUtils.getImage
import top.tbpdt.vanilla.utils.EVocalRackUtils.getLatestRank


object EVocalRank : SimpleListenerHost() {
    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.overviewHandle() {
        if (!message.isCommand("vcrank")) {
            return
        }
        val requestRank = message.getRemovedPrefixCommand("vcrank").toIntOrNull()

        if (requestRank != null) {
            if (requestRank !in 1..30) {
                group.sendMessage("你查询的曲子超出了主榜范围(1~30)！")
                return
            }
            group.sendMessage(message.quote() + "加载中……")
            val latestData = try {
                getLatestRank()
            } catch (e: Exception) {
                group.sendMessage("意外失去了与母星的联系……\n${e.message}")
                e.printStackTrace()
                return
            }
            val requestData = latestData.main_rank[requestRank - 1]
            val returnStr = "#${requestRank}\n" +
                    "${requestData.title}\n" +
                    "得分: ${requestData.point}\n" +
                    "发布日期: ${requestData.pubdate}\n" +
                    "播放: ${requestData.play}\n" +
                    "评论: ${requestData.comment}\n" +
                    "弹幕: ${requestData.danmaku}\n" +
                    "点赞：${requestData.like}\n" +
                    "收藏: ${requestData.favorite}\n" +
                    "硬币: ${requestData.coin}\n" +
                    requestData.url + "\n"
            val image = getImage(requestData.avid, requestData.coverurl, group)
            if (image == null) {
                group.sendMessage("$returnStr[视频封面获取失败]")
            } else {
                group.sendMessage(PlainText(returnStr) + image)
            }
            return
        }
        val latestData = try {
            getLatestRank()
        } catch (e: Exception) {
            group.sendMessage("意外失去了与母星的联系……\n${e.message}")
            e.printStackTrace()
            return
        }
        val overviewStr = "周刊虚拟歌手中文曲排行榜♪${latestData.ranknum}\n" +
                "总计收录: ${latestData.statistic.total_collect_count}首\n" +
                "新曲数: ${latestData.statistic.new_video_count}首\n" +
                "新曲入榜数: ${latestData.statistic.new_in_rank_count}首\n" +
                "新曲入主榜数: ${latestData.statistic.new_in_mainrank_count}首\n" +
                "最后收录时间: ${latestData.collect_end_time}\n" +
                "发送 .vcrank [排名(1~30)] 以获取主榜详细信息~"
        group.sendMessage(overviewStr)

        var chain = emptyMessageChain()
        val result = ForwardMessageBuilder(group)
        chain += PlainText("主榜\n")
        for (i in latestData.main_rank) {
            chain += PlainText("#${i.rank} ${i.title}\n")
            if (i.rank % 10 == 0) {
                result.add(bot.id, bot.nick, chain)
                chain = emptyMessageChain()
            }
        }
        group.sendMessage(result.build())

//        val mainRankStr = mainRankStrBuilder.toString()
//
//        val subRankStrBuilder: StringBuilder = StringBuilder().append("副榜")
//        for (i in latestData.main_rank.slice(31..110)) {
//
//        }
    }
}