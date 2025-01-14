<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>대용량 파일 업로드</title>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
</head>
<body>
<p>대용량 파일 전송 개발을 위한 프로토타입 버전입니다.</p>
<p>1. 대용량 파일 업로드 - Blob</p>
<div id="uploadPanel1" class="uploadPanel">
    <form id="uploadForm1" method="post" enctype="multipart/form-data">
        <input type="file" name="file" id="file1">
    </form>
</div>
</body>
<script>
    const uploadFileItem = {

        file : null,
        fileID : null,
        fileSize : null,
        chunkSize : 0,
        chunkCount : 0,
        chunkPosition : 0
    };

    function uploadFileCheck(file){
        const data = {originalFileName:file.name ,originalFileSize : file.size};
        console.log(data);
        $.ajax({
            url: '/uploadFileCheck',
            type: 'POST',
            data : JSON.stringify(data),
            contentType: 'application/json',
            dataType: "json",
            async: false,
            success: function(response) {
                    uploadFileItem.file = file;
                    uploadFileItem.fileID = response.fileID;
                    uploadFileItem.fileSize = response.originalFileSize;
                    uploadFileItem.chunkSize = response.chunkSize;
                    uploadFileItem.chunkCount = response.chunkCount;
                    uploadFileItem.chunkPosition = response.chunkPosition;
                    uploadProcess(uploadFileItem);
            },
            error: function(request, status, error) {
                console.log("오류가 발생했습니다.");
            }
        });
    }

    $(document).ready(function(e) {

        $("#uploadForm1 > input[name=file]").change(function(e){
            const currentFile = e.target.files[0];

            if (currentFile){
                uploadFileCheck(currentFile);
                uploadProcess(uploadFileItem);
            }

        })
    });



    //업로드 프로세스
    //기존 filecheck를 통해서 업로드 정보를 가져옴
    function uploadProcess(uploadFileItem){
        // 업로드 파일 시작 위치 (Byte)
        const startPos = uploadFileItem.chunkPosition * uploadFileItem.chunkSize
        // 업로드 파일 종료 위치 (Byte)
        const endPos = Math.min(uploadFileItem.file.size , startPos + uploadFileItem.chunkSize);
        // 청크 데이터
        let chunkData = uploadFileItem.file.slice(startPos, endPos);

        const formData = new FormData();
        formData.append('fileID',uploadFileItem.fileID);
        formData.append('chunkPosition',uploadFileItem.chunkPosition);
        formData.append('chunkCount',uploadFileItem.chunkCount);
        formData.append('chunkData',chunkData);

        $.ajax({
            url: '/upload',
            type: 'POST',
            data: formData,
            async: false,
            contentType: false, // FormData 객체를 사용하면 브라우저가 Content-Type을 multipart/form-data로 올바르게 설정
            processData: false,
            success: function(response) {
                if(response.success){
                    uploadFileItem.chunkPosition++;
                    if(uploadFileItem.chunkPosition < uploadFileItem.chunkCount){
                        uploadProcess(uploadFileItem);
                    }else{
                        console.log("청크 업로드 완료");
                    }
                }
            },
            error: function(request, status, error) {
                console.log("오류가 발생했습니다.");
            }
        });
    }
</script>
</html>