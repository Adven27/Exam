package com.adven.concordion.extensions.exam.files;

import com.adven.concordion.extensions.exam.html.Html;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.concordion.api.Evaluator;
import nu.xom.Document;

/**
 * Интерфейс реализации работы с файлами
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

        public boolean lineNumbers() {
            return lineNumbers;
        }

        public String content() {
            return content;
        }

        public boolean autoFormat() {
            return autoFormat;
        }

        public void name(String name) {
            this.name = name;
        }

        public void content(String cnt) {
            this.content = cnt;
        }

        public void autoFormat(boolean autoFormat) {
            this.autoFormat = autoFormat;
        }

        public void lineNumbers(boolean lineNumbers) {
            this.lineNumbers = lineNumbers;
        }
    }
}