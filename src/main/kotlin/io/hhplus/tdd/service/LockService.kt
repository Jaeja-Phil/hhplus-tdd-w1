package io.hhplus.tdd.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * LockService
 *
 * @author jaypark
 * @version 1.0.0
 * @since 3/20/24
 */
@Service
class LockService {

    companion object {
        /**
         * thread-safe 한 ConcurrentHashMap 을 사용하여 현재 처리중인 요청이 없을 경우에만
         * 포인트 추가 및 삭제를 진행하도록 합니다.
         */
        private val queue = ConcurrentHashMap<Long, ReentrantLock>()

        // Maximum number of retries
        private val maxRetries = 3

        // Delay between retries in milliseconds
        private val retryDelayMillis = 100L
    }

    fun <T> runWithLock(lockId: Long, action: () -> T): T {
        val lock = queue.computeIfAbsent(lockId) { ReentrantLock() }
        var retries = 0

        while (retries < maxRetries) {
            if (lock.tryLock()) {
                try {
                    return action()
                } finally {
                    // 작업이 완료되면 lock 을 해제합니다.
                    lock.unlock()
                }
            }
            retries++
            TimeUnit.MILLISECONDS.sleep(retryDelayMillis)
        }

        throw Exception("$maxRetries retry 시도 실패.")
    }
}