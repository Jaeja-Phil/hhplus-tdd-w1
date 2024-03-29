package io.hhplus.tdd.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.springframework.stereotype.Service

@Service
class PointHistoryService(
    private val pointHistoryTable: PointHistoryTable,
) {
    fun addChargePointHistory(userPoint: UserPoint, amount: Long): PointHistory {
        return pointHistoryTable.insert(
            userPoint.id,
            amount,
            TransactionType.CHARGE,
            System.currentTimeMillis()
        )
    }

    fun addUsePointHistory(userPoint: UserPoint, amount: Long): PointHistory {
        return pointHistoryTable.insert(
            userPoint.id,
            amount,
            TransactionType.USE,
            System.currentTimeMillis()
        )
    }

    fun getPointHistories(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }

}
