package com.example.fileupload.controller;

import com.example.fileupload.service.UploadService;
import com.example.fileupload.vo.FileUploadVO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class UploadController {

    private static final String TEMP_DIR = "/tmp/uploads/";
    private static final String REAL_DIR = "/uploads/";
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private UploadService uploadService;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/upload")
    public String uploadGet() {
        return "upload";
    }

    @PostMapping("/uploadFileCheck")
    @ResponseBody
    public ResponseEntity<FileUploadVO> uploadFileCheck(@RequestBody FileUploadVO fileUploadVO) {
        FileUploadVO result = uploadService.uploadFileCheck(fileUploadVO);
        if(result == null){ // 새로운 파일
            String tempFileName = UUID.randomUUID().toString().replaceAll("-", "");
            int chunkSize = 102400;
            int chunkCount = (int)Math.ceil((double)fileUploadVO.getOriginalFileSize() / (double)chunkSize);

            FileUploadVO tempFile = new FileUploadVO();
            tempFile.setFileID(tempFileName);
            tempFile.setFilePath(TEMP_DIR+"/"+tempFileName);
            tempFile.setFileCategory("");
            tempFile.setOriginalFileName(fileUploadVO.getOriginalFileName());
            tempFile.setOriginalFileSize(fileUploadVO.getOriginalFileSize());
            tempFile.setChunkSize(chunkSize); // 100kb
            tempFile.setChunkCount(chunkCount);
            tempFile.setChunkPosition(0);
            tempFile.setRegistrationID("jisung0509");

            if(uploadService.addUploadFileInfo(tempFile)){
                return new ResponseEntity<>(tempFile,HttpStatus.OK);
            }else{
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/upload")
    @ResponseBody
    //파일아이디 , 현재청크위치 , 청크파일을 가져옴
    public Map<String, Object> uploadPost(FileUploadVO uploadVO) throws IOException {

        String fileID = uploadVO.getFileID();
        int chunkPosition = uploadVO.getChunkPosition();
        MultipartFile chunkData = uploadVO.getChunkData();

        //업로드 중인건지 확인
        FileUploadVO tempUploadFile = uploadService.getTempUplopadFile(uploadVO);
        if(tempUploadFile != null){
            if(chunkPosition == tempUploadFile.getChunkPosition()){
                String tempFileName =  tempUploadFile.getFileID()+".part"+tempUploadFile.getChunkPosition();
                File tempFile = new File(tempUploadFile.getFilePath(),tempFileName);
                try (InputStream in = chunkData.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempFile);
                ){
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1){
                        out.write(buffer,0,bytesRead);
                    }
                    chunkPosition++;
                    tempUploadFile.setChunkPosition(chunkPosition);
                    boolean success = uploadService.updateTempUplopadFile(tempUploadFile);
                    if(success){
                        Map<String, Object> response = new HashMap<>();
                        response.put("result", true);
                        response.put("fileID", fileID);
                        response.put("chunkSize", tempUploadFile.getChunkSize());
                        response.put("chunkCount", tempUploadFile.getChunkCount());
                        response.put("chunkPosition", tempUploadFile.getChunkPosition());
                        response.put("message", "청크 파일이 업로드 되었습니다.");

                        if(chunkPosition == tempUploadFile.getChunkCount()){
                            File realFile = new File(REAL_DIR,uploadVO.getFileID());
                            File readDir = new File(REAL_DIR);
                            if (!readDir.exists()) {
                                readDir.mkdirs();
                            }
                            try (FileOutputStream fos = new FileOutputStream(realFile)){
                                for(int i=0;i<tempUploadFile.getChunkCount();i++){
                                    File part = new File(tempUploadFile.getFilePath(),uploadVO.getFileID() +".part"+i);
                                    try (InputStream fis = Files.newInputStream(part.toPath())) {
                                        byte[] buffer2 = new byte[1024];
                                        int bytesRead2;
                                        while ((bytesRead2 = fis.read(buffer2)) != -1) {
                                            fos.write(buffer2, 0, bytesRead2);
                                        }
                                    }
                                }
                            }
                        }
                        return response;
                    }
                }catch (Exception e){
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "청크 파일 업로드를 실패하였습니다.");
                    return response;
                }
            }
        }
        return null;
    }

}
