package dosbanana.gfx;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class BackgroundWall {
    private BufferedImage cache;
    private int cw, ch;

    public void paint(Graphics2D g, int width, int height) {
        if (cache == null || width != cw || height != ch) {
            cw = width;
            ch = height;
            cache = generate(width, height);
        }
        g.drawImage(cache, 0, 0, null);
    }

    private BufferedImage generate(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D gg = img.createGraphics();

        gg.setColor(new Color(12, 12, 12));
        gg.fillRect(0, 0, w, h);

        int brickW = 80;
        int brickH = 40;
        int mortar = 3;

        Color brickBase = new Color(20, 20, 20);
        Color brickEdge = new Color(35, 35, 35);
        Color mortarCol = new Color(10, 10, 10);

        gg.setColor(new Color(0, 0, 0));
        gg.fillRect(0, 0, w, h);

        Random rnd = new Random(1337L + w * 31L + h * 17L);

        for (int row = 0, y = 0; y < h + brickH; row++, y += brickH) {
            int offset = (row % 2 == 0) ? 0 : brickW / 2;

            for (int x = -offset; x < w + brickW; x += brickW) {
                int bx = x;
                int by = y;

                int v = rnd.nextInt(9) - 4;
                int r = clamp(brickBase.getRed() + v, 0, 255);
                int gr = clamp(brickBase.getGreen() + v, 0, 255);
                int b = clamp(brickBase.getBlue() + v, 0, 255);

                gg.setColor(new Color(r, gr, b));
                gg.fillRect(bx + mortar, by + mortar, brickW - mortar * 2, brickH - mortar * 2);

                gg.setColor(brickEdge);
                gg.drawRect(bx + mortar, by + mortar, brickW - mortar * 2, brickH - mortar * 2);

                gg.setColor(mortarCol);
                gg.fillRect(bx, by, brickW, mortar);
                gg.fillRect(bx, by, mortar, brickH);
            }
        }

        for (int i = 0; i < (w * h) / 18; i++) {
            int px = rnd.nextInt(w);
            int py = rnd.nextInt(h);
            int n = rnd.nextInt(35);
            int c = 10 + n;
            img.setRGB(px, py, new Color(c, c, c).getRGB());
        }

        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        gg.setColor(Color.BLACK);
        gg.fillRect(0, 0, w, 40);
        gg.fillRect(0, h - 40, w, 40);
        gg.fillRect(0, 0, 40, h);
        gg.fillRect(w - 40, 0, 40, h);

        gg.dispose();
        return img;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}