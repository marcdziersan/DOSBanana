# ▣ DOS Banana

**Version 1.1.0 -- Flame & Fall Update**

> Java Swing • Jump'n'Run • 1990/1991 Shareware-Vibe\
> Ein Lernprojekt mit bewusst klassischem Aufbau.

<img src="assets/thumb.png">

# 🎮 Was ist DOS Banana?

**DOS Banana** ist ein kleines 2D-Retro-Jump'n'Run im Stil früher
PC-Shareware-Games der frühen 90er Jahre.

Das Projekt wurde bewusst ohne Game-Engine umgesetzt, um Rendering,
Game-Loop, Kollision, Physik und Entity-Management vollständig selbst zu
implementieren.

Es ist ein reines Java/Swing Lernprojekt, das klassische
Arcade-Mechaniken mit sauberer Struktur verbindet.


# 🚀 Features

## 🟡 Gameplay

-   Alle Bananen sammeln → Tür öffnet sich\
-   Gegner vermeiden oder von oben „stompen"\
-   Mehrere Level als klassische Text-Tilemaps

## 🔥 Version 1.1.0

-   Flame Power-Up (P) sammeln\
-   Flammen in Laufrichtung schießen (F / CTRL)\
-   Herz-System mit Half-Hearts\
-   Fallschaden bei hohen Stürzen


# ⌨ Controls

  Aktion           Taste
  ---------------- ---------------------
  Bewegung         A / D oder ← / →
  Springen         SPACE / W / ↑
  Flammen-Schuss   F oder CTRL
  Restart          R (Endscreen) / ESC
  Quit             Q


# 🛠 Technische Umsetzung

## Game Loop

-   javax.swing.Timer (\~60 FPS)
-   Input → Update → Kollision → Render

## Rendering

-   2D via Graphics2D
-   paintComponent()
-   Kein Framework, keine Engine

## Physik

-   Schwerkraft
-   Sprunggeschwindigkeit
-   Bounding-Box Kollision
-   Fall-Distanz Tracking

## Level-System

Textdateien mit Symbolen:

  Symbol   Bedeutung
  -------- ----------------
  \#       Solid Tile
  @        Player Start
  B        Banana
  X        Enemy
  P        Flame Power-Up
  E        Exit


# ▶ Build & Run

## Kompilieren

javac dosbanana/*.java dosbanana/entity/*.java dosbanana/gfx/\*.java

## Starten

java dosbanana.Main


# 🧠 Lernziele

-   Game Loop verstehen
-   Physik implementieren
-   Collision Detection
-   Entity Lifecycle
-   Strukturierte Java-Projekte

© 2026 Marcus Dziersan\
Lernprojekt -- Java Swing Retro Game
