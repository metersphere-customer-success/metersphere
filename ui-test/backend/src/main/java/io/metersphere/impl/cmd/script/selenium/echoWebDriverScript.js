//var target = ""${target}"";
var target = "${target}";
target = variableParsing(target);
// 去除换行
target = target.replace(/[\r\n]/g,"");
//driver.executeScript("console.log(" + target + ")");
// 控制台显示内容
var result = (responseHeaders["${cmdId}"].body);
result = !result ? target : result + "\n" + target;
responseHeaders["${cmdId}"].body = result;