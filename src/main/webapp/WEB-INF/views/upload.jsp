<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>대용량 파일 업로드</title>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
</head>
<style>
    .file-upload {
        position: relative;
        display: inline-block;
    }

    .custom-button {
        /* 사용자 정의 버튼 스타일 */
        padding: 10px 15px;
        background-color: #4CAF50;
        color: white;
        border: none;
        cursor: pointer;
    }

    #file1 {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        opacity: 0; /* 숨김 처리 */
        cursor: pointer;
    }

    /* 파일 목록 스타일 (필요에 따라 추가) */
    .file-item {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
    }

    .progress-bar-container {
        width: 200px;
        height: 20px;
        background-color: #eee;
        margin-left: 10px;
        margin-right: 10px;
        border-radius: 4px;
        overflow: hidden;
        position: relative;
    }

    .progress-bar {
        height: 100%;
        background-color: #4CAF50;
        color: white;
        text-align: center;
        line-height: 20px;
        width: 0%; /* 초기 너비 0 */
        transition: width 0.3s ease; /* 부드러운 애니메이션 효과 */
        position: absolute;
        top: 0;
        left: 0;
    }

    .start-button, .stop-button {
        margin-left: 5px;
    }
</style>
<body>
<p>대용량 파일 전송 개발을 위한 프로토타입 버전입니다.</p>
<p>1. 대용량 파일 업로드 - Blob</p>
<div id="uploadPanel1" class="uploadPanel">
    <div class="file-upload">
        <button type="button" class="custom-button">파일 선택</button>
        <input type="file" name="uploadFile" id="uploadFile" multiple hidden>
    </div>
    <div id="fileList">

    </div>
</div>
</body>
<script>
    const policy = {
        maxFileSize : 1024 * 1024 * 1024,
        allowFileType : [
            'video/mp4', //MP4
            'application/pdf', // PDF
            'application/msword', // DOC
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // DOCX
            'application/vnd.ms-excel', // XLS
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // XLSX
            'application/vnd.ms-powerpoint', // PPT
            'application/vnd.openxmlformats-officedocument.presentationml.presentation', // PPTX
            'text/plain', // TXT
            'text/csv', // CSV
            'image/png',  // PNG 추가
            'image/jpeg'  // JPEG 추가
        ]
    }
    const uploadFileList = [

    ];
    const uploadFileItem = {
        file : null,
        fileID : "",
        chunkSize : 0,
        chunkCount : 0,
        chunkPosition : 0,
        status: 0
    };

    $(document).ready(function(e) {
        let fileIndex = 0;

        $(".custom-button").on("click", function() {
            $("#uploadFile").click();
        });

        $("#uploadFile").change(function(e){
            e.preventDefault();
            e.stopPropagation();
            const selectedFiles = e.target.files;
            console.log(selectedFiles);
            // 선택된 각 파일에 대해 UI 생성 및 정보 표시
            for (let i = 0; i < selectedFiles.length; i++) {
                const selectedFile = selectedFiles[i];
                if(uploadFileCheck(selectedFile)){
                    addFileToList(selectedFile, fileIndex);
                    fileIndex++;
                }
            }
        });

        // 시작 버튼 클릭 이벤트
        $(document).on("click",".start-button",function() {
            const fileIndex = $(this).data("file-index");
            $(this).prop("disabled", true); // 시작 버튼 비활성화
            $(this).parent().find(".stop-button").prop("disabled", false); // 중지 버튼 활성화
            initialUpload(uploadFileList[fileIndex]);

        });
        // 중지 버튼 클릭 이벤트
        $(document).on("click",".stop-button",function() {
            const fileIndex = $(this).data("file-index");
            $(this).prop("disabled", true); // 중비 버튼 비활성화
            $(this).parent().find(".start-button").prop("disabled", false); // 중지 버튼 활성화
            // uploadFileCheck(file, fileIndex); // 해당 파일 업로드 시작
        });
    });

    function addFileToList(file,index){
        const fileListContainer = $("#fileList");
        const fileSizeInKB = (file.size/1024).toFixed(2); // 소수점 2번째자리까지 표시하고 반올림
        let fileListHtml = `
            <div class="file-item" id="file-\${index}">
                <span>\${file.name} (\${fileSizeInKB} KB)</span>
                <div class="progress-bar-container">
                    <div class="progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">0%</div>
                </div>
                <button class="start-button" data-file-index="\${index}">시작</button>
                <button class="stop-button" data-file-index="\${index}" disabled>중지</button>
            </div>
        `;
        uploadFileList.push(
            {
                fileIndex : index,
                file : file,
                fileID : "",
                chunkSize : 0,
                chunkCount : 0,
                chunkPosition : 0,
                status: 0
            }
        )
        fileListContainer.append(fileListHtml);

    }

    function uploadFileCheck(file){

        if(file.size > policy.maxFileSize){
            alert("업로드 가능한 사이즈를 초과했습니다.");
            return false;
        }
        if(!policy.allowFileType.includes(file.type)){
            alert("해당 파일 형식은 지원하지 않습니다.");
            return false;
        }
        return true;
    }

    function initialUpload(file){
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
                uploadFileItem.status = response.status;
                const progress = Math.floor((uploadFileItem.chunkPosition / uploadFileItem.chunkCount) * 100);
                updateProgressBar(index, progress);
                if(uploadFileItem.status == 3){
                    console.log("청크 업로드 완료");
                }else if(uploadFileItem.status == 2){
                    uploadProcess(uploadFileItem);
                }



            },
            error: function(request, status, error) {
                console.log("오류가 발생했습니다.");
            }
        });
    }

    function updateProgressBar(index, progress) {
        const progressBar = $(`#file-\${index}`).find(".progress-bar");
        progressBar.css("width", progress + "%").text(progress + "%");
        progressBar.attr("aria-valuenow", progress);
    }

</script>
</html>