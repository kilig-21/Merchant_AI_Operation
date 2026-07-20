package org.example.merchant_ai_operation.common;




// BizException 表示业务规则不允许继续，例如库存不足、商品不存在、重复提交
//继承Exception那么他就是有一个异常类
public class BizException extends RuntimeException {//运行时错误


    private final int code;


    public BizException(String message){
        //如果你不传消息,那么就默认错误码就409;
        this(409,message);
    }
    public BizException(int code,String message){
        super(message);
        this.code=code;
    }
    public int getCode(){
        return code;
    }


}
