package dosbanana.gfx;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class BananaSprite {

    private BufferedImage coinBase;
    private int cw = -1, ch = -1;

    public void draw(Graphics2D g, int x, int y, int w, int h, double timeSec) {
        if (coinBase == null || w != cw || h != ch) {
            cw = w;
            ch = h;
            coinBase = renderCoinBase(w, h);
        }

        double phase = timeSec * 3.5;
        double wobble = 0.85 + 0.15 * Math.sin(phase);

        int cx = x + w / 2;
        int cy = y + h / 2;

        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.scale(wobble, 1.0);
        g.translate(-w / 2.0, -h / 2.0);

        g.drawImage(coinBase, 0, 0, null);

        int sx = (int)(w * (0.30 + 0.08 * Math.sin(timeSec * 5.0)));
        int sy = (int)(h * 0.25);
        g.setColor(new Color(255, 255, 255, 55));
        g.fillOval(sx, sy, Math.max(2, w / 6), Math.max(2, h / 6));

        g.setTransform(old);
    }

    private BufferedImage renderCoinBase(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color goldDark  = new Color(130, 95, 25);
        Color goldMid   = new Color(190, 150, 45);
        Color goldLight = new Color(255, 225, 120);

        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(1, 2, w - 2, h - 2);

        g.setColor(goldDark);
        g.fillOval(0, 0, w - 1, h - 1);

        int inset = Math.max(2, Math.min(w, h) / 7);
        g.setColor(goldMid);
        g.fillOval(inset, inset, w - 1 - 2 * inset, h - 1 - 2 * inset);

        g.setColor(new Color(goldLight.getRed(), goldLight.getGreen(), goldLight.getBlue(), 160));
        g.drawArc(1, 1, w - 3, h - 3, 120, 120);

        g.setColor(new Color(60, 40, 10, 140));
        g.drawArc(1, 1, w - 3, h - 3, 300, 120);

        g.setColor(new Color(255, 240, 170, 120));
        g.drawOval(inset, inset, w - 1 - 2 * inset, h - 1 - 2 * inset);

        g.setColor(new Color(90, 65, 15, 120));
        int ex = w / 2 - Math.max(2, w / 10);
        int ey = h / 2 - Math.max(2, h / 10);
        int ew = Math.max(4, w / 5);
        int eh = Math.max(4, h / 5);
        g.drawRoundRect(ex, ey, ew, eh, 4, 4);

        g.setColor(new Color(255, 255, 255, 35));
        g.fillOval(inset + 1, inset + 1, w - 1 - 2 * inset, Math.max(4, h / 2));

        g.setColor(new Color(255, 255, 255, 70));
        g.drawOval(0, 0, w - 1, h - 1);

        g.dispose();
        return img;
    }
}