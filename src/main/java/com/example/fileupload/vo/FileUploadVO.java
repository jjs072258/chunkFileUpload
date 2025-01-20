package com.example.fileupload.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

@Data
public class FileUploadVO {
    private int seq;
    private String fileID;
    private String filePath;
    private String fileCategory;
    private String fileType;
    private String originalFileName;
    private long originalFileSize;
    private MultipartFile chunkData;
    private int chunkSize;
    private int chunkCount;
    private int chunkPosition;
    private String registrationID;
    private Date registrationDate;
    private Date modificationDate;
    private int status;
    private int fileIndex;
}
