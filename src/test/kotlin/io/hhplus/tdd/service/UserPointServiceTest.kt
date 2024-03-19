package io.hhplus.tdd.service

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.error.NotEnoughPointException
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UserPointServiceTest {
    private lateinit var userPointService: UserPointService
    private lateinit var userPointTable: UserPointTable

    @BeforeEach
    fun setUp() {
        userPointTable = UserPointTable()
        userPointService = UserPointService(userPointTable)
    }

    private fun checkUserPoint(userPoint: UserPoint, id: Long, point: Long) {
        assertEquals(id, userPoint.id)
        assertEquals(point, userPoint.point)
    }

    @Test
    fun `getUserPoint - 존재하지 않는 유저의 포인트를 조회할 시 0포인트의 UserPoint를 반환하는지`() {
        // given
        val userId = 1L

        // when
        val userPoint = userPointService.getUserPoint(userId)

        // then
        checkUserPoint(userPoint, userId, 0)
    }

    @Test
    fun `getUserPoint - 존재하는 유저의 포인트를 조회할 시 해당 UserPoint를 반환하는지`() {
        // given
        val userId = 1L
        val amount = 100L
        userPointTable.insertOrUpdate(userId, amount)

        // when
        val userPoint = userPointService.getUserPoint(userId)

        // then
        checkUserPoint(userPoint, userId, amount)
    }

    @Test
    fun `addUserPoint - 존재하지 않는 유저의 포인트를 추가할 시 해당 UserPoint를 반환하는지`() {
        // given
        val userId = 1L
        val amount = 100L

        // when
        val userPoint = userPointService.addUserPoint(userId, amount)

        // then
        checkUserPoint(userPoint, userId, amount)
    }

    @Test
    fun `addUserPoint - 존재하는 유저의 포인트를 추가할 시 해당 UserPoint를 반환하는지`() {
        // given
        val userId = 1L
        val amount = 100L
        userPointTable.insertOrUpdate(userId, amount)

        // when
        val userPoint = userPointService.addUserPoint(userId, amount)

        // then
        checkUserPoint(userPoint, userId, amount * 2)
    }

    @Test
    fun `removeUserPoint - 존재하지 않는 유저의 포인트를 제거할 시 에러를 반환하는지`() {
        // given
        val userId = 1L
        val amount = 100L

        // when & then
        assertThrows(NotEnoughPointException::class.java) {
            userPointService.removeUserPoint(userId, amount)
        }
    }

    @Test
    fun `removeUserPoint - 존재하는 유저의 현재 포인트보다 많은 포인트를 제거할 시 에러를 반환하는지`() {
        // given
        val userId = 1L
        val amount = 100L
        userPointTable.insertOrUpdate(userId, amount)

        // when & then
        assertThrows(NotEnoughPointException::class.java) {
            userPointService.removeUserPoint(userId, amount + 1)
        }
    }

    @Test
    fun `removeUserPoint - 존재하는 유저의 포인트를 알맞게 차감한 UserPoint를 반환하는지`() {
        // given
        val userId = 1L
        val amount = 100L
        userPointTable.insertOrUpdate(userId, amount)

        // when
        val userPoint = userPointService.removeUserPoint(userId, amount)

        // then
        checkUserPoint(userPoint, userId, 0)
    }

    @ParameterizedTest(
        name = "유저의 포인트를 {1}만큼 추가할 시 {1}만큼의 포인트를 가진 UserPoint를 반환하는지"
    )
    @CsvSource(
        "100, 100, 0",
        "100, 50, 50",
        "200, 50, 150",
        "100, 0, 100"
    )
    fun `removeUserPoint - 존재하는 유저의 포인트를 알맞게 차감한 UserPoint를 반환하는지 - parameterized test`(
        initialAmount: Long,
        removeAmount: Long,
        leftAmount: Long
    ) {
        // given
        val userId = 1L
        userPointTable.insertOrUpdate(userId, initialAmount)

        // when
        val userPoint = userPointService.removeUserPoint(userId, removeAmount)

        // then
        checkUserPoint(userPoint, userId, leftAmount)
    }
}