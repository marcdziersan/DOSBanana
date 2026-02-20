package dosbanana.gfx;

import java.awt.*;

public class PlayerSprite {
    public void draw(Graphics2D g,
                     int x, int y, int w, int h,
                     double vx, double vy, boolean onGround,
                     double timeSec,
                     boolean invulnerable) {

        boolean facingRight = vx >= 0;

        // ---- HIT FX (blink + tiny shake) ----
        // Wenn invulnerable: blinkt schnell. Zusätzlich 1px shake.
        boolean hitBlink = invulnerable && ((int)(timeSec * 18) % 2 == 0);
        int shake = invulnerable ? (int)Math.round(Math.sin(timeSec * 40) * 1.5) : 0;

        // ---- Wobble / Stretch ----
        double speedAbs = Math.min(1.0, Math.abs(vx) / 220.0);
        double walkPhase = timeSec * (6.0 + 10.0 * speedAbs);

        int bob = 0;
        int stretchY = 0;

        if (onGround) {
            bob = (int) Math.round(Math.sin(walkPhase) * 2.0 * speedAbs);
        } else {
            if (vy < -40) stretchY = -2;
            else if (vy > 120) stretchY = +2;
        }

        int drawX = x + shake;
        int drawY = y + bob;
        int drawW = w;
        int drawH = h + (-stretchY);

        if (stretchY != 0) drawY += stretchY / 2;

        // ---- Farben (bei Hit leicht heller) ----
        Color body = hitBlink ? new Color(140, 190, 255) : new Color(30, 120, 220);
        Color bodyDark = hitBlink ? new Color(60, 110, 190) : new Color(10, 60, 140);
        Color bodyLight = hitBlink ? new Color(200, 230, 255) : new Color(80, 170, 255);

        // Schatten
        g.setColor(new Color(0, 0, 0, 120));
        g.fillOval(drawX + 2, drawY + drawH - 2, Math.max(8, drawW - 4), 6);

        // Körper außen
        g.setColor(bodyDark);
        g.fillRoundRect(drawX, drawY, drawW, drawH, 10, 10);

        // Körper innen
        g.setColor(body);
        g.fillRoundRect(drawX + 2, drawY + 2, drawW - 4, drawH - 4, 10, 10);

        // Highlight-Streifen
        g.setColor(bodyLight);
        int stripeW = Math.max(2, drawW / 6);
        if (facingRight) {
            g.fillRoundRect(drawX + 3, drawY + 4, stripeW, Math.max(8, drawH - 10), 8, 8);
        } else {
            g.fillRoundRect(drawX + drawW - 3 - stripeW, drawY + 4, stripeW, Math.max(8, drawH - 10), 8, 8);
        }

        // Kontur
        g.setColor(Color.WHITE);
        g.drawRoundRect(drawX, drawY, drawW, drawH, 10, 10);

        // ---- Gesicht ----
        int eyeY = drawY + (int) (drawH * 0.18);
        int eyeW = Math.max(6, drawW / 4);
        int eyeH = Math.max(6, drawH / 4);

        int leftEyeX  = drawX + (int) (drawW * 0.18);
        int rightEyeX = drawX + (int) (drawW * 0.58);

        // Blink selten (normal) – beim Hit lassen wir es an
        boolean blink = !invulnerable && (Math.sin(timeSec * 2.7) > 0.98);

        if (invulnerable && !hitBlink) {
            // in den "off" frames: Augen als X (kleiner Schock-Effekt)
            g.setColor(Color.WHITE);
            drawXEyeX(g, leftEyeX, eyeY, eyeW, eyeH);
            drawXEyeX(g, rightEyeX, eyeY, eyeW, eyeH);
        } else if (!blink) {
            g.setColor(new Color(250, 250, 250));
            g.fillOval(leftEyeX, eyeY, eyeW, eyeH);
            g.fillOval(rightEyeX, eyeY, eyeW, eyeH);

            g.setColor(Color.BLACK);
            g.drawOval(leftEyeX, eyeY, eyeW, eyeH);
            g.drawOval(rightEyeX, eyeY, eyeW, eyeH);

            int pupilW = Math.max(2, eyeW / 3);
            int pupilH = Math.max(2, eyeH / 3);
            int pupilOffset = facingRight ? +2 : -2;

            int p1x = leftEyeX + eyeW / 2 - pupilW / 2 + pupilOffset;
            int p2x = rightEyeX + eyeW / 2 - pupilW / 2 + pupilOffset;
            int py = eyeY + eyeH / 2 - pupilH / 2;

            g.fillOval(p1x, py, pupilW, pupilH);
            g.fillOval(p2x, py, pupilW, pupilH);
        } else {
            g.setColor(Color.BLACK);
            g.drawLine(leftEyeX, eyeY + eyeH / 2, leftEyeX + eyeW, eyeY + eyeH / 2);
            g.drawLine(rightEyeX, eyeY + eyeH / 2, rightEyeX + eyeW, eyeY + eyeH / 2);
        }

        // Mund
        int mouthW = (int) (drawW * 0.55);
        int mouthH = Math.max(7, drawH / 6);
        int mouthX = facingRight ? drawX + (int) (drawW * 0.28) : drawX + (int) (drawW * 0.15);
        int mouthY = drawY + (int) (drawH * 0.45);

        g.setColor(new Color(20, 10, 10));
        g.fillRoundRect(mouthX, mouthY, mouthW, mouthH, 6, 6);
        g.setColor(new Color(240, 240, 240));
        g.drawRoundRect(mouthX, mouthY, mouthW, mouthH, 6, 6);

        // Zähne
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

        // Füße
        int footY = drawY + drawH - 3;
        int footW = Math.max(5, drawW / 4);
        int footH = 3;

        int step = (onGround && speedAbs > 0.05) ? (int) Math.round(Math.sin(walkPhase) * 2.0) : 0;

        g.setColor(new Color(10, 30, 70));
        g.fillRect(drawX + 4, footY + step, footW, footH);
        g.fillRect(drawX + drawW - 4 - footW, footY - step, footW, footH);

        // Extra Hit Overlay (ganz leichtes Weiß-Blitzband)
        if (hitBlink) {
            g.setColor(new Color(255, 255, 255, 50));
            g.fillRoundRect(drawX + 2, drawY + 2, drawW - 4, Math.max(6, drawH / 3), 10, 10);
        }
    }

    private void drawXEyeX(Graphics2D g, int x, int y, int w, int h) {
        int cx1 = x + 1;
        int cx2 = x + w - 2;
        int cy1 = y + 1;
        int cy2 = y + h - 2;
        g.drawLine(cx1, cy1, cx2, cy2);
        g.drawLine(cx1, cy2, cx2, cy1);
    }
}