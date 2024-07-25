/***
 *
 * selenium
 * 全局预执行脚本
 */
var pkg = JavaImporter(org.openqa.selenium);
var support_ui = JavaImporter(org.openqa.selenium.support.ui.WebDriverWait);
var action_import = JavaImporter(org.openqa.selenium.interactions.Actions);
var select_import = JavaImporter(org.openqa.selenium.support.ui.Select);
var conditions = org.openqa.selenium.support.ui.ExpectedConditions;
var keyAction = org.openqa.selenium.Keys;
var platform = org.openqa.selenium.Platform;
var vars = org.apache.jmeter.threads.JMeterContextService.getContext().getVariables();
var timeunit = java.util.concurrent.TimeUnit;
load("${base64Url}");
load("${mockJsUrl}");
var errorMsg = "";
var exception = null;
// 脚本表达式参数
var args = [];

// 弹框相关
var nextAlertStatus = "";
var alertResultText = "";
var answerPromptContent = "";

//兼容纯 js 库的写法
var driver = WDS.browser;
var By = pkg.By;
var cmdName = null;
var customName = null;
var singleCommandConfig = null;
var actionProvider = new action_import.Actions(driver);
var platformName = driver.capabilities.getPlatformName();
WDS.log.info("当前操作系统:"+platformName)
// 等待元素超时超时时间 15 秒
var timeout = 15000;
var wait = getWaitObject(Math.floor(timeout / 1000));

var commandConfig = ${CommandConfig};
// 截图路径(默认 Jmeter 的 bin 目录或用户目录，否则可能会有权限问题)
var screenShotPath = commandConfig.screenShotAddress;
var pageLoadTimeOut = commandConfig.secondsWaitWindowOnLoad;
// 报告 ID
var reportId = commandConfig.reportId;

// 全局变量定义
var storedVars = new java.util.HashMap();

// 当前的窗口句柄数
var preHandlesCount = 0;

// 当前的窗口句柄
var preHandle = 0;

// 上一次操作之后的窗口句柄数
var lastHandlesCount = 0;

// 上一次操作之后的窗口句柄
var lastHandle = 0;


// urlPattern
var urlPattern = /^https?:\/\//;

//全局的系统分辨率，因为每一次headless模式打开新窗口分辨率都会复原成1366，所以每次切换窗口都设置一次分辨率保证无头模式和gui模式体验一致
var resolutionRatioX = commandConfig.resolutionRatioX;
var resolutionRatioY = commandConfig.resolutionRatioY;

// 全局函数注入点
${injectFunction}
${environmentFunction}
//类型处理相关函数
${injectTypeFunction}

var scopeStoreOrScriptVars = new java.util.HashMap();
//是否需要切换窗口句柄
var shouldSwitchHandle = true;
// 全局变量设置
function variablesSet(key, value) {
	storedVars.put(key, value);
	scopeStoreOrScriptVars.put(key, value);
}

// scope > global > local > env
// since v2.4 步骤中的提取变量 优先级 高于  变量入参（场景变量）
//global是数据提取里的变量
//local是场景变量
function getVariablesTarget(key, local, scope, global){
    if(scope && scope.containsKey(key) && scope.get(key) != null){
       return scope;
    }

    if(global && global.containsKey(key) && global.get(key) != null){
        return global;
    }

    if(local && local.containsKey(key) && local.get(key) != null){
        return local;
    }

    return getEnvVars();
}

//获取场景变量
function getEnvVars(){
    if(!currentEnvId || !envVars[currentEnvId]){
        return new java.util.HashMap();
    }
    return envVars[currentEnvId];
}

