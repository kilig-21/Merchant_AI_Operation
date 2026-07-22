package org.example.merchant_ai_operation.common;




//全局异常类

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// GlobalExceptionHandler 负责把后端抛出的异常统一转换成 ApiResponse JSON
//@RestControllerAdvice意思是：这是一个全局 Controller 异常处理器。Controller 或它后面调用的代码抛异常时，它可以统一接住。
//全局异常处理器，统一拦截所有 @RestController 抛出的异常，封装统一返回体（就是你上面的 GlobalExceptionHandler）
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //精确处理业务问题:在业务代码中抛出
    @ExceptionHandler(value = BizException.class)
    public ApiResponse<?> handleBizException(BizException ex){
        return ApiResponse.error(ex.getCode(),ex.getMessage());
    }

    //参数异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
                        //getBindingResult():拿到这次校验的结果，里面包含哪些字段错了
        String message=ex.getBindingResult()
                //.getFieldErrors():拿到字段错误列表。比如可能有：
                .getFieldErrors()
                //这一步拿到的东西类似：
                //[
                //  FieldError(field="name", rejectedValue="", defaultMessage="名称不能为空"),
                //  FieldError(field="quantity", rejectedValue=0, defaultMessage="数量必须大于等于1")
                //]

                //把这个错误列表变成流水线，方便一步步处理。
                //原来是：List<FieldError>变成：Stream<FieldError>
                //数据本身没变，只是方便继续写：
                .stream()

                //先取第一个错误。因为现在我们先简单返回一个错误提示，不一次性返回全部。
                //为什么是 Optional?因为它有可能一个错误都没有，所以 Java 用 Optional 表示“可能有，也可能没有”。
                .findFirst()

                //如果拿到了第一个错误，就取它的默认提示文字。这个提示文字来自 DTO 里写的：
                    //@NotBlank(message = "名称不能为空")
                    //@Min(value = 1, message = "数量必须大于等于1")
                .map(error->error.getDefaultMessage())


                //如果极端情况下没拿到错误信息，就给一个兜底提示。
                .orElse("参数错误");
        return ApiResponse.error(400,message);

    }

    //兜底异常
    @ExceptionHandler(value = Exception.class)
    public ApiResponse<?> handleException(Exception ex){
        log.error("Unhandled exception", ex);
        return ApiResponse.error(500, "系统异常，请稍后再试");
    }


}
