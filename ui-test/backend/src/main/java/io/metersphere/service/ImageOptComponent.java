package io.metersphere.service;

import io.metersphere.UiApplication;
import io.metersphere.commons.utils.LogUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 操作图片
 */
public class ImageOptComponent {

    public static final String DEFAULT_TYPE = "png";

    public static final Integer FONT_SIZE = 25;

    public static final Integer FONT_SIZE_BUFFER = 10;

    public static final Integer FONT_PADDING = 110;

    public static final Color FONT_COLOR = new Color(85, 85, 85);

    public static final Color GAP_BACK_GROUND_COLOR = new Color(232, 232, 232);

    private static final String FONT_FAMILY = "微软雅黑";

    private AtomicInteger cursor = new AtomicInteger(0);

    private Map<String, BufferedImage> bufferedImageCache = new ConcurrentHashMap<>(16);

    private int getCursor() {
        return cursor.get();
    }

    private void cursorCumulative(int num) {
        cursor.getAndSet(cursor.get() + num);
    }

    private void clearCursor() {
        cursor.getAndSet(0);
    }

    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(UiApplication.class.getClassLoader());

    public static final double IMAGE_HEIGHT = 1096;

    public static final double IMAGE_WIDTH = 1912;

    public double zoomX = 1;

    public double zoomY = 1;


    /**
     * 1 : 1 纵横比
     */
    private Integer zoom = 1;

    @Getter
    @Setter
    public static class ImageDTO {
        private String title;
        private String path;
    }

    private Font font = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE);

    public ImageOptComponent() {
        try {
            Resource resource = resolver.getResource("fonts/songTypeface.ttf");
            Font baseFont = Font.createFont(Font.PLAIN, resource.getInputStream());
            this.font = baseFont.deriveFont(Font.PLAIN, FONT_SIZE);
        } catch (Exception e) {
            LogUtil.error("初始化字体失败", e);
        }
    }

    /**
     * 导入本地图片到缓冲区
     *
     * @param imgName
     * @return
     */
    public BufferedImage loadImageLocal(String imgName) {
        try {
            return ImageIO.read(new File(imgName));
        } catch (IOException e) {
            LogUtil.info("拼接图片业务执行失败，图片加载失败");
        }
        return null;
    }


    public void load(List<ImageDTO> images) {
        for (ImageDTO dto : images) {
            bufferedImageCache.put(dto.getPath(), loadImageLocal(dto.getPath()));
        }
    }

    public int calcTotalHeight() {
        return bufferedImageCache.values().stream().mapToInt(BufferedImage::getHeight).sum();
    }

    public int calcMaxWidth() {
        return bufferedImageCache.values().stream().mapToInt(BufferedImage::getWidth).max().orElse((int) IMAGE_WIDTH);
    }

    public void mergeImage(List<ImageDTO> images, String path) {

        //load all image
        load(images);
        //获取最宽的宽度
        int targetWidth = calcMaxWidth();

        int targetHeight = calcTotalHeight();
        int offsetHeight = images.size() * (FONT_PADDING + FONT_SIZE + FONT_SIZE_BUFFER);

        //横纵比
        calcZoom(targetWidth, targetHeight, images.size());
        //重新计算宽高
        targetWidth = (int) Math.round(targetWidth * zoomX);
        targetHeight = (int) Math.round(targetHeight * zoomY) + offsetHeight;

        // 新的图
        BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        // 画图
        doPaint(newImage, images, targetWidth, targetHeight);

        //生成图片
        writeImageLocal(path, newImage);
    }

    private void calcZoom(int targetWidth, int targetHeight, int size) {
        calcZoomX(targetWidth);
        calcZoomY(targetHeight, size);
    }

    public double calcZoomX(int targetWidth) {
        zoomX = IMAGE_WIDTH / targetWidth;
        return zoomX;
    }

    public double calcZoomY(int targetHeight, int size) {
        zoomY = IMAGE_HEIGHT * size / targetHeight;
        return zoomY;
    }

    private void doPaint(BufferedImage newImage, List<ImageDTO> images, int targetWidth, int targetHeight) {
        Graphics2D graphics = (Graphics2D) newImage.getGraphics();

        //通用配置
        //抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        //追求着色质量
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics.setPaint(Color.WHITE);
        graphics.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());

        //动态写
        for (int i = 0; i < images.size(); i++) {

            BufferedImage currentImage = bufferedImageCache.get(images.get(i).path);

            //先写文字
            graphics.setPaint(GAP_BACK_GROUND_COLOR);
            graphics.fillRect(0, getCursor(), targetWidth, FONT_PADDING + FONT_SIZE + FONT_SIZE_BUFFER);
            cursorCumulative(FONT_PADDING);

            setText(graphics, targetWidth, images.get(i).title, FONT_SIZE, getCursor());
            cursorCumulative(FONT_SIZE + FONT_SIZE_BUFFER);

            //图片局中
            int offsetX = 0;
            if (targetWidth > calcWidthAndZoomX(currentImage.getWidth())) {
                offsetX = targetWidth / 2 - calcWidthAndZoomX(currentImage.getWidth()) / 2;
            }
            //再写图片
            graphics.drawImage(currentImage, offsetX, getCursor(),
                    calcWidthAndZoomX(currentImage.getWidth()),
                    calcHeightAndZoomY(currentImage.getHeight()),
                    null);
            cursorCumulative(calcHeightAndZoomY(currentImage.getHeight()));
        }

//        //底部追加 padding
//        graphics.setPaint(GAP_BACK_GROUND_COLOR);
//        graphics.fillRect(0, getCursor(), targetWidth, FONT_PADDING);
//        cursorCumulative(FONT_PADDING);

        //重置 cursor
        clearCursor();
    }

    public int calcWidthAndZoomX(int width) {
        return (int) Math.round(width * zoomX);
    }

    public int calcHeightAndZoomY(int height) {
        return (int) Math.round(height * zoomX);
    }

    /**
     * 生成新图片到本地
     *
     * @param newImage
     * @param img
     */
    public void writeImageLocal(String newImage, BufferedImage img) {
        if (newImage != null && img != null) {
            try {
                File outputFile = new File(newImage);
                ImageIO.write(img, DEFAULT_TYPE, outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setText(Graphics2D graphics, int targetWidth, String title, int start, int end) {
        graphics.setFont(font);
        graphics.setColor(FONT_COLOR);
        //写入 title
        int width = graphics.getFontMetrics(font).stringWidth(title);
        int startY = end - 2 * FONT_SIZE;
        int correct = FONT_SIZE;
        //矫正
        if (width / (targetWidth - 3 * FONT_SIZE) >= 3 && width % (targetWidth - 3 * FONT_SIZE) > 0) {
            startY = end - 3 * FONT_SIZE + FONT_SIZE_BUFFER;
            correct = 2 * FONT_SIZE - FONT_SIZE_BUFFER;
        }
        if (width > targetWidth) {
            char[] array = title.toCharArray();
            StringBuilder sb = new StringBuilder(array[0]);
            for (char c : array) {
                sb.append(c);
                int newWidth = graphics.getFontMetrics(font).stringWidth(sb.toString());
                // 准备换行
                if ((newWidth + 3 * FONT_SIZE) >= targetWidth) {

                    //写折行文字
                    graphics.drawString(sb.toString(), start, startY);
                    cursorCumulative(FONT_SIZE + FONT_SIZE_BUFFER);

                    startY += FONT_SIZE;
                    sb.delete(0, sb.length());
                }
            }
            graphics.drawString(sb.toString(), start, startY);

        } else {
            graphics.drawString(title, start, startY + correct);
        }
    }

}
