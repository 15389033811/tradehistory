package com.example.demo.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 接口返回数据格式
 */
@ApiModel(value = "接口返回数据结构", description = "接口返回数据结构")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CODE_FAIL = -1;

    public static final int CODE_OK = 0;

    /**
     * 返回处理消息
     */
    @ApiModelProperty(value = "返回处理消息")
    private String message = "ok";

    /**
     * 返回代码
     */
    @ApiModelProperty(value = "返回代码")
    private Integer code = 0;

    /**
     * 返回数据对象 data
     */
    @ApiModelProperty(value = "返回数据对象")
    private T result;

    /**
     * 时间戳
     */
    @ApiModelProperty(value = "时间戳")
    private long timestamp = System.currentTimeMillis();

    public Result() {
        setTimestamp(System.currentTimeMillis());
    }

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.setCode(CODE_OK);
        return r;
    }


    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(CODE_OK);
        r.setResult(data);
        return r;
    }


    public static <T> Result<T> ok(String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(CODE_OK);
        r.setMessage(msg);
        r.setResult(data);
        return r;
    }

    public static <T> Result <T> failWithMsg(String msg) {
        return fail(CODE_FAIL, msg);
    }

    public static <T> Result <T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
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

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Result{" + "message='" + message + '\'' + ", code=" + code + ", result=" + result + ", timestamp=" + timestamp + '}';
    }
}