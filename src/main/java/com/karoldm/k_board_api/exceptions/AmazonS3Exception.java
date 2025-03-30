package com.karoldm.k_board_api.exceptions;

public class AmazonS3Exception extends RuntimeException {
    public AmazonS3Exception(String message){
        super(message);
    }
}
