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

    @Data
    @Builder
    @Accessors(fluent = true)
    static class FileTag {
        private String name;
        private String content;
        private boolean autoFormat;
        private boolean lineNumbers;
    }
}
