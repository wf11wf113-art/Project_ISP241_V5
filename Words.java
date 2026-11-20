                                                                                                                                    //Аллазкани
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import java.nio.file.Path;
import java.util.*;

public class Words {
    private static final int WORD_COUNT = 10;
    private record Puzzle(char[][] grid, List<String> words) {}
    private static final String LETTERS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    private static final Set<String> SIZES = Set.of("10x10", "11x10", "10x11", "12x10", "10x12");

    private static List<String> readWords(Path file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        var document = factory.newDocumentBuilder().parse(file.toFile());
        var arrays = document.getElementsByTagName("string-array");
        Set<String> words = new LinkedHashSet<>();
        for (int i = 0; i < arrays.getLength(); i++) {
            Element array = (Element) arrays.item(i);
            if (!"words".equals(array.getAttribute("name"))) continue;
            var items = array.getElementsByTagName("item");
            for (int j = 0; j < items.getLength(); j++) {
                String word = items.item(j).getTextContent().strip().toUpperCase(Locale.ROOT);
                if (!word.matches("[А-ЯЁ]+")) {
                    throw new IllegalArgumentException("Недопустимое слово в XML: " + word);
                }
                words.add(word);
            }
        }
        if (words.size() < WORD_COUNT) {
            throw new IllegalArgumentException("В массиве words должно быть хотя бы 10 разных русских слов.");
        }
        return List.copyOf(words);
    }

                                                                                                                                        //Плюшко
private record Line(int row, int col, int dr, int dc) {
        int r(int i) { return row + i * dr; }
        int c(int i) { return col + i * dc; }
        boolean forward() { return dr > 0 || (dr == 0 && dc >= 0); }
    }

    private static Puzzle generate(int width, int height, List<String> dictionary, Random random) {
        Map<Integer, List<Line>> paths = new HashMap<>();
        for (String word : dictionary) {
            paths.computeIfAbsent(word.length(), length -> lines(width, height, length));
        }
        for (int attempt = 0; attempt < 500; attempt++) {
            char[][] grid = new char[height][width];
            List<String> selected = new ArrayList<>();
            List<String> pool = new ArrayList<>(dictionary);
            Collections.shuffle(pool, random);
            while (selected.size() < WORD_COUNT && !pool.isEmpty()) {
                String first = pool.remove(0);
                List<String> replacements = new ArrayList<>();
                replacements.add(first);
                List<String> shorter = new ArrayList<>();
                for (String word : pool) {
                    if (word.length() < first.length()) shorter.add(word);
                }
                // После неудачи пробуем меньшую длину; внутри одной длины порядок случайный.
                shorter.sort(Comparator.comparingInt(String::length).reversed());
                replacements.addAll(shorter);
                for (String word : replacements) {
                    pool.remove(word);
                    List<Line> candidates = new ArrayList<>(paths.get(word.length()));
                    Collections.shuffle(candidates, random);
                    boolean placed = false;
                    for (Line line : candidates) {
                        if (!matches(grid, line, word, true)) continue;
                        char[][] trial = new char[height][];
                        for (int r = 0; r < height; r++) trial[r] = grid[r].clone();
                        for (int i = 0; i < word.length(); i++) {
                            trial[line.r(i)][line.c(i)] = word.charAt(i);
                        }
                        List<String> check = new ArrayList<>(selected);
                        check.add(word);
                        if (!unique(trial, check, paths)) continue;
                        grid = trial;
                        selected.add(word);
                        placed = true;
                        break;
                    }
                    if (placed) break;
                }
            }
            if (selected.size() != WORD_COUNT) continue;
            for (int retry = 0; retry < 8; retry++) {
                char[][] result = new char[height][];
                for (int row = 0; row < height; row++) {
                    result[row] = grid[row].clone();
                    for (int col = 0; col < width; col++) {
                        if (result[row][col] == 0) {
                            result[row][col] = LETTERS.charAt(random.nextInt(LETTERS.length()));
                        }
                    }
                }
                if (unique(result, selected, paths)) return new Puzzle(result, List.copyOf(selected));
            }
        }
        throw new IllegalStateException("Не удалось разместить 10 слов. Измените размер или начальное число.");
    }
    private static List<Line> lines(int width, int height, int length) {
        List<Line> result = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (length == 1) {
                    result.add(new Line(row, col, 0, 0));
                    continue;
                }
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        Line line = new Line(row, col, dr, dc);
                        int endRow = line.r(length - 1);
                        int endCol = line.c(length - 1);
if (endRow >= 0 && endRow < height && endCol >= 0 && endCol < width) {
                            result.add(line);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean matches(char[][] grid, Line line, String word, boolean allowEmpty) {
        for (int i = 0; i < word.length(); i++) {
            char cell = grid[line.r(i)][line.c(i)];
            if (cell != word.charAt(i) && !(allowEmpty && cell == 0)) return false;
        }
        return true;
    }
                                                                                                                            //Егоров

public static void main(String[] args) {
        try {
            if (args.length < 1 || args.length > 3 || !SIZES.contains(args[0])) {
                throw new IllegalArgumentException("Использование: java Words ШИРИНАxВЫСОТА [ФАЙЛ_XML] [ЧИСЛО]");
            }
            String[] size = args[0].split("x");
            int width = Integer.parseInt(size[0]);
            int height = Integer.parseInt(size[1]);
            long seed = args.length == 3 ? Long.parseLong(args[2]) : new Random().nextLong();
            List<String> words = readWords(Path.of(args.length >= 2 ? args[1] : "words.xml"));
            Puzzle puzzle = generate(width, height, words, new Random(seed));
            char[][] grid = puzzle.grid();
            PrintWriter out = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            out.printf("Матрица %dx%d | Начальное число %d%n", width, height, seed);
            for (char[] row : grid) {
                StringJoiner letters = new StringJoiner(" ");
                for (char cell : row) letters.add(String.valueOf(cell));
                out.println(letters);
            }
            out.printf("Слова (%d): %s%n", puzzle.words().size(), String.join(", ", puzzle.words()));
        } catch (Exception error) {
            System.err.println("Ошибка: " + error.getMessage());
            System.exit(1);
        }
    }

    private static boolean unique(char[][] grid, List<String> words, Map<Integer, List<Line>> paths) {
        for (String word : words) {
            boolean palindrome = word.contentEquals(new StringBuilder(word).reverse());
            int count = 0;
            for (Line line : paths.get(word.length())) {
                // Палиндром на одном отрезке считаем один раз.
                if (palindrome && !line.forward()) continue;
                if (matches(grid, line, word, false) && ++count > 1) return false;
            }
            if (count != 1) return false;
        }
        return true;
    }
}
