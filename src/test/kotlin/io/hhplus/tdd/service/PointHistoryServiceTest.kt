package io.hhplus.tdd.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointHistoryServiceTest {
    private lateinit var pointHistoryService: PointHistoryService
    private lateinit var pointHistoryTable: PointHistoryTable

    @BeforeEach
    fun setUp() {
        pointHistoryTable = PointHistoryTable()
        pointHistoryService = PointHistoryService(pointHistoryTable)
    }

    private fun checkPointHistory(
        userId: Long,
        amount: Long,
        type: TransactionType,
        pointHistory: PointHistory
    ) {
        assertEquals(userId, pointHistory.userId)
        assertEquals(amount, pointHistory.amount)
        assertEquals(type, pointHistory.type)
    }

    @Test
    fun `addChargePointHistory - 포인트 추가 이력을 생성할 수 있다`() {
        // given
        val userId = 1L
        val amount = 100L

        // when
        val pointHistory = pointHistoryService.addChargePointHistory(userId, amount)

        // then
        checkPointHistory(userId, amount, TransactionType.CHARGE, pointHistory)
    }

    @Test
    fun `addUsePointHistory - 포인트 사용 이력을 생성할 수 있다`() {
        // given
        val userId = 1L
        val amount = 100L

        // when
        val pointHistory = pointHistoryService.addUsePointHistory(userId, amount)

        // then
        checkPointHistory(userId, amount, TransactionType.USE, pointHistory)
    }

    @Test
    fun `getPointHistories - 특정 유저의 포인트 이력을 조회할 수 있다`() {
        // given
        val userId = 1L
        val amount = 100L
        pointHistoryService.addChargePointHistory(userId, amount)
        pointHistoryService.addUsePointHistory(userId, amount)

        // when
        val pointHistories = pointHistoryService.getPointHistories(userId)

        // then, 총 2개의 이력이 있으며, 1개는 CHARGE, 1개는 USE 타입인지 확인
        assertEquals(2, pointHistories.size)
        assertEquals(1, pointHistories.filter { it.type == TransactionType.CHARGE }.size)
        assertEquals(1, pointHistories.filter { it.type == TransactionType.USE }.size)
    }


}