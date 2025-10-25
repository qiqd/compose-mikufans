package com.mikufans.xmd.miku.entiry;


public class Result<T> {
    private T data;

    private String message;

    private Integer code;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        result.setCode(200);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setMessage(message);
        result.setData(data);
        result.setCode(200);
        return result;
    }


    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setMessage(message);
        result.setCode(500);
        return result;
    }

    public static <T> Result<T> error(String message, Integer code) {
        Result<T> result = new Result<>();
        result.setMessage(message);
        result.setCode(code);
        return result;
    }

    public static <T> Result<T> error(T data, String message, Integer code) {
        Result<T> result = new Result<>();
        result.setData(data);
        result.setMessage(message);
        result.setCode(code);
        return result;
    }
}
