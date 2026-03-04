package com.toy.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모든 API의 공통 JSON 응답 포맷
 *
 * <pre>
 * 성공: { "status": 200, "message": "성공", "data": { ... } }
 * 실패: { "status": 401, "message": "로그인이 필요합니다.", "data": null }
 * </pre>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;

    // 데이터 있는 성공 응답
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "성공", data);
    }

    // 메시지 + 데이터 함께 담는 성공 응답
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // 데이터 없는 성공 응답 (생성/삭제 완료 메시지 등) — ok(String)과의 제네릭 모호성 방지를 위해 success 사용
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    // 에러 응답
    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
