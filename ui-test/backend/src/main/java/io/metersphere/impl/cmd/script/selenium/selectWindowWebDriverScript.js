var target = variableParsing("${target}");

if (/^handle=/.test(target)) {
    driver.switchTo().window(target.split('handle=')[1]);
} else if (/^name=/.test(target)) {
    driver.switchTo().window(target.split('name=')[1]);
} else if (/^win_ser_/.test(target)) {
    if (target === 'win_ser_local') {
        var handles =  driver.getWindowHandles();
        var iterator = handles.iterator();
        var counter = 1;
        while (iterator.hasNext())
        {
          var handle = iterator.next();
          driver.switchTo().window(handle);
          if(counter === 1){
            break;
          }
        }
    } else {
        var index = parseInt(target.substr('win_ser_'.length));
        var handles =  driver.getWindowHandles();
        if(index > handles.size()){
            throw new Error("inputed window index: " + index + " is not exists! max is :" + handles.size());
        }
        var iterator = handles.iterator()
        var counter = 1;
        while (iterator.hasNext())
        {
            var handle = iterator.next();
            driver.switchTo().window(handle);
            if(counter === index){
                break;
            }
            counter ++;
        }
    }
}