// 全局变量解析 先取局部变量表
function variablesGet(key) {
var _localVars = programRegistryStack[programRegistry] ? localVars[programRegistryStack[programRegistry]] : localVars[currentCmdLineId];
if(!_localVars){
    _localVars = new java.util.HashMap();
}

if (!scopeStoreOrScriptVars.get(key) && !_localVars.get(key) && !storedVars.get(key) && !getEnvVars().get(key)) {
 if (/(\.)/.exec(key)) {
   //a.b , a[0].b, a.b[3].c
   var propertyAccess = /([^.\n]+)\.(.*)/.exec(key)
   //局部、全局 变量处理
   var __target__ = getVariablesTarget(propertyAccess[1], _localVars, scopeStoreOrScriptVars,storedVars);

   if (__target__.containsKey(propertyAccess[1])) {
     //a.b, a.b[0].c
     var r3 = getPropertyValue(
       __target__.get(propertyAccess[1]),
       propertyAccess[2]
     )
     return r3
   } else if (/([^.\n]+)\[(\d*)\]/.exec(propertyAccess[1])) {
     //a[0].b
     var arrayAccess = /([^.\n]+)\[(\d*)\]/.exec(propertyAccess[1])
     //__target__ = _localVars.containsKey(arrayAccess[1]) ? _localVars : storedVars;
     __target__ = getVariablesTarget(arrayAccess[1], _localVars, scopeStoreOrScriptVars, storedVars);
     if (__target__.containsKey(arrayAccess[1])) {
       var r3 = getPropertyValue(
         __target__.get(arrayAccess[1])[arrayAccess[2]],
         propertyAccess[2]
       )
       return r3
     }
   }
 } else if (/([^.\n]+)(\[(\d*)\]+)/.exec(key)) {
       //a[0]
       var arrayAccess = /([^.\n]+)(\[(\d*)\]{1,2})/.exec(key)
       var accessIndex = arrayAccess[arrayAccess.length-1];

       //局部、全局 变量处理
       //var __target__ = _localVars.containsKey(arrayAccess[1]) ? _localVars : storedVars;
       var  __target__ = getVariablesTarget(arrayAccess[1], _localVars, scopeStoreOrScriptVars, storedVars);
       if (__target__.containsKey(arrayAccess[1])) {
         var r3 = __target__.get(arrayAccess[1])
         return r3[accessIndex]
       }
       else if(arrayAccess[1] && /([^.\n]+)(\[(\d*)\]+)/.exec(arrayAccess[1])){
         try{
            var arrayAccessTemp = /([^.\n]+)(\[(\d*)\]+)/.exec(arrayAccess[1]);
            var accessTempIndex = arrayAccessTemp[arrayAccessTemp.length-1];
            //reset __target__
            //__target__ = _localVars.containsKey(arrayAccessTemp[1]) ? _localVars : storedVars;
             __target__ = getVariablesTarget(arrayAccessTemp[1], _localVars, scopeStoreOrScriptVars, storedVars);
            if (__target__.containsKey(arrayAccessTemp[1])) {
                 var r3 = __target__.get(arrayAccessTemp[1])
                 return r3[accessTempIndex][accessIndex]
            }
         }catch(e){}
       }
 }
// return storedVars.get(key)
}
var __fresh__ = getVariablesTarget(key, _localVars, scopeStoreOrScriptVars, storedVars);
if(__fresh__){
    return __fresh__.get(key);
}

if (_localVars && _localVars.containsKey(key)){
    return _localVars.get(key)
} else if( storedVars.containsKey(key)){
    return storedVars.get(key)
}
    return getEnvVars().get(key)
}

// 判断全局变量是否存在
function variablesHas(key) {
	if (storedVars.containsKey(key) == false) {
	 if (/(\.)/.exec(key)) {
	   //a.b or a[0].b or a.b[0].c
	   var propertyAccess = /([^.\n]+)\.(.*)/.exec(key)
	   if (
	     storedVars.containsKey(propertyAccess[1]) == false &&
	     /([^.\n]+)\[(\d*)\]/.exec(propertyAccess[1])
	   ) {
	     //arr[0].a
	     var arrayAccess = /([^.\n]+)\[(\d*)\]/.exec(propertyAccess[1])
	     return storedVars.containsKey(arrayAccess[1])
	   } else return storedVars.containsKey(propertyAccess[1])
	 } else if (/([^.\n]+)\[(\d*)\]/.exec(key)) {
	   // a[0]
	   var arrayAccess = /([^.\n]+)\[(\d*)\]/.exec(key)
	   return storedVars.containsKey(arrayAccess[1])
	 }
	}
	return storedVars.containsKey(key)
}

