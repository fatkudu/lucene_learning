package org.cirerje.lucene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexFilesTest {
    private static final Logger log = LogManager.getLogger(IndexFilesTest.class);

    public static IndexWriter getIndexWriter(Path indexPath, boolean create) throws IOException {
        Directory dir = FSDirectory.open(indexPath);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        if (create) {
            // Create a new index in the directory, removing any previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }
        // Optional: for better indexing performance, if you are indexing many documents, increase the RAM buffer.
        // But requires larger max heap size to the JVM (eg add -Xmx512m or -Xmx1g): iwc.setRAMBufferSizeMB(256.0);
        return new IndexWriter(dir, iwc);
    }

    @Test
    void indexDocs() throws IOException, ParseException {
        Path indexPath = Paths.get("target/indices/" + this.getClass().getName(), "index");
        FilesUtil.clearFolder(indexPath);
        IndexWriter indexWriter = getIndexWriter(indexPath, true);
        assertTrue(true, "Should be true");
        Document doc = new Document();
        Field idField = new StringField("id", "docid", Field.Store.YES);
        doc.add(idField);
        Field contentField = new TextField("content", "’Twas brillig, and the slithy toves\n" +
                "Did gyre and gimble in the wabe", Field.Store.YES);
        doc.add(contentField);
        indexWriter.addDocument(doc);
        indexWriter.close();
        log.info("Created index {}", indexPath.toAbsolutePath());
        IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("content", analyzer);
        Query query = parser.parse("id:docid");
        TopDocs topDocs = searcher.search(query, 100);
        assertEquals(1, topDocs.totalHits, "number of hits");
        List<Document> docsFound = new ArrayList<>();
        for (ScoreDoc topDoc : topDocs.scoreDocs) {
            docsFound.add(searcher.doc(topDoc.doc));
        }
        assertEquals(1, docsFound.size(), "number of documents found");
        Document docFound = docsFound.get(0);
        log.info("Document found: {}", docFound);
        String foundId = docFound.get("id");
        assertEquals("docid", foundId, "ID of document");
        reader.close();
    }
}