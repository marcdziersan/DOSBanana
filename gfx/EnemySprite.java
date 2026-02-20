package dosbanana.gfx;

import java.awt.*;

public class EnemySprite {

    public void draw(Graphics2D g,
                     int x, int y, int w, int h,
                     double vx, double timeSec) {

        boolean facingRight = vx >= 0;

        // ---- Walk animation factors ----
        double speedAbs = Math.min(1.0, Math.abs(vx) / 90.0); // Enemy SPEED ~90
        double walkPhase = timeSec * (5.0 + 9.0 * speedAbs);

        int bob = (int) Math.round(Math.sin(walkPhase) * 2.0 * speedAbs); // Körper wippt
        int step = (int) Math.round(Math.sin(walkPhase) * 2.0 * speedAbs); // Füße alternieren

        int drawX = x;
        int drawY = y + bob;
        int drawW = w;
        int drawH = h;

        // ---- Colors ----
        Color body = new Color(200, 40, 40);
        Color bodyDark = new Color(120, 20, 20);
        Color bodyLight = new Color(240, 90, 90);

        // Schatten
        g.setColor(new Color(0, 0, 0, 120));
        g.fillOval(drawX + 2, drawY + drawH - 2, Math.max(8, drawW - 4), 6);

        // Körper außen (dunkel)
        g.setColor(bodyDark);
        g.fillRoundRect(drawX, drawY, drawW, drawH, 8, 8);

        // Körper innen
        g.setColor(body);
        g.fillRoundRect(drawX + 2, drawY + 2, drawW - 4, drawH - 4, 8, 8);

        // Highlight-Streifen je nach Richtung
        g.setColor(bodyLight);
        int stripeW = Math.max(2, drawW / 6);
        if (facingRight) {
            g.fillRoundRect(drawX + 3, drawY + 4, stripeW, Math.max(8, drawH - 10), 6, 6);
        } else {
            g.fillRoundRect(drawX + drawW - 3 - stripeW, drawY + 4, stripeW, Math.max(8, drawH - 10), 6, 6);
        }

        // Kontur
        g.setColor(Color.WHITE);
        g.drawRoundRect(drawX, drawY, drawW, drawH, 8, 8);

        // Spikes/Hörner
        g.setColor(new Color(230, 230, 230));
        int hornW = Math.max(3, drawW / 6);
        int hornH = Math.max(4, drawH / 6);

        g.fillPolygon(
                new int[]{drawX + 4, drawX + 4 + hornW, drawX + 4 + hornW / 2},
                new int[]{drawY + 3, drawY + 3, drawY - hornH},
                3
        );
        g.fillPolygon(
                new int[]{drawX + drawW - 4 - hornW, drawX + drawW - 4, drawX + drawW - 4 - hornW / 2},
                new int[]{drawY + 3, drawY + 3, drawY - hornH},
                3
        );

        // ---- Gesicht: Augen + Pupillen ----
        int eyeY = drawY + (int) (drawH * 0.20);
        int eyeW = Math.max(6, drawW / 4);
        int eyeH = Math.max(6, drawH / 4);

        int leftEyeX  = drawX + (int) (drawW * 0.18);
        int rightEyeX = drawX + (int) (drawW * 0.58);

        // Augen (weiß)
        g.setColor(new Color(250, 250, 250));
        g.fillOval(leftEyeX, eyeY, eyeW, eyeH);
        g.fillOval(rightEyeX, eyeY, eyeW, eyeH);

        // Augen-Kontur
        g.setColor(Color.BLACK);
        g.drawOval(leftEyeX, eyeY, eyeW, eyeH);
        g.drawOval(rightEyeX, eyeY, eyeW, eyeH);

        // Pupillen (blickt nach vorne + minimal "nervös" wobble)
        int pupilW = Math.max(2, eyeW / 3);
        int pupilH = Math.max(2, eyeH / 3);

        int pupilDir = facingRight ? +2 : -2;
        int pupilJitter = (int) Math.round(Math.sin(timeSec * 6.0) * 1.0); // kleiner Zitter

        int p1x = leftEyeX + eyeW / 2 - pupilW / 2 + pupilDir + pupilJitter;
        int p2x = rightEyeX + eyeW / 2 - pupilW / 2 + pupilDir + pupilJitter;
        int py = eyeY + eyeH / 2 - pupilH / 2;

        g.fillOval(p1x, py, pupilW, pupilH);
        g.fillOval(p2x, py, pupilW, pupilH);

        // ---- Mund + Zähne ----
        int mouthW = (int) (drawW * 0.55);
        int mouthH = Math.max(7, drawH / 5);
        int mouthX = facingRight ? drawX + (int) (drawW * 0.30) : drawX + (int) (drawW * 0.15);
        int mouthY = drawY + (int) (drawH * 0.42);

        g.setColor(new Color(30, 10, 10));
        g.fillRoundRect(mouthX, mouthY, mouthW, mouthH, 6, 6);
        g.setColor(new Color(255, 230, 230));
        g.drawRoundRect(mouthX, mouthY, mouthW, mouthH, 6, 6);

        g.setColor(Color.WHITE);
        int teeth = 4;
        for (int i = 0; i < teeth; i++) {
            int tx = mouthX + 2 + i * (mouthW - 4) / teeth;
            int tw = Math.max(2, (mouthW - 8) / teeth - 1);
            g.fillPolygon(
                    new int[]{tx, tx + tw, tx + tw / 2},
                    new int[]{mouthY + 2, mouthY + 2, mouthY + 2 + Math.max(4, mouthH / 2)},
                    3
            );
        }

        // ---- Füße (Schritt) ----
        g.setColor(new Color(80, 10, 10));
        int footY = drawY + drawH - 3;
        int footW = Math.max(5, drawW / 4);
        int footH = 3;

        // linker / rechter Fuß alternieren
        g.fillRect(drawX + 4, footY + step, footW, footH);
        g.fillRect(drawX + drawW - 4 - footW, footY - step, footW, footH);
    }
}