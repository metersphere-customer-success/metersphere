// 句柄只有一个时关闭会有 session 错误
var handlesCount = driver.getWindowHandles().size();
if(handlesCount > 1){
    try{
        driver.close();
        //有多个句柄 关掉后需切换其他窗口
        var currentHandle = driver.getWindowHandles();
        var currentIterator = currentHandle.iterator();
        var lastHandle = null;
        while (currentIterator.hasNext()){
            lastHandle = currentIterator.next()
        }
        if(lastHandle != null){
            driver.switchTo().window(lastHandle);
        }
    }catch (e) {
        WDS.log.info("close handle err, handle has been closed ~")
    }
}