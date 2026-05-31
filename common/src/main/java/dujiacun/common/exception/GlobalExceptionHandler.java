package dujiacun.common.exception;

import dujiacun.common.error.ErrorCode;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import dujiacun.common.CommonResult;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<String> handleValidException(MethodArgumentNotValidException e) {
        //BindingResult 是Spring自带的,存储了所有参数验证失败的错误信息
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            message = bindingResult.getFieldError().getField() + ":" +
                      bindingResult.getFieldError().getDefaultMessage();
        }
        return CommonResult.error(ErrorCode.VALIDATE_FAILED.getCode(), "参数校验失败-->" + message);
    }

    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public CommonResult<String> handleBusinessException(BusinessException e) {
        return CommonResult.error("业务异常:" + e.getMessage());
    }

    @ResponseBody//将信息转换为JSON返回给前端
    @ExceptionHandler(Exception.class)
    public CommonResult<String> handleException(Exception e) {
        return CommonResult.error(ErrorCode.FAILED.getCode(), e.getMessage());
    }
}
