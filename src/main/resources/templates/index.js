function getFile(value){
    // 获取文本框dom
    var textBox= document.querySelector(".file-name");
    // 获取上传控件dom
    var upload = document.getElementById('upload');
    // 获取文件名
    var file = upload.files[0];
    // 将文件名载入文本框
    textBox.innerHTML = file.name;
}


async function onSubmit() {
    // 下拉框逻辑
    let myselect=document.getElementById("market-select");
    let index=myselect.selectedIndex;
    if (!index) {
        alert('请先选择交易所')
        return
    }
    const selected = myselect.options[index].value;

    let upload = document.getElementById('upload');
    let file = upload.files[0];
    let formData = new FormData();
    formData.append('file', file);
    formData.append('market',selected)
    const res = await fetch('http://www.traderhistory.top/api/upload', {
        method: 'POST',
        mode: 'cors', 
        body: formData,
    })
    const data = await res.json();
    let resText = document.querySelector('.text-box');
    resText.innerHTML = data.result;
}

function handleCopy() {
    const divText = document.querySelector('.text-box').innerText;
    const textarea = document.querySelector('#input');
    textarea.textContent = divText;
    textarea.select();
	document.execCommand('copy');
}
