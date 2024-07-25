var path = require("path");
var fs = require("fs");
var dirs = [];
var pathName = "D:/metersphere/backend/src/main/java/io/metersphere/xpack/ui/impl/cmd/definition/";
var result = [];
const request = require("request");
const http = require("http");
var webdriver = require('selenium-webdriver');
var chrome = require('selenium-webdriver/chrome');
let By = require("selenium-webdriver")
var options = new chrome.Options();
options.setChromeBinaryPath("C:\Program Files\Google\Chrome\Application\chrome.exe");
var driver = new webdriver.Builder()
    .forBrowser('chrome')
    .build();

async function convert() {
    fs.readdir(pathName, function (err, files) {
        iterator(0, files);
    });
}
function translate(kw) {
    return new Promise(async resolve => {
        await driver.get(`http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=${kw}`)
        let ps = await driver.getPageSource();
        let pre = await driver.findElement(By.By.tagName("pre"));
        let r = await pre.getText();
        resolve(JSON.parse(r).translateResult[0][0].tgt);
    })
}



function jianfanti(kw) {

    return new Promise(async (resolve) => {
        await driver.get(`http://www.aies.cn/`)
        var txt = await driver.findElement(By.By.id("txt"));
        txt.sendKeys(kw);
        var btn = await driver.findElement(By.By.css("input[value='简体转繁体']"));
        btn.click();
        let r = await txt.getAttribute("value");
        resolve(r);
    });
}

function splitUpperCase(str) {
    if (!str)
        return str;
    let arr = str.split('');
    let r = "";
    for (let i = 0; i < arr.length; i++) {
        if (/[A-Z]/.test(arr[i])) {
            r += " " + arr[i];
        } else {
            r += arr[i];
        }
    }
    return r;
}

async function iterator(i, files) {
    if (!files || !files.length || i == files.length) {
        driver.quit();
        return;
    }
    fs.stat(path.join(pathName, files[i]), async function (err, data) {
        if (data.isFile()) {
            dirs.push(files[i]);
        }
        var json = JSON.parse(fs.readFileSync(pathName + files[i], "utf-8"));
        json.sort = 1;
        if (json.target) {

            json.targetCNName = await translate(splitUpperCase(json.target));

            json.targetTWName = await jianfanti(json.targetCNName);
        }

        if (json.value) {
            json.valueCNName = await translate(splitUpperCase(json.value));;
            json.valueTWName = await jianfanti(json.valueCNName);
        }
        if (json.cnName) {
            json.twName = await jianfanti(json.cnName);
        }

        if (json.cnDesc) {
            json.twDesc = await jianfanti(json.cnDesc);
        }

        if (json.cnType) {
            json.twType = await jianfanti(json.cnType);
        }
        fs.writeFile(`C:\\Users\\Administrator\\Desktop\\definition\\${files[i]}`, JSON.stringify(json), err => {
            if (err) {
                console.error(err)
                return
            }
            //文件写入成功。
        })
        iterator(i + 1, files);
    });
}

convert();