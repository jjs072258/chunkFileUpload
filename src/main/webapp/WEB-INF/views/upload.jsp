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
        fileID : "",
        fileSize : 0,
        chunkSize : 0,
        chunkCount : 0,
        chunkPosition : 0
    };

    function uploadFileCheck(file){
        const data = {originalFileName:file.name ,originalFileSize : file.size,registrationID : "jisung0509"};
        // 업로드할 파일 체크
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
                    //TODO - filepath도 가져와야하나?
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

    //업로드 프로세스
    //청크위치 , 청크사이즈 ,
    function uploadProcess(uploadFileItem){
        // 업로드 파일 시작 위치 (Byte)
        const startPos = uploadFileItem.chunkPosition * uploadFileItem.chunkSize;
        // 업로드 파일 종료 위치 (Byte)
        const endPos = Math.min(uploadFileItem.file.size , startPos + uploadFileItem.chunkSize);
        // 청크 데이터
        let chunkData = uploadFileItem.file.slice(startPos, endPos);

        const formData = new FormData();
        formData.append('fileID',uploadFileItem.fileID);
        formData.append('chunkPosition',uploadFileItem.chunkPosition);
        formData.append('chunkData',chunkData);
        formData.append('registrationID','jisung0509');

        $.ajax({
            url: '/upload',
            type: 'POST',
            data: formData,
            async: false,
            contentType: false, // FormData 객체를 사용하면 브라우저가 Content-Type을 multipart/form-data로 올바르게 설정
            processData: false,
            success: function(response) {
                uploadFileItem.chunkPosition = response.chunkPosition;
                uploadFileItem.chunkCount = response.chunkCount;
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
            }

        })
    });


</script>
</html>