package dosbanana;

import dosbanana.entity.*;
import dosbanana.gfx.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dosbanana.Rect;

public class World {
    public static final int TILE = 32;

    private final char[][] map;
    private final int cols, rows;

    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> pendingAdd = new ArrayList<>(); // queued spawns during update loop
    private Player player;

    private Door door;
    private boolean exitTriggered = false;

    private final HudPanel hudPanel = new HudPanel();

    private final TileSprite tileSprite = new TileSprite();
    private final BananaSprite bananaSprite = new BananaSprite();

    private final FlamePickupSprite flamePickupSprite = new FlamePickupSprite();
    private final FlameShotSprite flameShotSprite = new FlameShotSprite();
    private final HeartSprite heartSprite = new HeartSprite();

    // PlayerSprite ist jetzt "smart" und braucht mehr Parameter -> keine Sprite-Interface Nutzung
    private final PlayerSprite playerSprite = new PlayerSprite();

    private final DoorSprite doorSprite = new DoorSprite();

    // EnemySprite ist ebenfalls "smart" (facingRight)
    private final EnemySprite enemySprite = new EnemySprite();

    private int score = 0;
    private int bananasRemaining = 0;

    // Exit-Tile-Position
    private int exitTx = -1;
    private int exitTy = -1;

    // Player state
    private int maxHearts = 3;
    private int healthHalf = 6; // half-hearts (2 per heart)

    // Animation time (Sekunden)
    private double animTime = 0.0;

    public World(String levelFilePath) { this(levelFilePath, 0, 3); }
    public World(String levelFilePath, int startScore) { this(levelFilePath, startScore, 3); }

    public World(String levelFilePath, int startScore, int startLives) {
        this(levelFilePath, startScore, startLives, startLives * 2);
    }

