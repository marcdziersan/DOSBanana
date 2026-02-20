package dosbanana.gfx;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class TileSprite implements Sprite {

    private BufferedImage tile32; // Cache für 32x32
    private int cachedW = -1, cachedH = -1;

    @Override
    public void draw(Graphics2D g, int x, int y, int w, int h) {
        if (tile32 == null || w != cachedW || h != cachedH) {
            cachedW = w;
            cachedH = h;
            tile32 = generateTile(w, h);
        }
        g.drawImage(tile32, x, y, null);
    }

    private BufferedImage generateTile(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // etwas bessere Kanten
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Random rnd = new Random(1337); // einmalig für das Tile

        // --- Palette ---
        Color baseOuter = new Color(10, 10, 10);
        Color baseMid   = new Color(22, 22, 22);
        Color baseInner = new Color(30, 30, 30);

        Color bevelLight = new Color(115, 115, 115);
        Color bevelDark  = new Color(0, 0, 0);

        Color topRunLine  = new Color(140, 140, 140);
        Color topRunLine2 = new Color(60, 60, 60);

        Color goldA = new Color(210, 170, 70);
        Color goldB = new Color(120, 95, 35);

        // --- Drop shadow (im Tile selbst leicht, wirkt auf Background) ---
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(2, 3, w - 2, h - 2, 8, 8);

        // --- Base block ---
        g.setColor(baseOuter);
        g.fillRoundRect(0, 0, w, h, 8, 8);

        g.setColor(baseMid);
        g.fillRoundRect(1, 1, w - 2, h - 2, 8, 8);

        g.setColor(baseInner);
        g.fillRoundRect(3, 3, w - 6, h - 6, 7, 7);

        // --- Strong bevel lines ---
        g.setColor(new Color(bevelLight.getRed(), bevelLight.getGreen(), bevelLight.getBlue(), 220));
        g.drawLine(2, 2, w - 3, 2);
        g.drawLine(2, 2, 2, h - 3);

        g.setColor(new Color(bevelDark.getRed(), bevelDark.getGreen(), bevelDark.getBlue(), 230));
        g.drawLine(2, h - 3, w - 3, h - 3);
        g.drawLine(w - 3, 2, w - 3, h - 3);

        // inner bevel
        g.setColor(new Color(180, 180, 180, 90));
        g.drawLine(4, 4, w - 5, 4);
        g.drawLine(4, 4, 4, h - 5);

        g.setColor(new Color(0, 0, 0, 120));
        g.drawLine(4, h - 5, w - 5, h - 5);
        g.drawLine(w - 5, 4, w - 5, h - 5);

        // --- Top run surface ---
        int runH = Math.max(3, h / 6);
        g.setColor(topRunLine);
        g.fillRoundRect(4, 4, w - 8, runH, 8, 8);

        g.setColor(new Color(topRunLine2.getRed(), topRunLine2.getGreen(), topRunLine2.getBlue(), 220));
        g.fillRect(4, 4 + runH, w - 8, 2);

        // --- Gloss band ---
        g.setColor(new Color(255, 255, 255, 45));
        g.fillRoundRect(5, 6, w - 10, Math.max(6, h / 4), 10, 10);

        // --- Inset shadow ---
        g.setColor(new Color(0, 0, 0, 90));
        g.drawRoundRect(3, 3, w - 6, h - 6, 7, 7);

        // --- Gold shimmer (im Cached Tile ein paar Stellen) ---
        if (rnd.nextInt(100) < 60) {
            int gx = 6 + rnd.nextInt(Math.max(1, w - 16));
            int gy = 6 + rnd.nextInt(Math.max(1, h - 16));
            int gw = 10 + rnd.nextInt(12);
            int gh = 3 + rnd.nextInt(4);

            g.setColor(new Color(goldA.getRed(), goldA.getGreen(), goldA.getBlue(), 80));
            g.fillRoundRect(gx, gy, Math.min(gw, w - 8), Math.min(gh, h - 8), 8, 8);

            g.setColor(new Color(goldB.getRed(), goldB.getGreen(), goldB.getBlue(), 60));
            g.fillRoundRect(gx + 2, gy + 2, Math.max(2, Math.min(gw - 4, w - 12)), 2, 8, 8);
        }

        // --- Speckles: NUR EINMAL ins Tile, nicht jedes Frame ---
        int speckles = 18;
        for (int i = 0; i < speckles; i++) {
            int px = 4 + rnd.nextInt(Math.max(1, w - 8));
            int py = 4 + rnd.nextInt(Math.max(1, h - 8));
            int v = 25 + rnd.nextInt(60);
            int a = 60 + rnd.nextInt(90);
            g.setColor(new Color(v, v, v, a));
            g.fillRect(px, py, 1, 1);
        }

        // outline
        g.setColor(new Color(220, 220, 220, 70));
        g.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

        g.dispose();
        return img;
    }
}