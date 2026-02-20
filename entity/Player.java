package dosbanana.entity;

import dosbanana.Rect;
import dosbanana.World;

public class Player extends Entity {
    private boolean moveLeft, moveRight;
    private boolean jumpRequested;
    private boolean onGround;

    private static final double SPEED = 220;
    private static final double GRAVITY = 1000;
    private static final double JUMP_V = -500;

    private static final double STOMP_BOUNCE_V = -420;
    private double invulnTimer = 0;

    public Player(int x, int y) {
        super(x, y, 26, 30);
    }

    public void setMoveLeft(boolean b) { moveLeft = b; }
    public void setMoveRight(boolean b) { moveRight = b; }
    public void requestJump() { jumpRequested = true; }

    public boolean isInvulnerable() { return invulnTimer > 0; }
    public boolean isOnGround() { return onGround; }
    public void tickInvuln(double dt) { invulnTimer = Math.max(0, invulnTimer - dt); }

    public void bounceFromStomp() {
        vy = STOMP_BOUNCE_V;
        onGround = false;
    }

    public void takeHit() {
        invulnTimer = 0.8;
    }

    @Override
    public void update(World world, double dt) {
        tickInvuln(dt);

        vx = 0;
        if (moveLeft)  vx -= SPEED;
        if (moveRight) vx += SPEED;

        vy += GRAVITY * dt;

        if (jumpRequested && onGround) {
            vy = JUMP_V;
            onGround = false;
        }
        jumpRequested = false;

        x += vx * dt;
        if (world.collidesSolid(getBounds())) {
            x -= vx * dt;
            int step = (vx > 0) ? 1 : -1;
            if (vx != 0) {
                while (!world.collidesSolid(getBounds())) x += step;
                x -= step;
            }
            vx = 0;
        }

        y += vy * dt;
        if (world.collidesSolid(getBounds())) {
            y -= vy * dt;

            int step = (vy > 0) ? 1 : -1;
            while (!world.collidesSolid(getBounds())) y += step;
            y -= step;

            if (vy > 0) onGround = true;
            vy = 0;
        } else {
            onGround = false;
        }
    }
}
