function getFile(value){
    // 获取文本框dom
    var textBox= document.querySelector(".file-name");
    // 获取上传控件dom
    var upload = document.getElementById('upload');
    // 获取文件名
    var file = upload.files[0];
    // 将文件名载入文本框
    textBox.innerHTML = file.name;

    console.log(file);
    console.log(file.name);
}

let selected = '';
function handleSelected(val) {
    selected = val;
}

async function onSubmit() {
    if (!this.selected) {
        alert('请先选择交易所')
    }
    let upload = document.getElementById('upload');
    let file = upload.files[0];
    let formData = new FormData();
    formData.append('file', file);
    formData.append('market',this.selected)
    const res = await fetch('/upload', {
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
