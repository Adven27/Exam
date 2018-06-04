package com.adven.concordion.extensions.exam.files;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import com.adven.concordion.extensions.exam.html.Html;
import com.google.common.io.Files;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.concordion.api.Evaluator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.getResource;
import static java.io.File.separator;
import static java.lang.Boolean.parseBoolean;

public class DefaultFilesLoader implements FilesLoader {
    private static final Charset CHARSET = Charset.forName("UTF-8");

    @Override
    public void clearFolder(String path) {
        final File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    throw new RuntimeException("could not delete file " + file.getPath());
                }
            }
        }
    }

    @Override
    public void createFileWith(String filePath, String fileContent) {
        try {
            File to = new File(filePath);
            Files.createParentDirs(to);
            if (to.createNewFile() && !isNullOrEmpty(fileContent)) {
                Files.append(fileContent, to, CHARSET);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getFileNames(String path) {
        final File dir = new File(path);
        List<String> fileNames = getFileNamesForDir(dir, "");
        String[] names = new String[fileNames.size()];
        names = fileNames.toArray(names);
        return names;
    }

    private List<String> getFileNamesForDir(File dir, String s) {
        List<String> fileNames = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) {
            return fileNames;
        }
        for (File file : files) {
            if (file.isFile()) {
                String path = "".equals(s) ? "" : s + separator;
                fileNames.add(path + file.getName());
            }
            if (file.isDirectory()) {
                String path = "".equals(s) ? file.getName() : s + separator + file.getName();
                fileNames.addAll(getFileNamesForDir(file, path));
            }
        }
        return fileNames;
    }

    @Override
    public boolean fileExists(String filePath) {
        File actual = new File(filePath);
        return actual.exists();
    }

    public Document documentFrom(String path) {
        try {
            File xml = new File(path);
            return new Builder().build(xml);
        } catch (ParsingException | IOException e) {
            throw new RuntimeException("invalid xml", e);
        }
    }

    public String readFile(String path, String file) {
        String readRes = "";
        try {
            File fileToRead = new File(path + separator + file);
            readRes = Files.toString(fileToRead, CHARSET);
        } catch (IOException e) {
            readRes = "ERROR WHILE FILE READING";
        }
        return readRes;
    }

    protected String readFile(File file) {
        try {
            return Files.toString(file, CHARSET);
        } catch (IOException e) {
            return "ERROR WHILE FILE READING";
        }
    }

    public FileTag readFileTag(Html f, Evaluator eval) {
        final String content = getContentFor(f);
        FileTag fileTag = new FileTag();
        fileTag.name(f.attr("name"));
        fileTag.content(content == null ? null : PlaceholdersResolver.resolveXml(content, eval).trim());
        fileTag.autoFormat(parseBoolean(f.attr("autoFormat")));
        fileTag.lineNumbers(parseBoolean(f.attr("lineNumbers")));
        return fileTag;
    }

    private String getContentFor(Html f) {
        final String from = f.attr("from");
        return from == null
                ? f.hasChildren() ? f.text() : null
                : readFile(new File(getResource(from).getFile()));
    }
}