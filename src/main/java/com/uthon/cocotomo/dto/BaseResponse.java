package com.uthon.cocotomo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {
    private int status;
    private String message;

    public static BaseResponse success(String message) {
        return new BaseResponse(200, message);
    }

    public static BaseResponse error(int status, String message) {
        return new BaseResponse(status, message);
    }

    public static BaseResponse badRequest(String message) {
        return new BaseResponse(400, message);
    }

    public static BaseResponse notFound(String message) {
        return new BaseResponse(404, message);
    }

    public static BaseResponse forbidden(String message) {
        return new BaseResponse(403, message);
    }

    public static BaseResponse conflict(String message) {
        return new BaseResponse(409, message);
    }

    public static BaseResponse internalServerError(String message) {
        return new BaseResponse(500, message);
    }
}