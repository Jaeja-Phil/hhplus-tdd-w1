package io.hhplus.tdd.point

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.service.PointHistoryService
import io.hhplus.tdd.service.UserPointService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@AutoConfigureMockMvc
@SpringBootTest
class PointControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var pointController: PointController


    @Autowired
    private lateinit var pointHistoryService: PointHistoryService

    @Autowired
    private lateinit var userPointTable: UserPointTable

    @Test
    fun `포인트 조회`() {
        // given
        val userId = 1L
        val url = "/point/$userId"

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url))

        // then
        resultActions.andExpect { status().isOk }
            .andExpect { jsonPath("$.id").value(1) }
            .andExpect { jsonPath("$.point").value(0) }
            .andExpect { jsonPath("$.updateMillis").isNumber }
            .andExpect { jsonPath("$.*").doesNotExist() }
            .andDo { print() }
    }

    @Test
    fun `존재하는 유저의 포인트 조회`() {
        // given
        val userId = 1L
        val amount = 100L
        userPointTable.insertOrUpdate(userId, amount)
        val url = "/point/$userId"

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url))

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(amount))
            .andExpect(jsonPath("$.updateMillis").isNumber)
            .andDo(print())
    }

    @Test
    fun `포인트 충전`() {
        // given
        val userId = 1L
        val amount = 100L
        val url = "/point/$userId/charge"
        val request = """
            {
                "amount": $amount
            }
        """.trimIndent()

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType("application/json")
            .content(request))

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(amount))
            .andExpect(jsonPath("$.updateMillis").isNumber)
            .andDo(print())
    }

    @Test
    fun `존재하는 유저의 포인트 충전`() {
        // given
        val userId = 1L
        val amount = 100L
        userPointTable.insertOrUpdate(userId, amount)
        val url = "/point/$userId/charge"
        val request = """
            {
                "amount": $amount
            }
        """.trimIndent()

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType("application/json")
            .content(request))

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(amount * 2))
            .andExpect(jsonPath("$.updateMillis").isNumber)
            .andDo(print())
    }

    @Test
    fun `잘못된 유저 아이디로 포인트 충전 시도`() {
        // given
        val userId = 0L
        val amount = 100L
        val url = "/point/$userId/charge"
        val request = """
            {
                "amount": $amount
            }
        """.trimIndent()

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType("application/json")
            .content(request))

        // then
        resultActions.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
            .andDo(print())
    }

    @Test
    fun `포인트 사용`() {
        // given
        val userId = 1L
        val amount = 100L
        val url = "/point/$userId/use"
        userPointTable.insertOrUpdate(userId, amount)
        val request = """
            {
                "amount": $amount
            }
        """.trimIndent()

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType("application/json")
            .content(request))

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(0))
            .andExpect(jsonPath("$.updateMillis").isNumber)
            .andDo(print())
    }

    @Test
    fun `잔여금액보다 큰 포인트 사용`() {
        // given
        val userId = 1L
        val amount = 100L
        val url = "/point/$userId/use"
        userPointTable.insertOrUpdate(userId, amount)
        val request = """
            {
                "amount": ${amount + 1}
            }
        """.trimIndent()

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType("application/json")
            .content(request))

        // then
        resultActions.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("포인트가 부족합니다."))
            .andDo(print())
    }

    @Test
    fun `사용자 포인트 히스토리 조회`() {
        // given
        val userId = 1L
        val amount = 100L
        val chargeAmount = 100L
        val useAmount = 50L
        val chargeType = TransactionType.CHARGE
        val useType = TransactionType.USE
        userPointTable.insertOrUpdate(userId, amount)
        pointHistoryService.addChargePointHistory(userId, chargeAmount)
        pointHistoryService.addUsePointHistory(userId, useAmount)
        val url = "/point/$userId/histories"

        // when
        val resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url))

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[0].type").value(chargeType.name))
            .andExpect(jsonPath("$[0].amount").value(chargeAmount))
            .andExpect(jsonPath("$[0].timeMillis").isNumber)
            .andExpect(jsonPath("$[1].userId").value(userId))
            .andExpect(jsonPath("$[1].type").value(useType.name))
            .andExpect(jsonPath("$[1].amount").value(useAmount))
            .andExpect(jsonPath("$[1].timeMillis").isNumber)
            .andDo(print())
    }
}