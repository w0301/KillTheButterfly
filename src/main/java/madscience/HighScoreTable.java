package madscience;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Richard Kakaš
 */
public class HighScoreTable {
    private static final HighScoreTable tableInstance;

    static {
        tableInstance = new HighScoreTable(new File("highscore"));
    }

    private static class TableEntry {
        public String name;
        public int score;
        public int level;

        public TableEntry(String name, int score, int level) {
            this.name = name;
            this.score = score;
            this.level = level;
        }
    }

    private final File file;
    private List<TableEntry> tableEntries = new LinkedList<TableEntry>();

    private void saveTable() throws FileNotFoundException {
        BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(file));

    }

    private HighScoreTable(File file) {
        this.file = file;


    }


    public HighScoreTable getInstance() {
        return tableInstance;
    }

}
