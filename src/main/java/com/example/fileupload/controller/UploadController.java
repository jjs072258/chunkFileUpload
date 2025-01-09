package com.example.fileupload.controller;

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

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/upload")
    public String uploadGet() {
        return "upload";
    }

    @PostMapping("/upload")
    @ResponseBody
    //@ResponseBody 어노테이션이 붙은 메서드의 반환 값이 Map이면, 이를 자동으로 JSON으로 변환하여 응답 본문(body)에 담아 보냅니다.
    public Map<String,Object> uploadPost(@RequestParam("file") MultipartFile chunkData,
                          @RequestParam("currentChunkPos") int currentChunkPos,
                          @RequestParam("totalChunk") int totalChunk,
                          @RequestParam("fileName") String fileName
    ) throws IOException {

        Map<String,Object> response = new HashMap<>();

        String tempFileName = fileName +".part"+currentChunkPos;
        File tempFile = new File(TEMP_DIR,tempFileName);

        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

//        try 블록의 괄호 () 안에 선언된 리소스(여기서는 InputStream과 FileOutputStream)는 try 블록이 종료될 때 자동으로 close() 메서드가 호출됩니다.
        try (InputStream in = chunkData.getInputStream();
             FileOutputStream out = new FileOutputStream(tempFile);
        ){
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1){
                out.write(buffer,0,bytesRead);
            }
        }catch (Exception e){
            response.put("success", false);
            response.put("message", "Chunk uploaded fail!");
            return response;
        }

        if(currentChunkPos == totalChunk - 1){
            File realFile = new File(REAL_DIR,fileName);
            File readDir = new File(REAL_DIR);
            if (!readDir.exists()) {
                readDir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(realFile)){
                for(int i=0;i<totalChunk;i++){
                    File part = new File(tempDir,fileName +".part"+i);
                    try (InputStream fis = Files.newInputStream(part.toPath())) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }

            tempFile.delete();
        }

        response.put("success", true);
        response.put("message", "Chunk uploaded successfully.");
        return response;
    }

}