function variablesDelete(key) {
    if (storedVars.containsKey(key)) {
        storedVars.remove(key);
    }
    else if (/(\.)/.exec(key)) {
      var propertyAccess = /([^.]+)\.(.*)/.exec(key)
      storedVars.remove(propertyAccess[1])
    }
}

// 获取变量属性的值
function getPropertyValue(obj1, dataToRetrieve) {
  return dataToRetrieve.split('.').reduce(function(o, k) {
    if (/([^.\n]+)\[(\d*)\]/.exec(k)) {
      var arr = /([^.\n]+)\[(\d*)\]/.exec(k)
      return o && o[arr[1]][arr[2]]
    }
    return o && o[k]
  }, obj1)
}

/**
 *
 * target、value 变量解析通用方法，解析 ${} 对应的值
 * @param {*} str 需要解析的变量
 * @param {*} allowNull 是否允许返回空 true 当值不存在的时候返回 null,false 不存在的是否原样返回 str
 * @returns
 */
var variableParsing = function(str, allowNull){
	var result = "";
	var keys = [];
	var match = str.match(/\\$\{(.*?)\}/g);
    if (!match) {
        keys.push(str)
    }else{
        var i = 0;
        while (i < str.length) {
            var currentKey = match.shift();
            var currentKeyIndex = str.indexOf(currentKey, i);
            if (currentKeyIndex > i) {
                // push the string before the current key
                keys.push(str.substr(i, currentKeyIndex - i));
                i = currentKeyIndex
            }
            if (currentKey) {
                // 取 ${} 内部作为变量名
                var regexp = /\\$\{(.*?)\}/g
                var r2 = regexp.exec(currentKey)
                var variableName = r2[1]
                var strItem = variablesGet(variableName);
                WDS.log.info("取 ${} 内部作为变量名:" +  strItem)
                if(strItem != null){
                    if(typeof strItem === "string" && strItem.startsWith("@")){
                        strItem = Mock.mock(strItem);
                    }
                    keys.push(strItem);
                }else {
                    // not a key, and not a stored variable, push it as-is
                    //如果允许空值返回 一般用于提取
                    if (allowNull) {
                        keys.push(null);
                    } else {
                        keys.push(currentKey)
                    }
                }
                i += currentKey.length;
            }else if (i < str.length) {
                // push the rest of the string
                keys.push(str.substr(i, str.length))
                i = str.length
            }
        }
    }
    if(keys.length > 0){
        if(keys.length == 1){
            return keys[0];
        }
        for(var i=0; i<keys.length; i++){
            result = result + keys[i];
        }
    }
    return result;
}

// target、value 变量解析通用方法，解析 ${} 对应的值
var variableInputParsing = function(str){
	var result = "";
	var keys = [];
	var match = str.match(/\\$\{(.*?)\}/g);
    if (!match) {
        keys.push(str)
    }else{
        var i = 0;
        while (i < str.length) {
            var currentKey = match.shift();
            var currentKeyIndex = str.indexOf(currentKey, i);
            if (currentKeyIndex > i) {
                // push the string before the current key
                keys.push(str.substr(i, currentKeyIndex - i));
                i = currentKeyIndex
            }
            if (currentKey) {
                // 取 ${} 内部作为变量名
                var regexp = /\\$\{(.*?)\}/g
                var r2 = regexp.exec(currentKey)
                var variableName = r2[1]
                var strItem = variablesGet(variableName);
                WDS.log.info("取 Input${} 内部作为变量名:" +  strItem)
                if(strItem != null){
                    var keyTemp = strItem;
                    try{
                       if(IsOnlyJsonString(JSON.stringify(strItem))){
                          keyTemp = JSON.stringify(strItem);
                       }
                       if(IsArray(strItem)){
                          keyTemp = JSON.stringify(strItem);
                       }
                    }catch(e){}
                    keys.push(keyTemp);

                }else {
                    // not a key, and not a stored variable, push it as-is
                    keys.push(currentKey)
                }
                i += currentKey.length;
            }else if (i < str.length) {
                // push the rest of the string
                keys.push(str.substr(i, str.length))
                i = str.length
            }
        }
    }
    if(keys.length > 0){
        for(var i=0; i<keys.length; i++){
           var key = keys[i];
           if (key.startsWith("@")) {
               key = Mock.mock(key);
           }
            result = result + key;
        }
    }

    return result;
}



