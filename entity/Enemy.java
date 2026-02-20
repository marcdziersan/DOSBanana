package dosbanana.entity;

import dosbanana.Rect;
import dosbanana.World;

public class Enemy extends Entity {
    private static final double SPEED = 90;
    private boolean moveRight = true;
    private boolean alive = true;

    public Enemy(int x, int y) {
        super(x, y, 26, 26);
        vx = SPEED;
    }

    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }

    @Override
    public void update(World world, double dt) {
        if (!alive) return;

        vx = moveRight ? SPEED : -SPEED;

        x += vx * dt;
        if (world.collidesSolid(getBounds())) {
            x -= vx * dt;
            moveRight = !moveRight;
        } else {
            int frontX = (int)(x + (moveRight ? w : 0));
            int footY  = (int)(y + h + 1);

            if (!world.isSolidAt(frontX, footY)) {
                moveRight = !moveRight;
            }
        }
    }
}
