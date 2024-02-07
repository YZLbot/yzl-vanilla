package top.tbpdt.utils

import top.tbpdt.PluginMain.logger
import java.sql.Date
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDate

/**
 * @author Takeoff0518
 */
object AccountUtils {

    fun createTable() {
        DBUtils.connectToDB().use { connection ->
            DBUtils.createTable(
                connection,
                """
                    CREATE TABLE IF NOT EXISTS accounts (
                    user_id INT,
                    user_nick TEXT,
                    encounter_date DATE,
                    last_sign_date DATE,
                    total_sign_days INT,
                    continuous_sign_days INT,
                    money INT,
                    experience INT,
                    PRIMARY KEY (user_id)
                    )
                    """
            )
        }
        logger.info("已尝试创建数据表 accounts")
    }


    /**
     * 初始化账户信息
     * @param userId QQ 号
     * @param userNick 昵称
     */
    fun initUserAccount(userId: Long, userNick: String) {
        val query = """
        INSERT INTO accounts (user_id, user_nick, encounter_date, last_sign_date, total_sign_days, continuous_sign_days, money, experience)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, userId)
                preparedStatement.setString(2, userNick)

                val currentTimestamp = Timestamp(System.currentTimeMillis())
                val currentDate = Date(currentTimestamp.time)

                preparedStatement.setDate(3, currentDate)
                preparedStatement.setDate(4, currentDate)
                preparedStatement.setLong(5, 0)
                preparedStatement.setLong(6, 0)
                preparedStatement.setLong(7, 0)
                preparedStatement.setLong(8, 0)

                preparedStatement.executeUpdate()
            }
        }
    }


    /**
     * 更新昵称信息
     */
    fun updateNick(userId: Long, userNick: String) {
        val query = "UPDATE accounts SET user_nick = ? WHERE user_id = ?"
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, userNick)
                preparedStatement.setLong(2, userId)

                preparedStatement.executeUpdate()
            }
        }

    }

    /**
     * 更新签到信息
     */
    fun updateSign(userId: Long, lastSignDate: Date, totalSignDays: Long, continuousSignDays: Long) {
        val query =
            "UPDATE accounts SET last_sign_date = ?, total_sign_days = ?, continuous_sign_days = ? WHERE user_id = ?"
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, lastSignDate)
                preparedStatement.setLong(2, totalSignDays)
                preparedStatement.setLong(3, continuousSignDays)
                preparedStatement.setLong(4, userId)

                preparedStatement.executeUpdate()
            }
        }

    }

    /*
    fun hasSignedToday(userId: Long): Boolean {
        return
    }

    fun getSignDate(userId: Long): LocalDate {

    }
    */

    /**
     * 签到
     * @return first: 累计时间
     *
     * second: 连续时间
     */
    /*
    fun sign(userId: Long): Pair<Int, Int> {
        val lastDate = getSignDate(userId)
        val nowDate = LocalDate.now()
        val duration = Duration.between(lastDate.atStartOfDay(), nowDate.atStartOfDay()).toDays()
        if (duration <= 1) {
            updateSign(userId,,)
        }
    }
    */

}