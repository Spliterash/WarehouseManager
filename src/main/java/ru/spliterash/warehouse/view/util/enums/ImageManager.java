package ru.spliterash.warehouse.view.util.enums;

import javafx.scene.image.Image;
import lombok.SneakyThrows;
import lombok.Value;
import ru.spliterash.warehouse.Main;
import ru.spliterash.warehouse.other.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

public enum ImageManager {
    BROKEN,
    PLUS,
    CLOSE;

    private final BufferedImage original;
    private final Map<Size, Image> cache = new WeakHashMap<>();

    @SneakyThrows
    ImageManager() {
        String path = "images/" + name().toLowerCase() + ".png";
        URL resource = Main.getMain().getClass().getClassLoader().getResource(path);
        if (resource == null)
            throw new RuntimeException("Resource is null");
        InputStream stream = resource.openStream();
        original = ImageIO.read(stream);
    }

    public Image getResizedFx(int x, int y) {
        Size size = new Size(x, y);
        Image value = cache.get(size);
        if (value == null) {
            value = Utils.getImage(original, x, y);
            cache.put(size, value);
        }
        return value;
    }

    @Value
    private static class Size {
        int x, y;
    }
}
