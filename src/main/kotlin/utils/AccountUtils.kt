package top.tbpdt.utils

import top.tbpdt.PluginMain.logger
import java.sql.Date
import java.sql.Timestamp

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
        SELECT ?, ?, ?, ?, ?, ?, ?, ?
        WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = ?)
    """
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, userId)
                preparedStatement.setString(2, userNick)

                val currentTimestamp = Timestamp(System.currentTimeMillis())
                val currentDate = Date(currentTimestamp.time)

                preparedStatement.setDate(3, currentDate)
//                preparedStatement.setDate(4, currentDate)
                preparedStatement.setDate(4, Date(0))
                preparedStatement.setLong(5, 0) // total_sign_days
                preparedStatement.setLong(6, 0) // continuous_sign_days
                preparedStatement.setLong(7, 0) // money
                preparedStatement.setLong(8, 0) // experience

                preparedStatement.setLong(9, userId) // for WHERE clause in the SELECT statement

                preparedStatement.executeUpdate()
            }
        }
    }

    /**
     * 判断指定的用户是否存在于表中
     */
    fun isUserExist(userId: Long): Boolean {
        val query = "SELECT 1 FROM accounts WHERE user_id = ? LIMIT 1"

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, userId)
                val resultSet = preparedStatement.executeQuery()
                return resultSet.next()
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
     * 获取昵称信息
     */
    fun queryNick(userId: Long): String {
        val query = "SELECT user_nick FROM accounts WHERE user_id = ?"
        var nick = ""

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, userId)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    nick = resultSet.getString("user_nick")
                }
            }
        }
        return nick
    }

    /**
     * 更新签到信息
     */
    fun updateSign(userId: Long, lastSignDate: Date, totalSignDays: Int, continuousSignDays: Int) {
        val query =
            "UPDATE accounts SET last_sign_date = ?, total_sign_days = ?, continuous_sign_days = ? WHERE user_id = ?"
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setDate(1, lastSignDate)
                preparedStatement.setInt(2, totalSignDays)
                preparedStatement.setInt(3, continuousSignDays)
                preparedStatement.setLong(4, userId)

                preparedStatement.executeUpdate()
            }
        }
    }

    /**
     * 更新钱数
     */
    fun updateMoney(userId: Long, money: Int) {
        val query = "UPDATE accounts SET money = ? WHERE user_id = ?"
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, money)
                preparedStatement.setLong(2, userId)

                preparedStatement.executeUpdate()
            }
        }
    }


    /**
     * 添加钱数
     * @param delta 钱数增减，可为负数
     * @return 若修改成功返回 `true`，否则（透支）返回 `false`
     */
    fun addMoney(userId: Long, delta: Int): Boolean {
        val query = "UPDATE accounts SET money = money + ? WHERE user_id = ?"
        val selectQuery = "SELECT money FROM accounts WHERE user_id = ?"
        var success = false

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(selectQuery).use { selectStatement ->
                selectStatement.setLong(1, userId)
                val resultSet = selectStatement.executeQuery()

                if (resultSet.next()) {
                    val currentMoney = resultSet.getInt("money")
                    if (currentMoney + delta >= 0) {
                        connection.prepareStatement(query).use { preparedStatement ->
                            preparedStatement.setInt(1, delta)
                            preparedStatement.setLong(2, userId)
                            preparedStatement.executeUpdate()
                            success = true
                        }
                    }
                }
            }
        }
        return success
    }


    /**
     * 更新经验
     */
    fun updateExperience(userId: Long, experience: Int) {
        val query = "UPDATE accounts SET experience = ? WHERE user_id = ?"
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, experience)
                preparedStatement.setLong(2, userId)

                preparedStatement.executeUpdate()
            }
        }
    }

    /**
     * 获取账号信息
     */
    fun queryAccount(userId: Long): List<Account> {
        val query = """
        SELECT user_nick, encounter_date, last_sign_date, total_sign_days, continuous_sign_days, money, experience 
        FROM accounts WHERE user_id = ?
    """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, userId)

                val resultSet = preparedStatement.executeQuery()

                return generateSequence {
                    if (resultSet.next()) {
                        Account(
                            userId,
                            resultSet.getString("user_nick"),
                            resultSet.getDate("encounter_date"),
                            resultSet.getDate("last_sign_date"),
                            resultSet.getInt("total_sign_days"),
                            resultSet.getInt("continuous_sign_days"),
                            resultSet.getInt("money"),
                            resultSet.getInt("experience")
                        )
                    } else {
                        null
                    }
                }.toList()
            }
        }
    }

    /**
     * 获取钱数
     */
    fun queryMoney(userId: Long): Int {
        val query = "SELECT money FROM accounts WHERE user_id = ?"
        var money = 0

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, userId)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    money = resultSet.getInt("money")
                }
            }
        }
        return money
    }

    /**
     * 获取经验
     */
    fun queryExperience(userId: Long): Int {
        val query = "SELECT experience FROM accounts WHERE user_id = ?"
        var experience = 0

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, userId)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    experience = resultSet.getInt("experience")
                }
            }
        }
        return experience
    }

    /**
     * 获取两个日期间整的天数
     */
    fun getDaysBetweenDates(date1: Date, date2: Date): Int {
        val millisPerDay = 24 * 60 * 60 * 1000

        val utilDate1 = java.util.Date(date1.time)
        val utilDate2 = java.util.Date(date2.time)

        utilDate1.hours = 0
        utilDate1.minutes = 0
        utilDate1.seconds = 0
        utilDate1.time = utilDate1.time / millisPerDay * millisPerDay

        utilDate2.hours = 0
        utilDate2.minutes = 0
        utilDate2.seconds = 0
        utilDate2.time = utilDate2.time / millisPerDay * millisPerDay

        val diff = utilDate2.time - utilDate1.time
        return (diff / millisPerDay).toInt()
    }

    /**
     * 签到
     * @return first: 累计时间
     *
     * second: 连续时间
     */
    fun sign(userId: Long): Pair<Int, Int> {
        val userAccount = queryAccount(userId).first()
        val nowDate = Date(System.currentTimeMillis())
        val intervalDays = getDaysBetweenDates(userAccount.lastSignDate, nowDate)
        return when {
            intervalDays > 1 -> { // 未能连续签到
                val updatedTotalSignDays = userAccount.totalSignDays + 1
                updateSign(
                    userId,
                    nowDate,
                    updatedTotalSignDays,
                    1
                )
                Pair(updatedTotalSignDays, 1)
            }

            intervalDays == 1 -> { // 连续签到
                val updatedTotalSignDays = userAccount.totalSignDays + 1
                val updatedContinuousSignDays = userAccount.continuousSignDays + 1
                updateSign(
                    userId,
                    nowDate,
                    updatedTotalSignDays,
                    updatedContinuousSignDays
                )
                Pair(updatedTotalSignDays, updatedContinuousSignDays)
            }

            else -> { // 当天签到
                Pair(-1, -1)
            }
        }
    }

    data class Account(
        val userId: Long,
        val userNick: String,
        val encounterDate: Date,
        val lastSignDate: Date,
        val totalSignDays: Int,
        val continuousSignDays: Int,
        val money: Int,
        val experience: Int
    )
}