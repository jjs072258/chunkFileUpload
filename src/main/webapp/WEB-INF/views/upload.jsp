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
    let uploadFileItem = null;

    function uploadCheck(uploadFileItem){

    }

    $(document).ready(function(e) {

        $("#uploadForm1 > input[name=file]").change(function(e){
            const currentFile = e.target.files[0];

            if (currentFile){
                uploadFile(currentFile);
            }

        })
    });

    function uploadFile(file){
        const CHUNK_SIZE = 1024 * 500; //500kb
        const totalChunk = Math.ceil(file.size/CHUNK_SIZE);
        let currentChunkPos = 0;

        uploadProcess(file,currentChunkPos,totalChunk,CHUNK_SIZE);
    }

    //업로드 프로세스
    function uploadProcess(file,currentChunkPos,totalChunk,CHUNK_SIZE){

        // 업로드 파일 시작 위치 (Byte)
        const startPos = currentChunkPos * CHUNK_SIZE
        // 업로드 파일 종료 위치 (Byte)
        const endPos = Math.min(file.size , startPos + CHUNK_SIZE);
        // 청크 데이터
        let chunkData = file.slice(startPos, endPos);

        const formData = new FormData();
        formData.append('fileName',file.name);
        formData.append('currentChunkPos',currentChunkPos);
        formData.append('totalChunk',totalChunk);
        formData.append('file',chunkData);

        $.ajax({
            url: '/upload',
            type: 'POST',
            data: formData,
            async: false,
            contentType: false, // FormData 객체를 사용하면 브라우저가 Content-Type을 multipart/form-data로 올바르게 설정
            processData: false,
            success: function(response) {
                if(response.success){
                    currentChunkPos++;
                    if(currentChunkPos < totalChunk){
                        uploadProcess(file,currentChunkPos,totalChunk,CHUNK_SIZE);
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