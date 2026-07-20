package org.example.merchant_ai_operation.common;








//注意,ApiResponse主要用于封装统一的返回给前端的类
//主要三种:code meassage和 data,data就用泛型表示
//无码无对象带消息
public record  ApiResponse <T> (int code,String message,T data){
    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(0,"ok",data);
    }
    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>(500,message,null);
    }

    public static <T>ApiResponse<T> error(int code,String message){
        return new ApiResponse<>(code,message,null);
    }

    //code代码:
    //400 参数错误
    //401 未登录
    //403 没权限
    //409 业务冲突
    //500 系统错误

 }
