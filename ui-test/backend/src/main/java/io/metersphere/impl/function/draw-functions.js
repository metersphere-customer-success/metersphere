/**
 * 格式化坐标列表
 * 1.大于浏览器高度或者宽度取浏览器的高度或者宽度
 * 2.将坐标转换成偏移量供给 selenium action 使用
 *   如 (0, 0) -> (100, 0) -> (100 , 100) -> (0 ,100) -> (0, 0)
 *
 * @param coords 坐标列表
 * @param elementSize selenium element.getSize()
 */
function formatCoord(coords, elementSize) {
    coords = extractCoords(coords);
    if (!coords || !coords.length) {
        return [{x: 0, y: 0}];
    }

    var helfWidth = ((elementSize.getWidth()) / 2);
    var helfHeight = ((elementSize.getHeight()) / 2);
// 计算从元素中心点到元素左上角的偏移量
    var zeroPointX = 0 - helfWidth;
    var zeroPointY = 0 - helfHeight;

    for (var i = 0; i < coords.length; i++) {
        if (coords[i].x > elementSize.getWidth()) {
            coords[i].x = elementSize.getWidth();
        }
        if (coords[i].y > elementSize.getHeight()) {
            coords[i].y = elementSize.getHeight();
        }
    }

    //转成偏移量
    var tempCoords = JSON.parse(JSON.stringify(coords));
    for (var i = 1; i < coords.length; i++) {
        coords[i].x = tempCoords[i].x - tempCoords[i - 1].x;
        coords[i].y = tempCoords[i].y - tempCoords[i - 1].y;
    }

    if (coords[0]) {
        //以最左上角为0，0起点
        if (coords[0].x === 0) {
            coords[0].x = zeroPointX;
        } else {
            coords[0].x = coords[0].x + zeroPointX;
        }
        if (coords[0].y === 0) {
            coords[0].y = zeroPointY;
        } else {
            coords[0].y = coords[0].y + zeroPointY;
        }
    }
    return coords;
}

/**
 * 从 jackson 里面获取 coords
 * @param coords
 * @param elementSize
 */
function extractCoords(coords) {
    if (!coords || !coords.length) {
        return [{x: 0, y: 0}];
    }
    var finalCoords = [];
    for (var i = 0; i < coords.length; i++) {
        if (coords[i].map) {
            finalCoords.push(coords[i].map);
        }
    }
    return finalCoords;
}