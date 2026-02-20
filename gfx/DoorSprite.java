package dosbanana.gfx;

import java.awt.*;

public class DoorSprite implements Sprite {
    @Override
    public void draw(Graphics2D g, int x, int y, int w, int h) {

        g.setColor(new Color(120, 70, 20));
        g.fillRect(x, y, w, h);

        g.setColor(new Color(200, 160, 80));
        g.drawRect(x, y, w, h);

        g.setColor(Color.YELLOW);
        g.fillOval(x + w - 7, y + h / 2, 4, 4);
    }
}
