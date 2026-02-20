package dosbanana.gfx;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class HudPanel {

    private BufferedImage cache;
    private int cw = -1, ch = -1;

    public void paint(Graphics2D g, int x, int y, int w, int h) {
        if (cache == null || w != cw || h != ch) {
            cw = w;
            ch = h;
            cache = render(w, h);
        }
        g.drawImage(cache, x, y, null);
    }

    private BufferedImage render(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hintergrund leicht transparent, aber "massiv"
        Color bgOuter = new Color(8, 8, 8, 200);
        Color bgMid   = new Color(18, 18, 18, 210);
        Color bgInner = new Color(28, 28, 28, 220);

        // Drop shadow
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(3, 4, w - 4, h - 4, 14, 14);

        // Körper
        g.setColor(bgOuter);
        g.fillRoundRect(0, 0, w - 2, h - 2, 14, 14);

        g.setColor(bgMid);
        g.fillRoundRect(2, 2, w - 6, h - 6, 12, 12);

        g.setColor(bgInner);
        g.fillRoundRect(4, 4, w - 10, h - 10, 10, 10);

        // Top gloss band
        g.setColor(new Color(255, 255, 255, 35));
        g.fillRoundRect(6, 6, w - 14, Math.max(10, h / 3), 12, 12);

        // gold-ish accent line (passt zu Tiles)
        g.setColor(new Color(210, 170, 70, 70));
        g.drawLine(8, 10, w - 12, 10);

        // inner inset border
        g.setColor(new Color(0, 0, 0, 110));
        g.drawRoundRect(4, 4, w - 10, h - 10, 10, 10);

        // outer outline
        g.setColor(new Color(200, 200, 200, 70));
        g.drawRoundRect(0, 0, w - 2, h - 2, 14, 14);

        g.dispose();
        return img;
    }
}