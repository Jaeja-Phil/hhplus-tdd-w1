package io.hhplus.tdd.point

import io.hhplus.tdd.point.request.PointRequest
import io.hhplus.tdd.service.ConcurrencyPointService
import io.hhplus.tdd.service.PointHistoryService
import io.hhplus.tdd.service.UserPointService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    private val userPointService: UserPointService,
    private val pointHistoryService: PointHistoryService,
    private val concurrencyPointService: ConcurrencyPointService
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        return userPointService.getUserPoint(id)
    }

    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        return pointHistoryService.getPointHistories(id)
    }

    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody pointRequest: PointRequest
    ): UserPoint {
        return concurrencyPointService.process(id, pointRequest.amount, TransactionType.CHARGE)
    }

    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody pointRequest: PointRequest
    ): UserPoint {
        return concurrencyPointService.process(id, pointRequest.amount, TransactionType.USE)
    }
}