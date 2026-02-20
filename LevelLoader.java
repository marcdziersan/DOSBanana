package dosbanana;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LevelLoader {

    public record Level(char[][] map, int cols, int rows) {}

    private static final String RES_PREFIX = "res:";

    public static boolean existsFile(String filePath) {
        return resolveToExistingFile(filePath) != null || openResourceStream(filePath) != null;
    }

    public static Level loadFile(String filePath) {
        try (InputStream in = openBestStream(filePath)) {
            if (in == null) {
                throw new IllegalStateException("Level file not found (file/jar-dir/resource): " + filePath);
            }
            return parseLevel(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> discoverLevelFiles(String levelsDir) {
        File directDir = new File(levelsDir);
        if (directDir.isDirectory()) {
            return listLevelFilesFromDir(directDir);
        }
        File jarDir = getJarDirSafe();
        if (jarDir != null) {
            File besideJar = new File(jarDir, levelsDir);
            if (besideJar.isDirectory()) {
                return listLevelFilesFromDir(besideJar);
            }
        }
        List<String> res = new ArrayList<>();
        for (int i = 1; i <= 99; i++) {
            String rp = "/dosbanana/" + levelsDir.replace('\\', '/').replaceAll("^/+", "") + "/level" + i + ".txt";
            InputStream test = LevelLoader.class.getResourceAsStream(rp);
            if (test != null) {
                try { test.close(); } catch (IOException ignored) {}
                res.add(RES_PREFIX + rp);
            } else {
                if (!res.isEmpty()) {
                    boolean anyNext = false;
                    for (int k = 1; k <= 2; k++) {
                        String rp2 = "/dosbanana/" + levelsDir.replace('\\', '/').replaceAll("^/+", "") + "/level" + (i + k) + ".txt";
                        InputStream t2 = LevelLoader.class.getResourceAsStream(rp2);
                        if (t2 != null) {
                            try { t2.close(); } catch (IOException ignored) {}
                            anyNext = true;
                            break;
                        }
                    }
                    if (!anyNext) break;
                }
            }
        }
        return res;
    }
    private static Level parseLevel(InputStream in) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String s;
            while ((s = br.readLine()) != null) lines.add(s);
        }

        int rows = lines.size();
        int cols = 0;
        for (String l : lines) cols = Math.max(cols, l.length());

        char[][] map = new char[rows][cols];
        for (int y = 0; y < rows; y++) {
            String l = lines.get(y);
            for (int x = 0; x < cols; x++) {
                map[y][x] = (x < l.length()) ? l.charAt(x) : ' ';
            }
        }
        return new Level(map, cols, rows);
    }

    private static List<String> listLevelFilesFromDir(File dir) {
        File[] files = dir.listFiles((d, name) ->
                name.toLowerCase(Locale.ROOT).matches("level\\d+\\.txt"));

        if (files == null || files.length == 0) return List.of();

        List<File> list = Arrays.asList(files);
        list.sort(Comparator.comparingInt(LevelLoader::extractLevelNumber));

        List<String> paths = new ArrayList<>();
        for (File f : list) paths.add(f.getPath());
        return paths;
    }

    private static int extractLevelNumber(File f) {
        String name = f.getName().toLowerCase(Locale.ROOT);
        int start = "level".length();
        int end = name.indexOf(".txt");
        try {
            return Integer.parseInt(name.substring(start, end));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private static InputStream openBestStream(String filePath) throws IOException {

        if (filePath != null && filePath.startsWith(RES_PREFIX)) {
            return openResourceStream(filePath.substring(RES_PREFIX.length()));
        }

        File existing = resolveToExistingFile(filePath);
        if (existing != null) {
            return new FileInputStream(existing);
        }

        InputStream r1 = openResourceStream(filePath);
        if (r1 != null) return r1;

        String base = new File(filePath).getName();
        String rGuess = "/dosbanana/levels/" + base;
        return openResourceStream(rGuess);
    }

    private static File resolveToExistingFile(String filePath) {
        if (filePath == null || filePath.isBlank()) return null;

        File f = new File(filePath);
        if (f.isFile()) return f;

        File jarDir = getJarDirSafe();
        if (jarDir != null) {
            File beside = new File(jarDir, filePath);
            if (beside.isFile()) return beside;

            String base = new File(filePath).getName();
            File besideLevels = new File(new File(jarDir, "levels"), base);
            if (besideLevels.isFile()) return besideLevels;
        }

        return null;
    }

    private static InputStream openResourceStream(String resourcePath) {
        if (resourcePath == null) return null;

        String rp = resourcePath.trim();

        rp = rp.replace('\\', '/');
        if (!rp.startsWith("/")) rp = "/" + rp;

        InputStream in = LevelLoader.class.getResourceAsStream(rp);
        if (in != null) return in;

        if (!rp.startsWith("/dosbanana/")) {
            String base = new File(rp).getName();
            String guess = "/dosbanana/levels/" + base;
            return LevelLoader.class.getResourceAsStream(guess);
        }

        return null;
    }

    private static File getJarDirSafe() {
        try {
            URI uri = LevelLoader.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            File loc = new File(uri);
            if (loc.isDirectory()) return loc;
            File parent = loc.getParentFile();
            return (parent != null && parent.exists()) ? parent : null;
        } catch (Exception e) {
            return null;
        }
    }
}