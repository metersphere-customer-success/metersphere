try{
    timeout = 15000;
    var sc = ${singleCommandConfig};
    if(sc.secondsWaitElement !== undefined && sc.secondsWaitElement !== ""){
        timeout = sc.secondsWaitElement;
    }
    var wait = getWaitObject(Math.floor(timeout / 1000));

    cmdName = "${cmdName}";

    customName = "${customName}";

    beforeHandle("${cmdId}", sc);

    ${webdriverScript}

    afterHandle();

}catch (e){
    errorHandle(e);
}
finally{
    finallyHandle("${cmdName}");
}
