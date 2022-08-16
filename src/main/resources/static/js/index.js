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

async function onSubmit() {
    let upload = document.getElementById('upload');
    let file = upload.files[0];
    let formData = new FormData();
    formData.append('uploadFile', file);
    const res = await fetch('/upload', {
        method: 'POST',
        mode: 'cors', 
        body: formData,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        }
    })
    let resText = document.querySelector('.text-box');
    resText.innerHTML = res;
}
