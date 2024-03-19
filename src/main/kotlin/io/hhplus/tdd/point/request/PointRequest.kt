package io.hhplus.tdd.point.request

data class PointRequest(
    val amount: Long
) {
    /**
     * 필드가 1개밖에 없을 경우 빈 생성자를 만들지 않으면 에러가 발생:
     * ```
     * JSON parse error: Cannot construct instance of `io.hhplus.tdd.point.request.PointChargeRequest` (although at least
     * one Creator exists): cannot deserialize from Object value (no delegate- or property-based Creator)
     * ```
     */
    constructor() : this(0)
}
