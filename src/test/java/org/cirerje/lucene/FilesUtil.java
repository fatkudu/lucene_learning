package org.cirerje.lucene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesUtil {
    private static final Logger log = LogManager.getLogger(FilesUtil.class);

    public static void clearFolder(Path folderPath) {
        if (!Files.exists(folderPath)) {
            return;
        }
        try (Stream<Path> walker = Files.walk(folderPath)) {
            walker.sorted(Comparator.reverseOrder())
                    .forEach(FilesUtil::doDelete);
        } catch (IOException e) {
            log.debug("clearFolder failed for {}", folderPath, e);
        }
        assertFalse(Files.exists(folderPath), "clearFolder failed:Folder " + folderPath + " still exists");
    }

    private static void doDelete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException ioe) {
            log.debug("Delete failed for {}", path, ioe);
        }
    }
}
