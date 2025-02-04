package com.example.fileupload.controller;

import com.example.fileupload.service.UploadService;
import com.example.fileupload.vo.FileUploadVO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileUtils;
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

    private static final String TEMP_DIR = "/tmp/uploads";
    private static final String REAL_DIR = "/uploads/";
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private static final int CHUNK_SIZE = 1024 * 100;

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
            // 임시 파일 이름
            String tempFileName = UUID.randomUUID().toString().replaceAll("-", "");
            int chunkCount = (int)Math.ceil((double)fileUploadVO.getOriginalFileSize() / (double)CHUNK_SIZE); // 청크 수

            FileUploadVO tempFile = new FileUploadVO();
            tempFile.setFileID(tempFileName);
            tempFile.setFilePath(TEMP_DIR+"/"+tempFileName);
            tempFile.setFileCategory("");
            tempFile.setOriginalFileName(fileUploadVO.getOriginalFileName());
            tempFile.setOriginalFileSize(fileUploadVO.getOriginalFileSize());
            tempFile.setChunkSize(CHUNK_SIZE); // 100kb
            tempFile.setChunkCount(chunkCount);
            tempFile.setChunkPosition(0);
            tempFile.setFileIndex(fileUploadVO.getFileIndex());
            tempFile.setRegistrationID("jisung0509");

            if(uploadService.addUploadFileInfo(tempFile)){
                return new ResponseEntity<>(tempFile,HttpStatus.OK);
            }else{
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }else{ // 기존에 파일이 있으면
            result.setFileIndex(fileUploadVO.getFileIndex());
            return new ResponseEntity<>(result,HttpStatus.OK);
        }

    }

    @PostMapping("/uploadDelete")
    @ResponseBody
    public ResponseEntity<String> deleteFile(FileUploadVO fileUploadVO) {

        //업로드 중인건지 확인
        FileUploadVO tempUploadFile = uploadService.getTempUplopadFile(fileUploadVO);
        if (tempUploadFile != null) {
            // 파일 삭제
            File tempFile = new File(tempUploadFile.getFilePath());
            try {
                FileUtils.deleteDirectory(tempFile);
                System.out.println("폴더가 삭제되었습니다.");
            } catch (IOException e) {
                System.err.println("폴더 삭제 중 오류가 발생했습니다: " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
            }

            // 삭제
            boolean result = uploadService.deleteTempUplopadFile(fileUploadVO);
            if (result) {
                return new ResponseEntity<>("삭제 완료",HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/upload")
    @ResponseBody
    //파일아이디 , 현재청크위치 , 청크파일을 가져옴
    public ResponseEntity<FileUploadVO> uploadPost(FileUploadVO fileUploadVO) throws IOException {

        String fileID = fileUploadVO.getFileID();
        int chunkPosition = fileUploadVO.getChunkPosition();
        MultipartFile chunkData = fileUploadVO.getChunkData();

        boolean success = false;
        //업로드 중인건지 확인
        FileUploadVO tempUploadFile = uploadService.getTempUplopadFile(fileUploadVO);
        if(tempUploadFile != null){
            tempUploadFile.setFileIndex(fileUploadVO.getFileIndex());
            if(chunkPosition == tempUploadFile.getChunkPosition()){ // 업로드할 청크 위치가 맞으면
                String tempFileName =  tempUploadFile.getFileID()+".part"+tempUploadFile.getChunkPosition();
                File tempFile = new File(tempUploadFile.getFilePath(),tempFileName);
                File tempDir = new File(tempUploadFile.getFilePath());
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                try (InputStream in = chunkData.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempFile);
                ){
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1){
                        out.write(buffer,0,bytesRead);
                    }

                    chunkPosition++;
                    tempUploadFile.setChunkPosition(chunkPosition); //청크 포지션 증가
                    success = uploadService.updateTempUplopadFile(tempUploadFile);
                }catch (Exception e){
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                if(success){
                    //끝까지 업로드가 되었으면
                    if(chunkPosition == tempUploadFile.getChunkCount()){
                        File realFile = new File(REAL_DIR+"/"+tempUploadFile.getOriginalFileName());
                        File readDir = new File(REAL_DIR);
                        if (!readDir.exists()) {
                            readDir.mkdirs();
                        }
                        try (FileOutputStream fos = new FileOutputStream (realFile)){
                            for(int i=0;i<tempUploadFile.getChunkCount();i++){
                                // 임시폴더에 있는 파일 가져오기
                                File part = new File(tempUploadFile.getFilePath()+"/"+tempUploadFile.getFileID()+".part"+i);
                                try (InputStream fis = Files.newInputStream(part.toPath())) {
                                    byte[] buffer = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = fis.read(buffer)) != -1) {
                                        fos.write(buffer, 0, bytesRead);
                                    }
                                }
                            }
                        }
                        tempUploadFile.setStatus(4);
                        return new ResponseEntity<>(tempUploadFile,HttpStatus.OK);
                    }else{ //계속 진행
                        tempUploadFile.setStatus(1);
                        return new ResponseEntity<>(tempUploadFile,HttpStatus.OK);
                    }
                }else{
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
