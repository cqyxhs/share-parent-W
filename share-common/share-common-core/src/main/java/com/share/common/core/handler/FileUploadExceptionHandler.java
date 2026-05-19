package com.share.common.core.handler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.share.common.core.exception.file.FileUploadException;

@ControllerAdvice
public class FileUploadExceptionHandler {

    /**
     * 捕获 Spring 原生文件大小超限异常
     * 将其转换为您的业务异常体系
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleSpringSizeLimit(MaxUploadSizeExceededException ex) {
        // 将 Spring 异常包装为业务自定义异常（保留原始异常栈）
        FileUploadException customEx = new FileUploadException("文件大小超出系统限制", ex.getCause());
        return handleBusinessException(customEx);
    }

    /**
     * 统一处理业务层的文件上传异常
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<String> handleBusinessException(FileUploadException ex) {
        String userFriendlyMsg = "上传失败：" + ex.getMessage();
        // 可根据 ex.getCause()  类型细化不同错误提示
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(userFriendlyMsg);
    }
}