// sendKeys 封装解析函数，在通用解析之后再次调用
function preprocessKeys(str) {
  var keys = []
    var match = str.match(/\\$\{(.*?)\}/g)
    if (!match) {
      keys.push(str)
    } else {
      var i = 0
      while (i < str.length) {
        var currentKey = match.shift(),
          currentKeyIndex = str.indexOf(currentKey, i)
        if (currentKeyIndex > i) {
          // push the string before the current key
          keys.push(str.substr(i, currentKeyIndex - i))
          i = currentKeyIndex
        }
        if (currentKey) {
          if (/^\\$\{KEY_\w+\}/.test(currentKey)) {
            // is a key
            var keyName = currentKey.match(/\\$\{KEY_(\w+)\}/)[1]
            if(platformName === platform.MAC && keyName === "CONTROL"){
                keyName = "COMMAND"
            }
            var key = "keyAction." + keyName;
            if (keyName) {
              keys.push(key)
            } else {
              throw new Error("Unrecognised key " + keyName)
            }
          }
          else if(/^\\$\{KEY_\w+\+\w+\}/.test(currentKey)){
              var keyName = currentKey.match(/\\$\{KEY_(\w+)\+(\w+)\}/)[1]
              if(platformName === platform.MAC && keyName === "CONTROL"){
                   keyName = "COMMAND"
              }
              var compose = currentKey.match(/\\$\{KEY_(\w+)\+(\w+)\}/)[2]
              if(keyName && compose){
                 keys.push("keyAction." + keyName + "+\"" + compose + "\"");
              }
          }
          else {
            // not a key, and not a stored variable, push it as-is
            keys.push(currentKey)
          }
          i += currentKey.length
        } else if (i < str.length) {
          // push the rest of the string
          keys.push(str.substr(i, str.length))
          i = str.length
        }
      }
    }
    return keys
}

// 获取对应的变量值
function returnVarValue(varName){
    return variablesGet(varName);
}

// Base64 解码特殊指令
function escapeScriptPreprocessor(script){
	WDS.log.info("开始解析特殊指令");
	WDS.log.info(Base64.decode(script));
    return scriptPreprocessor(Base64.decode(script));
}

