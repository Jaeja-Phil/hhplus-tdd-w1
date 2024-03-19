package io.hhplus.tdd.service

import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Service
class ConcurrencyPointService(
    private val pointHistoryService: PointHistoryService,
    private val userPointService: UserPointService
) {
    /**
     * thread-safe 한 ConcurrentHashMap 을 사용하여 현재 처리중인 요청이 없을 경우에만
     * 포인트 추가 및 삭제를 진행하도록 합니다.
     */
    private val queue = ConcurrentHashMap<Long, ReentrantLock>()

    // Maximum number of retries
    private val maxRetries = 3

    // Delay between retries in milliseconds
    private val retryDelayMillis = 100L

    fun process(userId: Long, amount: Long, type: TransactionType): UserPoint {
        validateUserId(userId)
        validateAmount(amount)
        // userId 에 해당하는 lock 을 가져옵니다.
        val userLock = queue.computeIfAbsent(userId) { ReentrantLock() }

        // Retry logic for acquiring the lock
        var retries = 0
        var userPoint = UserPoint(userId, 0L, 0L)
        while (retries < maxRetries) {
            if (userLock.tryLock()) {
                try {
                    userPoint = userPointService.getUserPoint(userId)
                    when (type) {
                        TransactionType.CHARGE -> {
                            userPoint = userPointService.addPoint(userId, amount)
                            pointHistoryService.addChargePointHistory(userPoint, amount)
                        }
                        TransactionType.USE -> {
                            userPoint = userPointService.usePoint(userId, amount)
                            pointHistoryService.addUsePointHistory(userPoint, amount)
                        }
                    }
                    break // 저장 성공 후 while loop 를 빠져나옵니다.
                } finally {
                    // 작업이 완료되면 lock 을 해제합니다.
                    userLock.unlock()
                }
            }
            retries++
            TimeUnit.MILLISECONDS.sleep(retryDelayMillis)
        }

        // Check if the lock acquisition and operation were successful
        if (retries >= maxRetries) {
            throw Exception("$maxRetries retry 시도 실패.")
        }

        return userPoint
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