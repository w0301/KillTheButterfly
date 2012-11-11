package madscience;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import madscience.views.MenuView;

/**
 *
 * @author Richard Kakaš
 */
public class HighScoreTable {
    private static final HighScoreTable tableInstance;

    static {
        tableInstance = new HighScoreTable(new File("highscore"));
    }

    private static class TableEntry implements Comparable<TableEntry> {
        public String name;
        public int score;
        public int level;

        public TableEntry(String name, int score, int level) {
            this.name = name;
            this.score = score;
            this.level = level;
        }

        public String toMenuString() {
            return name + " - score: " + score + ", level: " + level;
        }

        @Override
        public String toString() {
            return name + " " + score + " " + level;
        }

        @Override
        public int compareTo(TableEntry t) {
            return t.score - score;
        }
    }

    private final File file;
    private MenuView menuView = null;
    private List<TableEntry> tableEntries = new LinkedList<TableEntry>();

    private void saveTable() {
        try {
            file.createNewFile();
        }
        catch (IOException e) {
            return;
        }

        FileOutputStream fileStream = null;
        PrintWriter out;
        try {
            fileStream = new FileOutputStream(file);
            out = new PrintWriter(fileStream);

            out.println(tableEntries.size());
            for (int i = 0; i < tableEntries.size(); i++) {
                out.println(tableEntries.get(i).toString());
            }
            out.close();
        }
        catch (Exception e) { }
    }

    private void loadTable() {
        if (!file.exists()) return;

        FileInputStream fileStream = null;
        Scanner in;
        try {
            fileStream = new FileInputStream(file);
            in = new Scanner(fileStream);

            int entriesCount = in.nextInt();
            for (int i = 0; i < entriesCount; i++) {
                String name = in.next();
                int score = in.nextInt();
                int level = in.nextInt();
                tableEntries.add(new TableEntry(name, score, level));
            }
            Collections.sort(tableEntries);

            in.close();
        }
        catch (Exception e) { }

    }

    private HighScoreTable(File file) {
        this.file = file;
        loadTable();
    }

    public void addEntry(String name, int score, int level) {
        tableEntries.add(new TableEntry(name, score, level));
        Collections.sort(tableEntries);

        saveTable();
    }

    public MenuView getMenuView() {
        return menuView;
    }

    public void setMenuView(MenuView menuView) {
        this.menuView = menuView;
        refreshMenuView();
    }

    public void refreshMenuView() {
        if (menuView == null) return;
        menuView.removeAllNotSelectableItems();
        int i = 0;
        for (TableEntry entry : tableEntries) {
            menuView.addItem(i++, entry.toMenuString(), null);
        }
    }

    public void showAddEntryDialog(final Component parent, final int score, final int level, final boolean badName) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String str = (badName ? "You entered bad name!\n" : "");
                str += "Enter your name: ";
                String ret = JOptionPane.showInputDialog(null, str, "New high score", JOptionPane.INFORMATION_MESSAGE);
                if (ret != null && !ret.equals("")) {
                    addEntry(ret, score, level);
                    refreshMenuView();
                }
                else showAddEntryDialog(parent, score, level, true);
            }
        });
    }

    public void showAddEntryDialog(final Component parent, final int score, final int level) {
        showAddEntryDialog(parent, score, level, false);
    }

    public static HighScoreTable getInstance() {
        return tableInstance;
    }

}
