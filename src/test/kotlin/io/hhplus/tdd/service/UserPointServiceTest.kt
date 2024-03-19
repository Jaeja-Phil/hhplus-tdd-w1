package io.hhplus.tdd.service

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
}