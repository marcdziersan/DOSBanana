package dosbanana.gfx;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public class BananaSprite {

    private BufferedImage bananaBase;   // gecachtes Banana-Image (ohne Animation)
    private int cw = -1, ch = -1;

    public void draw(Graphics2D g, int x, int y, int w, int h, double timeSec) {
        // Cache pro Größe
        if (bananaBase == null || w != cw || h != ch) {
            cw = w;
            ch = h;
            bananaBase = renderBananaBase(w, h);
        }

        // leichte Dreh-Illusion: X-Skalierung minimal
        double phase = timeSec * 3.5;
        double wobble = 0.88 + 0.12 * Math.sin(phase); // 0.88..1.00

        int cx = x + w / 2;
        int cy = y + h / 2;

        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.scale(wobble, 1.0);
        g.translate(-w / 2.0, -h / 2.0);

        g.drawImage(bananaBase, 0, 0, null);

        // winziger "glint" (sehr billig): kleiner heller Punkt wandert
        int sx = (int) (w * (0.25 + 0.10 * Math.sin(timeSec * 4.8)));
        int sy = (int) (h * (0.20 + 0.06 * Math.cos(timeSec * 3.6)));
        g.setColor(new Color(255, 255, 255, 55));
        g.fillOval(sx, sy, Math.max(2, w / 8), Math.max(2, h / 8));

        g.setTransform(old);
    }

    private BufferedImage renderBananaBase(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Farben (Banane + Schattierung)
        Color peelDark   = new Color(165, 125, 18);
        Color peelMid    = new Color(235, 200, 40);
        Color peelLight  = new Color(255, 245, 140);
        Color peelGreen  = new Color(125, 155, 45);   // leicht grünlich an Enden
        Color stemDark   = new Color(80, 55, 18);
        Color stemLight  = new Color(140, 105, 45);

        // --- Drop shadow (weich) ---
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(1, (int)(h * 0.55), w - 2, (int)(h * 0.35));

        // Wir zeichnen eine gebogene Banane als "Bauch" aus 2 Kurven (Path)
        // Koordinaten relativ zur Größe
        double x0 = w * 0.12;
        double y0 = h * 0.62;
        double x1 = w * 0.30;
        double y1 = h * 0.22;
        double x2 = w * 0.78;
        double y2 = h * 0.34;
        double x3 = w * 0.90;
        double y3 = h * 0.58;

        // Innenkante (dünner, darunter) für 3D
        double ix0 = w * 0.16;
        double iy0 = h * 0.66;
        double ix1 = w * 0.33;
        double iy1 = h * 0.30;
        double ix2 = w * 0.74;
        double iy2 = h * 0.42;
        double ix3 = w * 0.86;
        double iy3 = h * 0.62;

        // Banane-Shape: Außenkurve hin, Innenkurve zurück
        Path2D banana = new Path2D.Double();
        banana.moveTo(x0, y0);
        banana.curveTo(x1, y1, x2, y2, x3, y3);
        banana.curveTo(ix2, iy2, ix1, iy1, ix0, iy0);
        banana.closePath();

        // Grundfläche (mid)
        g.setColor(peelMid);
        g.fill(banana);

        // Außenkontur (dark)
        g.setStroke(new BasicStroke(Math.max(1f, w / 18f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(peelDark);
        g.draw(banana);

        // Unterkante / Bauchschatten (dunkle Linie)
        Path2D belly = new Path2D.Double();
        belly.moveTo(ix0, iy0);
        belly.curveTo(ix1, iy1 + h*0.10, ix2, iy2 + h*0.10, ix3, iy3);
        g.setStroke(new BasicStroke(Math.max(1f, w / 24f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(peelDark.getRed(), peelDark.getGreen(), peelDark.getBlue(), 180));
        g.draw(belly);

        // Highlight-Band (oben, light) - gibt Tiefe
        Path2D hi = new Path2D.Double();
        hi.moveTo(w * 0.22, h * 0.54);
        hi.curveTo(w * 0.36, h * 0.26, w * 0.70, h * 0.34, w * 0.82, h * 0.52);
        g.setStroke(new BasicStroke(Math.max(2f, w / 14f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(peelLight.getRed(), peelLight.getGreen(), peelLight.getBlue(), 160));
        g.draw(hi);

        // Leicht grünliche Enden (dezent)
        g.setStroke(new BasicStroke(Math.max(2f, w / 16f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(peelGreen.getRed(), peelGreen.getGreen(), peelGreen.getBlue(), 120));
        g.drawArc((int)(w*0.08), (int)(h*0.50), (int)(w*0.22), (int)(h*0.28), 210, 120);
        g.drawArc((int)(w*0.72), (int)(h*0.44), (int)(w*0.22), (int)(h*0.30), 320, 120);

        // Stiel links (kleiner dunkler Knubbel)
        int stemW = Math.max(3, w / 8);
        int stemH = Math.max(3, h / 8);
        int stemX = (int)(w * 0.08);
        int stemY = (int)(h * 0.52);
        g.setColor(stemDark);
        g.fillRoundRect(stemX, stemY, stemW, stemH, 3, 3);
        g.setColor(new Color(stemLight.getRed(), stemLight.getGreen(), stemLight.getBlue(), 160));
        g.drawRoundRect(stemX, stemY, stemW, stemH, 3, 3);

        // Kleiner "Schalenstrich" (braune Faser-Linien) -> billig, aber wirkt
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(120, 85, 20, 90));
        for (int i = 0; i < 3; i++) {
            int sx = (int)(w * (0.34 + i * 0.14));
            int sy = (int)(h * (0.48 - i * 0.03));
            g.drawLine(sx, sy, sx + (int)(w * 0.10), sy + (int)(h * 0.12));
        }

        // Crisp outline (leicht hell)
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(255, 255, 255, 70));
        g.draw(banana);

        g.dispose();
        return img;
    }
}
