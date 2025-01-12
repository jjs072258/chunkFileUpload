package com.example.fileupload.repository;

import com.example.fileupload.vo.FileUploadVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface UploadMapper {
    FileUploadVO selectFile();
}
