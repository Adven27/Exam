package com.adven.concordion.extensions.exam.files;

import com.adven.concordion.extensions.exam.html.Html;
import nu.xom.Document;
import org.concordion.api.Evaluator;

/**
 * Интерфейс реализации работы с файлами.
 */
public interface FilesLoader {

    void clearFolder(String path);

    void createFileWith(String filePath, String fileContent);

    String[] getFileNames(String path);

    boolean fileExists(String filePath);

    Document documentFrom(String path);

    String readFile(String path, String file);

    FileTag readFileTag(Html f, Evaluator eval);

    class FileTag {
        private String name;
        private String content;
        private boolean autoFormat;
        private boolean lineNumbers;

        public String name() {
            return name;
        }

        public void name(String name) {
            this.name = name;
        }

        public boolean lineNumbers() {
            return lineNumbers;
        }

        public void lineNumbers(boolean lineNumbers) {
            this.lineNumbers = lineNumbers;
        }

        public String content() {
            return content;
        }

        public void content(String cnt) {
            this.content = cnt;
        }

        public boolean autoFormat() {
            return autoFormat;
        }

        public void autoFormat(boolean autoFormat) {
            this.autoFormat = autoFormat;
        }
    }
}