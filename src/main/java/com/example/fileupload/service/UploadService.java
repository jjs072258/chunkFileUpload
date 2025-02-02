package com.example.fileupload.service;

import com.example.fileupload.repository.UploadMapper;
import com.example.fileupload.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UploadService {

    @Autowired
    private UploadMapper uploadMapper;

    public FileUploadVO uploadFileCheck(FileUploadVO vo){
        return uploadMapper.selectFile(vo);
    }

    public boolean addUploadFileInfo(FileUploadVO vo){
        return uploadMapper.insertUploadInfo(vo) == 1;
    }

    public FileUploadVO getTempUplopadFile(FileUploadVO vo){
        return uploadMapper.selectTempUplopadFile(vo);
    }

    public boolean updateTempUplopadFile(FileUploadVO vo){
        return uploadMapper.updateTempUploadFile(vo) == 1;
    }

    public boolean deleteTempUplopadFile(FileUploadVO vo){
        return uploadMapper.deleteTempUploadFile(vo) == 1;
    }
}
