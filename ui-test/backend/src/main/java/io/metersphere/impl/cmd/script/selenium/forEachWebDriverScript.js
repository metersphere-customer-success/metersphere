try {
    var collection_#{index} = "${target}";
    if (collection_#{index}.startsWith("${") && collection_#{index}.endsWith("}")) {
        var variableName = /\\$\\{(.*)\\}/.exec("${target}")[1];
        collection_#{index} = variablesGet(variableName);
    } else {
        //运行时处理了双引号为单引号，JSON.parse 只能识别双引号这里再转换一次
        collection_#{index} = collection_#{index}.replace(/\\'/g, "\"");
        collection_#{index} = JSON.parse(collection_#{index});
    }
}catch (e){}

for (var _i_#{index} = 0; collection_#{index} && _i_#{index} <= collection_#{index}.length - 1; _i_#{index}++) {
    var item = collection_#{index}[_i_#{index}];
    if(typeof(item) === "string"){
        item = variableParsing(item);
    }
    var value = variableParsing("${value}");
    // vars.putObject("${" + value + "}", item);
    variablesSet(value, item);
}
