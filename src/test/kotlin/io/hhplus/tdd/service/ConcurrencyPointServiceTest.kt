package io.hhplus.tdd.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.error.NotEnoughPointException
import io.hhplus.tdd.point.TransactionType
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class ConcurrencyPointServiceTest {
    private lateinit var concurrencyPointService: ConcurrencyPointService
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable

    @BeforeEach
    fun setUp() {
        userPointTable = UserPointTable()
        pointHistoryTable = PointHistoryTable()
        concurrencyPointService = ConcurrencyPointService(
            PointHistoryService(pointHistoryTable),
            UserPointService(userPointTable)
        )
    }

    @Test
    fun `process - userId가 1보다 작을 경우 IllegalArgumentException을 발생시키는지`() {
        // given
        val userId = 0L
        val amount = 100L
        val type = TransactionType.CHARGE

        // then
        assertThrows(IllegalArgumentException::class.java) {
            concurrencyPointService.process(userId, amount, type)
        }
    }

    @Test
    fun `process - amount가 0보다 작을 경우 IllegalArgumentException을 발생시키는지`() {
        // given
        val userId = 1L
        val amount = -1L
        val type = TransactionType.CHARGE

        // then
        assertThrows(IllegalArgumentException::class.java) {
            concurrencyPointService.process(userId, amount, type)
        }
    }

    @Test
    fun `process - 포인트 추가를 정상적으로 처리하는지`() {
        // given
        val userId = 1L
        val amount = 100L
        val type = TransactionType.CHARGE

        // when
        val userPoint = concurrencyPointService.process(userId, amount, type)

        // then
        assertEquals(amount, userPoint.point)
        assertEquals(1, pointHistoryTable.selectAllByUserId(userId).size)
    }

    @Test
    fun `process - 포인트 사용을 정상적으로 처리하는지`() {
        // given
        val userId = 1L
        val amount = 100L
        val type = TransactionType.CHARGE
        concurrencyPointService.process(userId, amount, type)

        // when
        val useAmount = 50L
        val useType = TransactionType.USE
        val userPoint = concurrencyPointService.process(userId, useAmount, useType)

        // then
        assertEquals(amount - useAmount, userPoint.point)
        val pointHistories = pointHistoryTable.selectAllByUserId(userId)
        assertEquals(2, pointHistories.size)
        assertEquals(useAmount, pointHistories[1].amount)
    }

    @Test
    fun `process - 포인트 사용 시 포인트가 부족할 경우 NotEnoughPointException을 발생시키는지`() {
        // given
        val userId = 1L
        val amount = 100L
        val type = TransactionType.CHARGE
        concurrencyPointService.process(userId, amount, type)

        // when
        val useAmount = 150L
        val useType = TransactionType.USE

        // then
        assertThrows(NotEnoughPointException::class.java) {
            concurrencyPointService.process(userId, useAmount, useType)
        }
        assertEquals(amount, userPointTable.selectById(userId).point)
        assertEquals(1, pointHistoryTable.selectAllByUserId(userId).size)
    }
}
