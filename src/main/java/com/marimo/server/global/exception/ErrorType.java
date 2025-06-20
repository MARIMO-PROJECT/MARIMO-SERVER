package com.marimo.server.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorType {

    /* 400 Bad Request */
    INVALID_PATH_ERROR(HttpStatus.BAD_REQUEST, 40001, "요청 경로의 변수 값이 허용된 형식과 다릅니다."),
    INVALID_FIELD_ERROR(HttpStatus.BAD_REQUEST, 40002, "요청 본문의 필드 값이 허용된 형식과 다릅니다."),
    NO_REQUEST_PARAMETER_ERROR(HttpStatus.BAD_REQUEST, 40003, "요청에 필요한 파라미터가 존재하지 않습니다."),
    NO_REQUEST_HEADER_ERROR(HttpStatus.BAD_REQUEST, 40004, "요청에 필요한 헤더가 존재하지 않습니다."),
    TYPE_MISMATCH_ERROR(HttpStatus.BAD_REQUEST, 40005, "유효하지 않은 값이 입력되었습니다."),
    INVALID_REQUEST_BODY_ERROR(HttpStatus.BAD_REQUEST, 40006, "유효하지 않은 Request Body입니다. 요청 형식 또는 필드를 확인하세요."),
    DATA_INTEGRITY_VIOLATION_ERROR(HttpStatus.BAD_REQUEST, 40007, "데이터 무결성 제약 조건을 위반했습니다."),
    INVALID_PRODUCT_TYPE_ERROR(HttpStatus.BAD_REQUEST, 40008, "유효하지 않은 productType입니다."),

    INVALID_INFORMATION_ERROR(HttpStatus.BAD_REQUEST, 40010, "올바른 정보를 입력해 주세요."),

    /* 404 Not Found */
    NOT_FOUND_PATH_ERROR(HttpStatus.NOT_FOUND, 40401, "존재하지 않는 경로입니다."),
    NOT_FOUND_IMAGE_PATH_ERROR(HttpStatus.NOT_FOUND, 40402, "존재하지 않는 이미지 경로입니다."),
    NOT_FOUND_INVITATION_ERROR(HttpStatus.NOT_FOUND, 40403, "존재하지 않는 청첩장 id입니다."),
    NOT_FOUND_PRE_VIDEO_ERROR(HttpStatus.NOT_FOUND, 40404, "존재하지 않는 식전영상 id입니다."),

    /* 500 Internal Server Error */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "예상치 못한 서버 에러가 발생했습니다."),
    FAILED_GET_PRESIGNED_URL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50002, "Presigned URL 획득에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
