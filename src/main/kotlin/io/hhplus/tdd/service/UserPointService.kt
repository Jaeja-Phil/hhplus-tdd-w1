package io.hhplus.tdd.service

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.error.NotEnoughPointException
import io.hhplus.tdd.point.UserPoint
import org.springframework.stereotype.Service

@Service
class UserPointService(
    private val userPointTable: UserPointTable
) {
    fun getUserPoint(userId: Long): UserPoint {
        return userPointTable.selectById(userId)
    }

    fun addPoint(userId: Long, amount: Long): UserPoint {
        // 유저의 현재 포인트를 조회하고 요청한 값 만큼의 현재포인트에 더한다.
        return userPointTable.selectById(userId).let {
            userPointTable.insertOrUpdate(userId, it.point + amount)
        }
    }

    fun usePoint(userId: Long, amount: Long): UserPoint {
        // 유저의 현재 포인트를 조회하고 요청한 값 만큼의 현재포인트를 뺀다.
        // 만약 현재 포인트가 요청한 값보다 적다면 NotEnoughPointException을 발생시킨다.
        return userPointTable.selectById(userId).let {
            if (it.point < amount) {
                throw NotEnoughPointException()
            }
            userPointTable.insertOrUpdate(userId, it.point - amount)
        }
    }
}