    // startLives = max hearts; startHealthHalf lets us carry over half-heart precision between levels
    public World(String levelFilePath, int startScore, int startLives, int startHealthHalf) {
        this.score = startScore;
        this.maxHearts = Math.max(1, startLives);
        this.healthHalf = Math.min(this.maxHearts * 2, Math.max(0, startHealthHalf));

        LevelLoader.Level lvl = LevelLoader.loadFile(levelFilePath);
        this.map = lvl.map();
        this.cols = lvl.cols();
        this.rows = lvl.rows();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                char c = map[y][x];
                int px = x * TILE;
                int py = y * TILE;

                if (c == '@') {
                    player = new Player(px, py);
                    entities.add(player);
                    map[y][x] = ' ';
                } else if (c == 'B') {
                    entities.add(new Banana(px + 8, py + 8));
                    bananasRemaining++;
                    map[y][x] = ' ';
                } else if (c == 'P') {
                    // Flame power-up
                    entities.add(new FlamePickup(px + 7, py + 7));
                    map[y][x] = ' ';
                } else if (c == 'E') {
                    exitTx = x;
                    exitTy = y;
                    map[y][x] = ' ';
                } else if (c == 'X') {
                    entities.add(new Enemy(px, py + (TILE - 26))); // Enemy auf Boden des Tiles
                    map[y][x] = ' ';
                }
            }
        }

        if (player == null) {
            player = new Player(64, 64);
            entities.add(player);
        }
    }

    public Player getPlayer() { return player; }
    public int getScore() { return score; }
    public int getLives() { return (int) Math.ceil(healthHalf / 2.0); }
    public int getHealthHalf() { return healthHalf; }
    public int getMaxHearts() { return maxHearts; }
    public int getBananasRemaining() { return bananasRemaining; }

    public void damagePlayerHalfHeart() {
        healthHalf = Math.max(0, healthHalf - 1);
    }

    public void damagePlayerFullHeart() {
        healthHalf = Math.max(0, healthHalf - 2);
    }

    public void spawnFlameShot(Player p) {
        int sx = (int) (p.isFacingRight() ? (p.x + p.w - 2) : (p.x - 12));
        int sy = (int) (p.y + p.h * 0.45);
        pendingAdd.add(new FlameShot(sx, sy, p.isFacingRight()));
    }

    public boolean consumeExitTriggered() {
        if (exitTriggered) {
            exitTriggered = false;
            return true;
        }
        return false;
    }

    public void update(double dt) {
        // Animation time läuft immer mit
        animTime += dt;

        // Update loop must not structurally modify entities while iterating.
        // (Projectiles can be spawned from Player.update on key press.)
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).update(this, dt);
        }

        // apply queued spawns after entity updates
        if (!pendingAdd.isEmpty()) {
            entities.addAll(pendingAdd);
            pendingAdd.clear();
        }

        // bananas collect
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            if (e instanceof Banana b && player.getBounds().intersects(b.getBounds())) {
                it.remove();
                bananasRemaining--;
                score += 10;
            }
            if (e instanceof FlamePickup fp && player.getBounds().intersects(fp.getBounds())) {
                it.remove();
                player.grantFlame();
                score += 25;
            }
        }

        // spawn door when all bananas collected
        if (bananasRemaining <= 0 && door == null && exitTx >= 0) {
            spawnDoorAtExitTile();
        }

        // enemy interactions
        handleEnemyContacts();

        // flame shots vs enemies / walls
        handleProjectiles();

        // door enter?
        if (door != null && player.getBounds().intersects(door.getBounds())) {
            exitTriggered = true;
        }
    }

    private void handleProjectiles() {
        List<Entity> toRemove = new ArrayList<>();

        // remove expired shots
        for (Entity ent : entities) {
            if (ent instanceof FlameShot fs && fs.isExpired()) toRemove.add(ent);
        }

        Rect pr = player.getBounds();
        for (Entity ent : entities) {
            if (!(ent instanceof FlameShot shot)) continue;

            Rect sr = shot.getBounds();
            if (sr.intersects(pr)) continue;

            for (Entity tgt : entities) {
                if (!(tgt instanceof Enemy enemy)) continue;
                if (!enemy.isAlive()) continue;
                if (!sr.intersects(enemy.getBounds())) continue;

                enemy.kill();
                score += 40;
                toRemove.add(enemy);
                toRemove.add(shot);
                break;
            }
        }

        // apply removals
        if (!toRemove.isEmpty()) entities.removeAll(toRemove);
    }

private void handleEnemyContacts() {
    Rect pr = player.getBounds();

    Iterator<Entity> it = entities.iterator();
    while (it.hasNext()) {
        Entity e = it.next();
        if (!(e instanceof Enemy enemy)) continue;
        if (!enemy.isAlive()) { 
            // Optional: dead enemies sofort aufräumen
            it.remove();
            continue;
        }

        Rect er = enemy.getBounds();
        if (!pr.intersects(er)) continue;

        // STOMP check: Player kommt von oben (fällt) und ist oberhalb Enemy
        boolean falling = player.vy > 0;
        int playerBottom = pr.y + pr.h;
        int enemyTop = er.y;

        // Toleranz: wenn Player-Bottom nahe Enemy-Top und falling -> stomp
        boolean stomp = falling && playerBottom <= enemyTop + 8;

        if (stomp) {
            enemy.kill();
            it.remove();           // ✅ Enemy wirklich entfernen
            score += 50;
            player.bounceFromStomp();
        } else {
            // side hit
            if (!player.isInvulnerable()) {
                damagePlayerFullHeart();
                player.takeHit();

                // kleiner Knockback
                if (player.x < enemy.x) player.x -= 20;
                else player.x += 20;
            }
        }
    }
}

    private void spawnDoorAtExitTile() {
        int px = exitTx * TILE + (TILE - 24) / 2;
        int py = exitTy * TILE + (TILE - 32);

        door = new Door(px, py);
        entities.add(door);
    }

    public void render(Graphics2D g) {
        // tiles
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (map[y][x] == '#') {
                    tileSprite.draw(g, x * TILE, y * TILE, TILE, TILE);
                }
            }
        }

        // entities
        for (Entity e : entities) {
            if (e instanceof Player p) {
                playerSprite.draw(
    g,
    (int)p.x, (int)p.y, (int)p.w, (int)p.h,
    p.vx, p.vy,
    p.isOnGround(),
    animTime,
    p.isInvulnerable()
);

            } else if (e instanceof Banana) {
    bananaSprite.draw(g, (int) e.x, (int) e.y, (int) e.w, (int) e.h, animTime);
} else if (e instanceof Door) {
                doorSprite.draw(g, (int) e.x, (int) e.y, (int) e.w, (int) e.h);

            } else if (e instanceof FlamePickup) {
                flamePickupSprite.draw(g, (int) e.x, (int) e.y, (int) e.w, (int) e.h, animTime);

            } else if (e instanceof FlameShot) {
                flameShotSprite.draw(g, (int) e.x, (int) e.y, (int) e.w, (int) e.h, animTime);

            } else if (e instanceof Enemy en) {
    enemySprite.draw(
            g,
            (int) en.x, (int) en.y, (int) en.w, (int) en.h,
            en.vx,
            animTime
    );
} else {
                e.drawDebug(g);
            }
        }