// 表达式解析函数，表达式解析指令使用
function scriptPreprocessor(script) {
  WDS.log.info("开始解析表达式指令");
  var value = script.replace(/^\s+/, '').replace(/\s+\$/, '')
  //checkVariable(value);
  var r2
  var parts = []
  var variablesUsed = {}
  var argv = []
  var argl = 0 // length of arguments
  if (/\\$\{/.exec(value)) {
    var regexp = /\\$\{(.*?)\}/g
    var lastIndex = 0
    while ((r2 = regexp.exec(value))) {
      var variableName = r2[1]
        WDS.log.info("r2:" + r2);
        WDS.log.info("value:" + value);
      // 如果变量未定义，返回原样
      if(variablesGet(variableName) === null){
        WDS.log.info("如果变量未定义，返回原样");
        continue;
      }

      if (r2.index - lastIndex > 0) {
        parts.push(value.substring(lastIndex, r2.index))
      }
      if (!variablesUsed.hasOwnProperty(variableName)) {
        variablesUsed[variableName] = argl
        argv.push(variableName)
        argl++
      }
      parts.push("arguments[" + variablesUsed[variableName] + "]")
      lastIndex = regexp.lastIndex
    }
    if (lastIndex < value.length) {
      parts.push(value.substring(lastIndex, value.length))
    }
    argv = argv.map(returnVarValue)
     WDS.log.info("解析指令结果rrr");
     WDS.log.info("解script:" + parts.join(''));
     WDS.log.info("解argv:" + argv);
     WDS.log.info("判断argv:" + argv.length);
    if (!argv ||  argv.length === 0 || argv === []) {
        parts = [];
        var str = script.substring(script.lastIndexOf("=")+2);
        WDS.log.info("再解str:" + str);
        parts.push("arguments[0]")
        parts.push(" === " + str)
        argv.push("NoSuchElementException")
     }
     WDS.log.info("再解argv:" + argv);
    return { script: parts.join(''), argv: argv }
  } else {
    WDS.log.info("解析指令结果");
     WDS.log.info("解script:" + value);
     WDS.log.info("解argv:" + argv);
    return {script: value, argv: argv}
  }
}

function parseProgramScript(script) {
    try {
		WDS.log.info("开始执行脚本: " + (scriptPreprocessor(script)).script);
		WDS.log.info("开始执行脚本值: " + (scriptPreprocessor(script)).argv);
		var result = driver.executeScript("return (" + (scriptPreprocessor(script)).script + ")", ((scriptPreprocessor(script)).argv));
		WDS.log.info("执行script结果: " + result);
		return result;
    } catch (e) {
		WDS.log.info((scriptPreprocessor(script)).script+"脚本内容执行失败: " + e);
        return false;
    }
}

var success = function () {
    WDS.sampleResult.setSuccessful(true);
}

var error = function (msg) {
    WDS.log.info("error处理: " + msg);
    WDS.sampleResult.setSuccessful(false);
    WDS.sampleResult.setResponseMessage("==========cmd: " + cmdName + " execute error! detail msg:==========" + msg);
}

var autoFind = function(target){
    if(!target || target == ""){
        throw new Error("target is not exist!")
    }
    try{
    if(target && typeof target == "string"){
        var type = target.split("=")[0];
        return finder[type](target);
    }
    }catch(e){
        throw new Error("target format is not a string");
    }
}

// 剥离 target string 中的对象
var splitTarget = function(target){
     if(target && typeof target == "string"){
        if(target.indexOf("=") != -1){
            return target.substr(target.indexOf("=") + 1, target.length);
        }else{
            return target;
        }
     }
}

// selenium 所有的选择器
var finder = {
    // id 选择器
    id : function(target){
        return By.id(splitTarget(target));
    },

    // class 选择器
    className : function(target){
        return By.className(splitTarget(target));
    },

    // name 选择器
    name : function(target){
        return By.name(splitTarget(target));
    },

    // tagName 选择器
    tagName : function(target){
        return By.tagName(splitTarget(target));
    },

    // linkText 选择器
    linkText : function(target){
        return By.linkText(splitTarget(target));
    },

    // partialLinkText 选择器
    partialLinkText : function(target){
        return By.partialLinkText(splitTarget(target));
    },

    // css 选择器
    css : function(target){
        return By.cssSelector(splitTarget(target));
    },

    // xpath 选择器
    xpath : function(target){
        return By.xpath(splitTarget(target));
    },

    // label 选择器
    label : function(target){
        return By.xpath("//option[. = '" + splitTarget(target) + "']");
    },

    // value 选择器
    value : function(target){
        return By.cssSelector("*[value='" + splitTarget(target) + "']");
    },

    // index 选择器
    index : function(target){
        return By.cssSelector("*:nth-child(" + splitTarget(target) + ")");
    }
}


// 指令id
var cmdId;
// 单条指令的结果
var r;
// 单条指令的配置
var sc;
// 循环的下标
var i = 0;
var responseHeaders = [];

//本地变量表
var localVars = {};
//环境变量表
var envVars = {};
// 当前使用的环境
var currentEnvId = "";

//当前运行的指令id
var currentCmdLineId = "";
//指令栈
var cmdLineStack = [];
//流程控制区间
var programRegistry = 0;
var programRegistryStack = [];

//扩展属性
var _extendProp = {
    //上一个指令执行的类型 PRE MAIN POST
    preType: "",
    curType: "",
    trunkErr: false,
};

function pushCmdStack(item){
    if(item){
        cmdLineStack.push(item);
    }
}
function popCmdStack(){
    return cmdLineStack.pop() || "";
}

function curToPre(curType){
  if(_extendProp.curType){
    //存在则放入pre
    _extendProp.preType = _extendProp.curType;
  }
  _extendProp.curType = curType;
}

function checkIsNewCommand(){
    // 兼容 APPEND
    if(_extendProp.preType === "APPEND" || _extendProp.preType === "POST" || _extendProp.preType === "MAIN"){
        if(_extendProp.curType === "PRE" || _extendProp.curType === "MAIN"){
            return true;
        }
    }
    return false;
}

// 指令执行前的处理
var beforeHandle = function(id, sConfig){

   cmdId = id;

   lastHandle = preHandle;
   preHandle = driver.getWindowHandle();

   lastHandlesCount = preHandlesCount;
   preHandlesCount = driver.getWindowHandles().size();

   r = {};
   sc = sConfig;
   r.startTime = Date.now();

   exception = null;
   curToPre(sc.processType);

   if(checkIsNewCommand()){
      scopeStoreOrScriptVars = new java.util.HashMap();
   }
   //处理 singleCommandConfig 配置的场景变量
   var subVars = sc ? sc.scenarioVariables : sc;
   //存储局部变量
   currentCmdLineId = id;
    pushCmdStack(id);
   if(subVars && subVars.length > 0){
       var scopeVars = new java.util.HashMap();
       for(var i=0; i<subVars.length; i++){
   	    scopeVars.put(subVars[i].name, typeConvert(subVars[i].value, subVars[i].type));
       }
       if(!localVars[currentCmdLineId]){
              localVars[currentCmdLineId] = scopeVars;
       }
   }

   // 初始化环境变量 确保只加载一次, 使用时候根据环境id 切换使用，保证统一环境变量在全局使用
   if(sc.envId && !envVars[sc.envId]){
       var tempEnvVars = sc.envVariables || [];
       if(tempEnvVars && tempEnvVars.length > 0){
         // 生成变量表
         var tempScopeVars = new java.util.HashMap();
         for(var i=0; i<tempEnvVars.length; i++){
            tempScopeVars.put(tempEnvVars[i].name, typeConvert(tempEnvVars[i].value, tempEnvVars[i].type));
         }
         envVars[sc.envId] = tempScopeVars;
       }
   }
   currentEnvId = sc.envId || "";

   //将上一次窗口size与当前窗口size做比较，上一次窗口id，当前窗口ID比较，如果都相同，则认为不需要跳到最新页面，
   if(lastHandle != preHandle || lastHandlesCount != preHandlesCount){
        //惰性windowhandle 需要手动切换我们不知道程序何时打开了新的窗口，click或者window.open等等或者网页自身打开
        switchToNewestWindow();
   }

}

//检查当前步骤是否是关闭窗口，如果只有一个窗口，逻辑报错，不截图
function checkWindowIsClosed(){
    if(cmdName === "close"){
        if(preHandlesCount <= 1) {
            WDS.log.info("window is already closed!");
            exception = new Error("window is already closed!");
        }
    }
}

// 窗口句柄是惰性的,程序不知道用户什么时候的操作打开了新的窗口，每次都切到最新的当前窗口以配合 driver.close
function switchToNewestWindow() {
    if (shouldSwitchHandle) {

        var currentHandle = driver.getWindowHandles();
        if (currentHandle.size() == 1) {
           // 当只有一个窗口句柄的时候 js 执行下面语句然后定位元素没问题。java 和 python 有问题，先记录
          return;
        }
        var currentIterator = currentHandle.iterator();
        while (currentIterator.hasNext()) {
            driver.switchTo().window(currentIterator.next());
        }
        //切到新的窗口的时候设置一次分辨率解决headless自动恢复1366x768的问题 导致有些时候窗口元素被隐藏无法定位等等问题
        resetBrowserWindowSize();
    }
}

//重置窗口分辨率
function resetBrowserWindowSize(){
   var size = driver.manage().window().getSize();
   if (size.getWidth() != resolutionRatioX || size.getHeight() != resolutionRatioY) {
       driver.manage().window().setSize(new org.openqa.selenium.Dimension(resolutionRatioX, resolutionRatioY));
   }
}


function incrProgramRegistry(cId){
 try{
    if(programRegistry < 0){
      programRegistry = 0;
    }
    programRegistry ++;
    programRegistryStack[programRegistry]= cId;
  }catch(e) {}
}

function decrProgramRegistry(){
 try{
     delete programRegistryStack[programRegistry];
     programRegistry--;
     if(programRegistry < 0){
        programRegistry = 0;
     }
   }catch(e) {}
}

function refreshProgramRegistryStack(value, result){
    try{
        programRegistryStack[programRegistry] ? localVars[programRegistryStack[programRegistry]].put(value, result): localVars[currentCmdLineId].put(value, result)
        localVars[currentCmdLineId].put(value, result)
    }catch(e){}

}

// 指令执行后的处理
var afterHandle = function(){
    r.success = true;
    if(programRegistry > 0){
        return;
    }
    //当前场景变量用完后删除防止影响其他指令
    //释放当前指令对应的变量表

   delete localVars[popCmdStack()]
   currentCmdLineId = cmdLineStack[cmdLineStack.length > 0 ? cmdLineStack.length - 1 : 0] || ""

   WDS.log.info("当前指令:" +  currentCmdLineId)

}


// error 的后续处理
var errorHandle = function(e){
    WDS.log.info("执行失败日志:" +  e)
    r.body = e.message;
    r.success = false;
    exception = e;
    error(e);
}

//存储所有截图数据
var screenshotArr = [];
//截图操作 filename-截图名称  options - 扩展项json string
var screenshot = function(name, options){
         if(cmdName === "close" && preHandlesCount <= 1){
              return;
         }
         //增加短暂的暂停时间
         java.lang.Thread.sleep(100);

         var fileName = cmdName + "——" + cmdId + "——" + Date.now() + "——" + "screenshot.png";
         //截图前首先要关闭RemoteAlert弹框
         try{
            //var currentAlertDialog = driver.switchTo().alert();
            //if(currentAlertDialog){
               //currentAlertDialog.dismiss();
               //存在弹框 不进行截图
               //return;
            //}
             // 存在弹框也尝试下截图
             var screenshot = driver.getScreenshotAs(pkg.OutputType.FILE);
             var fullName = screenShotPath + reportId + "/"  + fileName;
             org.apache.commons.io.FileUtils.moveFile(screenshot, new java.io.File(fullName));
             var obj = {
                stepId: cmdId,
                time: Date.now(),
                name: name,
                type: sc.processType, //截图类型
                path: fileName
             }
             screenshotArr.push(obj);
             WDS.log.info("screenshot:" +  JSON.stringify(screenshotArr))
         }catch(e){
            //没有弹框会抛出NoAlertPresentException异常， 忽略
         }
}
var synchronizeScreenshot = {};
// finally 的后续处理
var finallyHandle = function(cmdName){
     var fileName = cmdName + "——" + cmdId + "——" + Date.now() + "——" + "screenshot.png";
     WDS.log.info("fileName:" +  fileName)
     WDS.log.info("cmdName:" +  cmdName)
     r.endTime = Date.now();
     r.isNotStep = sc.isNotStep;
     r.processType = sc.processType;
     r.cmdName = cmdName;
     r.id = cmdId;
     r.reportId = reportId;

    if(checkIsNewCommand()){
       commandErrCache = {};
       _extendProp.trunkErr = false;
       synchronizeScreenshot = {};
    }

    if(!sc.screenshotConfig && sc.screenshotConfig != 0){
        //默认异常截图
        WDS.log.info("默认异常截图")
        sc.screenshotConfig = 1;
    }

    if(cmdName != "screenshot" && stepScreenshot(sc.screenshotConfig, r.processType)){
        screenshot(customName)
    }

     //异常情况截图
     if(cmdName != "screenshot" && stepErrorScreenshot(sc.screenshotConfig, exception) && !_extendProp.trunkErr){
         if(hasLock(sc.combinationProxyId)){
            screenshot(getScreenshotFullName(r.processType, exception))
         }
     }

     //检查是否关闭了所有窗口
     checkWindowIsClosed();
     //主干步骤报错 不再进行后置操作异常截图
     if(exception && isTrunk(sc.processType)){
         _extendProp.trunkErr = true;
     }

     //持久化截图缓存
     r.uiScreenshots = screenshotArr;
     //重置截图缓存
     screenshotArr = [];

     responseHeaders.push(r);

     //特殊处理提取异常不影响步骤状态
     if(sc.isExtract && exception){
        r.success = true;
     }

     if (!sc.isNotStep) {
         if (cmdName == "selectWindow" || cmdName == "selectFrame") {
            shouldSwitchHandle = false;
         } else {
            shouldSwitchHandle = true;
         }
     }

     if(!sc.ignoreFail && exception != null){
        WDS.log.info("finallyException:" +  exception)
        WDS.sampleResult.setResponseHeaders(JSON.stringify(responseHeaders));
        WDS.sampleResult.sampleEnd();
        WDS.sampleResult.setResponseCode("500");
        WDS.sampleResult.setSuccessful(false);
        throw (exception);
     }

}

var hasLock = function(combinationProxyId){
    if(!combinationProxyId){
        return true;
    }

    if(!synchronizeScreenshot[combinationProxyId] || synchronizeScreenshot[combinationProxyId] <= 0){
        synchronizeScreenshot[combinationProxyId] = 1;
        return true;
    }
    return false;
}

//存储前后置指令名称 计数统计 如 前置操作-提取参数异常1 ...
var commandErrCache = {};

//获取错误情况 当前截图名字计数
var getScreenshotFullName = function(processType, exception, targetName){
    var exceptionTip = exception ? " 异常" : "";
    WDS.log.info("customName:" +  customName)
     WDS.log.info("processType:" +  processType)
    if(customName){
        customName = customName == "runScript" ? "脚本" : customName;
    }
    var showName = targetName ? targetName : customName;
    var screenshotFullName = getOptionPrefix(processType) + showName +  exceptionTip;
    var _cache = commandErrCache[screenshotFullName];
    if(_cache || _cache == 0){
        var _targetIndex = parseInt(_cache) + 1;
        commandErrCache[screenshotFullName] = _targetIndex;
        screenshotFullName = screenshotFullName + _targetIndex;
        return screenshotFullName;
    }
    commandErrCache[screenshotFullName] = 0;
    return screenshotFullName;
}

//获取操作类型前缀
var getOptionPrefix = function(processType){
    if(processType === "PRE"){
        return "前置操作-";
    }
    else if(processType === "POST"){
        return "后置操作-"
    }
    return "";
}

//是否不截图
var notScreenshot = function(type, processType){
    return type === 2;
}

//是否异常截图
var stepErrorScreenshot = function(type, exception){
    return type === 1 && exception != null;
}

//是否当前步骤截图
var stepScreenshot = function(type, processType){
    if(type === 0 && processType === "MAIN"){
        return true;
    }
    return false;
}

//检测是否为主干步骤
var isTrunk = function(processType){
    if(processType === "MAIN"){
       return true;
    }
    return false;
}

// 弹窗事件处理函数
var alertHandle = function(){

}

var handleOutputParam = function(key, value){
   try{
       var _output = {};
       _output.id = cmdId;
       _output.name = customName;
       _output.key = key;
       _output.value = value;
       _output.type = sc.type;
       _output.processType = sc.processType;
       r.outputList = [];
       r.outputList.push(_output)
   }catch(e){}
}

function getWaitObject(seconds){
    if(!seconds){
        seconds = 0;
    }
    return new support_ui.WebDriverWait(driver, java.time.Duration.ofSeconds(seconds, 1));
}

WDS.sampleResult.sampleStart();
//保证每次执行前都是没有历史缓存
driver.manage().deleteAllCookies();
// 设置全局配置
// 设置分辨率
driver.manage().window().setSize(new org.openqa.selenium.Dimension(commandConfig.resolutionRatioX, commandConfig.resolutionRatioY));
//driver.manage().window().maximize();

