const limit = 500;
const contentInput = document.querySelector('.content-input');

//새로고침되면서 글자수 세기 작동(수정폼일 경우 필요)
document.addEventListener('DOMContentLoaded', function () {
    const textCount = document.querySelector('.text-count');
    countBytes(contentInput, textCount, limit);
});

function wordCount(textarea) {
    const textCount = document.querySelector('.text-count');
    countBytes(textarea, textCount, limit);
}

const submitButton = document.querySelector('.submit-button');
if (submitButton) {

    submitButton.addEventListener('click', () => {
        handleSubmit("/board/write", "POST")
    });
}
const editButton = document.querySelector('.edit-button');
if (editButton) {

    const boardId = document.querySelector('.board-id').value;

    editButton.addEventListener('click', () => {
        handleSubmit('/board/edit/' + boardId, 'PUT');
    });
}

function handleSubmit(url, method) {

    const formData = new FormData(document.querySelector('.board-form'));

    const tags = document.querySelectorAll('.hash-tag-list .hash-tag-button');
    tags.forEach(tag=>{
        formData.append('tags',tag.textContent);
    });

    const imageList = document.querySelectorAll('.image-item');

    if(imageList.length<=1){
        alert("한 장 이상의 사진을 첨부해주세요.");
        return;
    }

    for (let i = 1; i < imageList.length; i++) {
        const file = imageList[i].file;
        if (file) {
            formData.append('images', file);
        }else{
            formData.append('imageUrls',imageList[i].getAttribute("value"));
        }
    }

    fetch(url, {
        method: method,
        body: formData,
    }).then(response => {
        if (!response.ok) {
            console.log("실패");
        } else {
            return response.text();
        }
    }).then(url => {
        window.location.replace(url);
    }).catch(error => {
        console.error(error);
    });
}