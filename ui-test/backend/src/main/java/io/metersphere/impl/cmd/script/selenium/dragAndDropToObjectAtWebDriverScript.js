var target = ${target};
var value = ${value};

var dragged = wait.until(conditions.presenceOfElementLocated(autoFind(target.map.locator)));
var dropped = wait.until(conditions.presenceOfElementLocated(autoFind(value.map.locator)));
var targetCoords = formatCoord(target.map.coords.myArrayList, dragged.getSize());
var valueCoords = formatCoord(value.coords, dropped.getSize());



actionProvider.moveToElement(dragged);
actionProvider.moveByOffset(targetCoords[0].x, targetCoords[0].y);
actionProvider.clickAndHold();

for (var i = 1; i < targetCoords.length; i++) {
    actionProvider.moveByOffset(targetCoords[i].x, targetCoords[i].y);
}

actionProvider.moveToElement(dropped);

for (var i = 0; i < valueCoords.length; i++) {
    actionProvider.moveByOffset(valueCoords[i].x, valueCoords[i].y);
}
actionProvider.release().build().perform();