// HUD (mit cached Panel)
int hudX = 8;
int hudY = 6;
int lineH = 14;                 // für Font ~10 gut lesbar
int padding = 10;

String l1 = "SCORE:  " + score;
String l2 = "COINS:  " + Math.max(0, bananasRemaining);
String l3 = player.hasFlame() ? "POWER:  FLAME" : "POWER:  none";
String l4 = (door == null && exitTx >= 0) ? "EXIT:   locked" : "EXIT:   OPEN!";

Font hudFont = new Font("Monospaced", Font.BOLD, 10);

// Breite dynamisch nach Text messen
FontMetrics fm = g.getFontMetrics(hudFont);
int textW = Math.max(Math.max(fm.stringWidth(l1), fm.stringWidth(l2)),
            Math.max(fm.stringWidth(l3), fm.stringWidth(l4)));

int hudW = textW + padding * 2;
int hudH = padding + lineH * 4 + 6 + 18; // extra space for hearts

// Panel malen (cached)
hudPanel.paint(g, hudX, hudY, hudW, hudH);

// Text drüber
g.setFont(hudFont);
g.setColor(new Color(0, 255, 80)); // leicht neon-grün, passt zum retro look

int tx = hudX + padding;
int ty = hudY + padding + 6;

g.drawString(l1, tx, ty);
g.drawString(l2, tx, ty + lineH);
g.drawString(l3, tx, ty + lineH * 2);
g.drawString(l4, tx, ty + lineH * 3);

// hearts row
int heartsY = ty + lineH * 4 + 4;
drawHearts(g, tx, heartsY);
    }

    private void drawHearts(Graphics2D g, int x, int y) {
        int size = 14;
        int gap = 4;

        for (int i = 0; i < maxHearts; i++) {
            int hx = x + i * (size + gap);
            int remainingHalf = healthHalf - i * 2;

            HeartSprite.HeartFill fill;
            if (remainingHalf >= 2) fill = HeartSprite.HeartFill.FULL;
            else if (remainingHalf == 1) fill = HeartSprite.HeartFill.HALF;
            else fill = HeartSprite.HeartFill.EMPTY;

            heartSprite.draw(g, hx, y, size, fill);
        }
    }

    public boolean isSolidAt(int px, int py) {
        int tx = px / TILE;
        int ty = py / TILE;
        if (tx < 0 || ty < 0 || tx >= cols || ty >= rows) return true;
        return map[ty][tx] == '#';
    }

    public boolean collidesSolid(Rect r) {
        return isSolidAt(r.x, r.y)
                || isSolidAt(r.x + r.w - 1, r.y)
                || isSolidAt(r.x, r.y + r.h - 1)
                || isSolidAt(r.x + r.w - 1, r.y + r.h - 1);
    }
}