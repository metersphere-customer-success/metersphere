var target = variableParsing("${target}");
var value = "${value}";
value = JSON.parse(value);
var element = wait.until(conditions.presenceOfElementLocated(autoFind(target)));
var detector = new org.openqa.selenium.remote.LocalFileDetector();

if (value && value.length) {
    for (var i = 0; i < value.length; i++) {
        var f = detector.getLocalFile(value[i]);
        if (f == null) {
            throw new Error("file : " + value[i] + " not exists!");
        }
        WDS.log.info("upload file :" + f.toString());
        //the below line does the magic of ensuring that the file automatically gets shipped
        //to the actual node.
        element.setFileDetector(detector);
        element.sendKeys(f.getAbsolutePath());
    }
} else {
    throw new Error("file not exists!");
}