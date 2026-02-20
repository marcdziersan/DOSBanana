package dosbanana.entity;

import dosbanana.Rect;
import dosbanana.World;

import java.awt.*;

public abstract class Entity {
    public double x, y;
    public double vx, vy;
    public int w, h;

    protected Entity(double x, double y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    public Rect getBounds() {
        return new Rect((int) x, (int) y, w, h);
    }

    public abstract void update(World world, double dt);

    public void drawDebug(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        Rect r = getBounds();
        g.drawRect(r.x, r.y, r.w, r.h);
    }
}
