package org.example.merchant_ai_operation.common;








//注意,ApiResponse主要用于封装统一的返回给前端的类
//主要三种:code meassage和 data,data就用泛型表示
//无码无对象带消息
public record  ApiResponse <T> (int code,String message,T data){
    public static <T> ApiResponse ok(T data){
        return new ApiResponse<>(0,"ok",data);
    }

    public static <T> ApiResponse error(String message){
        return new ApiResponse<>(0,"发生错误",null);
    }
 }
