package com.connectsphere.media.controller;

import com.connectsphere.media.dto.ApiMessageResponse;
import com.connectsphere.media.exception.BadRequestException;
import com.connectsphere.media.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiMessageResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiMessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiMessageResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new ApiMessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiMessageResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiMessageResponse("Photos and videos must be 50 MB or smaller."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiMessageResponse> handleMultipartException(MultipartException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiMessageResponse("The upload could not be processed. Please try a supported file under 50 MB."));
    }
}
