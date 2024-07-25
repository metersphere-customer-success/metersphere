var target = variableParsing("${target}");
if(target === "fullScreen"){
    //driver.manage().window().fullscreen();
    driver.manage().window().maximize();
}else{
    resolutionRatioX = java.lang.Integer.valueOf(target.split("x")[0]);
    resolutionRatioY = java.lang.Integer.valueOf(target.split("x")[1]);
    driver.manage().window().setSize(new org.openqa.selenium.Dimension(resolutionRatioX, resolutionRatioY));
}


