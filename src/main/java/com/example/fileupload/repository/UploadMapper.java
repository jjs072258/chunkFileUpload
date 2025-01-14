package com.example.fileupload.repository;

import com.example.fileupload.vo.FileUploadVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.io.File;

@Mapper
public interface UploadMapper {
    FileUploadVO selectFile(FileUploadVO vo);

    int insertUploadInfo(FileUploadVO vo);

    FileUploadVO selectTempUplopadFile(FileUploadVO vo);

    int updateTempUploadFile(FileUploadVO vo);
}
