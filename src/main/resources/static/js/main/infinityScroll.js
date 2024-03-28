window.onbeforeunload = function () {
    // 페이지를 새로 고침할 때만 실행할 작업을 여기에 작성
    window.scrollTo(0, 0); // 페이지의 맨 위로 스크롤
}

let pageNum = 1;

async function fetchImages(pageNum) {
    const selectedType = document.querySelector("#order-select").value;
    try {
        if (pageNum >= totalPage) {
            alert("더 이상 콘텐츠가 없습니다.");
            return;
        }
        let response;
        if (selectedType === "latest") {
            response = await fetch(`?page=${pageNum}&id=${lastBoardId}`, {
                method: "GET",
            });
        } else if (selectedType === "popularity") {
            response = await fetch(`?page=${pageNum}&sort=readCount&id=${lastBoardId}`, {
                method: "GET",
            });
        }

        if (!response.ok) {
            console.log("실패");
            return;
        }

        const html = await response.text();

        //main 마지막 자식으로 HTML을 추가하기
        document.querySelector('main').insertAdjacentHTML("beforeend", html);

    } catch (error) {
        console.error(error);
    }
}

function throttling(callback, delay) {
    let timer = null;
    return () => {
        if (timer === null) {
            timer = setTimeout(() => {
                const scrollHeight = document.documentElement.scrollHeight;
                const scrollTop = document.documentElement.scrollTop;
                const clientHeight = document.documentElement.clientHeight;
                if (scrollHeight - scrollTop <= clientHeight + 10) {
                    callback(pageNum++);
                }
                timer = null;
            }, delay);
        }
    }
}


const throttleTracker = throttling(fetchImages, 500);
window.addEventListener('scroll', throttleTracker);