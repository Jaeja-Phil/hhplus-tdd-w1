package io.hhplus.tdd.service

import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ConcurrencyPointService(
    private val pointHistoryService: PointHistoryService,
    private val userPointService: UserPointService,
    private val lockService: LockService
) {

    fun process(userId: Long, amount: Long, type: TransactionType): UserPoint {
        validateUserId(userId)
        validateAmount(amount)

        return lockService.runWithLock(userId) {
            when (type) {
                TransactionType.CHARGE -> {
                    val userPoint = userPointService.addPoint(userId, amount)
                    pointHistoryService.addChargePointHistory(userId, amount)
                    userPoint
                }
                TransactionType.USE -> {
                    val userPoint = userPointService.usePoint(userId, amount)
                    pointHistoryService.addUsePointHistory(userId, amount)
                    userPoint
                }
            }
        }
    }

    private fun validateUserId(userId: Long) {
        if (userId < 1) {
            throw IllegalArgumentException("userId 는 1 이상이어야 합니다.")
        }
    }

    private fun validateAmount(amount: Long) {
        if (amount < 1) {
            throw IllegalArgumentException("amount 는 1 이상이어야 합니다.")
        }
    }
}