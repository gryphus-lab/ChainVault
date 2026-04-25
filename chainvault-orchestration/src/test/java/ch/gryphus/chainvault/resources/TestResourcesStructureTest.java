/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Validates the structure and correctness of test resource files changed in this PR.
 *
 * <p>Covers: {@code src/test/resources/db.json} and
 * {@code src/test/resources/templates/index.html}.
 */
class TestResourcesStructureTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    // -----------------------------------------------------------------------
    // db.json tests
    // -----------------------------------------------------------------------

    @Test
    void dbJson_ShouldBeValidJson() throws Exception {
        String content = readClasspathResource("db.json");
        assertThat(content).isNotBlank();
        // Parsing must not throw
        JsonNode root = JSON_MAPPER.readTree(content);
        assertThat(root).isNotNull();
    }

    @Test
    void dbJson_ShouldHaveDocumentsArray() throws Exception {
        JsonNode root = parseDbJson();
        assertThat(root.has("documents")).isTrue();
        assertThat(root.get("documents").isArray()).isTrue();
    }

    @Test
    void dbJson_ShouldContainExactlyThreeDocuments() throws Exception {
        JsonNode documents = parseDbJson().get("documents");
        assertThat(documents).hasSize(3);
    }

    @Test
    void dbJson_AllDocumentsShouldHaveRequiredFields() throws Exception {
        JsonNode documents = parseDbJson().get("documents");
        for (JsonNode doc : documents) {
            assertThat(doc.has("id")).as("document must have 'id'").isTrue();
            assertThat(doc.has("documentType")).as("document must have 'documentType'").isTrue();
            assertThat(doc.has("status")).as("document must have 'status'").isTrue();
            assertThat(doc.has("tags")).as("document must have 'tags'").isTrue();
        }
    }

    @Test
    void dbJson_AllDocumentsShouldHaveArchivedStatus() throws Exception {
        JsonNode documents = parseDbJson().get("documents");
        for (JsonNode doc : documents) {
            assertThat(doc.get("status").asString())
                    .as("status should be ARCHIVED")
                    .isEqualTo("ARCHIVED");
        }
    }

    @Test
    void dbJson_TagsShouldBeArrayWithThreeElements() throws Exception {
        JsonNode documents = parseDbJson().get("documents");
        for (JsonNode doc : documents) {
            JsonNode tags = doc.get("tags");
            assertThat(tags.isArray()).as("tags must be a JSON array").isTrue();
            assertThat(tags).as("each document must have exactly 3 tags").hasSize(3);
        }
    }

    @Test
    void dbJson_TagsShouldContainExpectedValues() throws Exception {
        JsonNode documents = parseDbJson().get("documents");
        for (JsonNode doc : documents) {
            JsonNode tagsNode = doc.get("tags");
            List<String> tags = new ArrayList<>();
            for (JsonNode tag : tagsNode) {
                tags.add(tag.asString());
            }
            assertThat(tags).containsExactlyInAnyOrder("finance", "2025", "batch");
        }
    }

    @Test
    void dbJson_FirstDocumentShouldHaveCorrectId() throws Exception {
        JsonNode first = parseDbJson().get("documents").get(0);
        assertThat(first.get("id").asString()).isEqualTo("DOC-ARCH-2025-001");
    }

    @Test
    void dbJson_FirstDocumentShouldHavePayloadUrl() throws Exception {
        JsonNode first = parseDbJson().get("documents").get(0);
        assertThat(first.has("payloadUrl")).isTrue();
        assertThat(first.get("payloadUrl").asString()).isEqualTo("/payloads/invoice_001.zip");
    }

    @Test
    void dbJson_SecondDocumentShouldHaveNoPayloadUrl() throws Exception {
        // DOC-ARCH-2025-002 has no payloadUrl in the fixture
        JsonNode second = parseDbJson().get("documents").get(1);
        assertThat(second.has("payloadUrl")).isFalse();
    }

    @Test
    void dbJson_ThirdDocumentShouldHaveCorrectId() throws Exception {
        JsonNode third = parseDbJson().get("documents").get(2);
        assertThat(third.get("id").asString()).isEqualTo("DOC-ARCH-2025-003");
    }

    @Test
    void dbJson_ShouldMatchExpectedStructureWithJsonAssert() throws Exception {
        String content = readClasspathResource("db.json");
        String expectedFragment =
                """
                {
                  "documents": [
                    {
                      "id": "DOC-ARCH-2025-001",
                      "docId": "DOC-ARCH-2025-001",
                      "title": "Invoice #7836 - Stark Industries",
                      "creationDate": "2025-01-31T20:53:05Z",
                      "clientId": "CHE-738.760.530",
                      "accountNo": "CH6900762067743211524",
                      "documentType": "INVOICE",
                      "department": "Accounts Payable",
                      "status": "ARCHIVED",
                      "originalSizeBytes": 5242880,
                      "pageCount": 5,
                      "tags": ["finance", "2025", "batch"],
                      "payloadUrl": "/payloads/invoice_001.zip"
                    },
                    {
                      "id": "DOC-ARCH-2025-002",
                      "docId": "DOC-ARCH-2025-002",
                      "title": "Invoice #7923 - Stark Industries",
                      "creationDate": "2025-01-31T20:53:05Z",
                      "clientId": "CHE-738.760.530",
                      "accountNo": "CH6900762067743211524",
                      "documentType": "INVOICE",
                      "department": "Accounts Payable",
                      "status": "ARCHIVED",
                      "tags": ["finance", "2025", "batch"]
                    },
                    {
                      "id": "DOC-ARCH-2025-003",
                      "docId": "DOC-ARCH-2025-003",
                      "title": "Invoice #7923 - Stark Industries",
                      "creationDate": "2025-01-31T20:53:05Z",
                      "clientId": "CHE-738.760.530",
                      "accountNo": "CH6900762067743211524",
                      "documentType": "INVOICE",
                      "department": "Accounts Payable",
                      "status": "ARCHIVED",
                      "tags": ["finance", "2025", "batch"],
                      "payloadUrl": "/payloads/invoice_003.zip"
                    }
                  ]
                }
                """;
        JSONAssert.assertEquals(expectedFragment, content, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void dbJson_DocumentsShouldAllBeInvoiceType() throws Exception {
        JsonNode documents = parseDbJson().get("documents");
        for (JsonNode doc : documents) {
            assertThat(doc.get("documentType").asString()).isEqualTo("INVOICE");
        }
    }

    // -----------------------------------------------------------------------
    // templates/index.html tests
    // -----------------------------------------------------------------------

    @Test
    void indexHtml_ShouldBeReadableFromClasspath() throws Exception {
        String html = readClasspathResource("templates/index.html");
        assertThat(html).isNotBlank();
    }

    @Test
    void indexHtml_ShouldUseLowercaseDoctype() throws Exception {
        String html = readClasspathResource("templates/index.html");
        // PR changed `<!DOCTYPE html>` → `<!doctype html>` (HTML5 canonical lowercase)
        assertThat(html).contains("<!doctype html>").doesNotContain("<!DOCTYPE html>");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "<html lang=\"en\">",
                "<title>ChainVault Migration Dashboard</title>",
                "<div id=\"root\"></div>",
                "adjusted by Vite build"
            })
    void indexHtml_ShouldHaveCorrectContent(String testString) throws Exception {
        String html = readClasspathResource("templates/index.html");
        assertThat(html).contains(testString);
    }

    @Test
    void indexHtml_ShouldHaveUtf8MetaCharset() throws Exception {
        String html = readClasspathResource("templates/index.html");
        // Case-insensitive regex accepting single or double quotes around UTF-8
        assertThat(html).containsPattern("(?i)charset\\s*=\\s*['\"]utf-8['\"]");
    }

    @Test
    void indexHtml_ShouldHaveModuleScriptTag() throws Exception {
        String html = readClasspathResource("templates/index.html");
        // Verify both type="module" and src="/assets/index.js" are on the same <script> tag
        assertThat(html)
                .containsPattern(
                        "<script[^>]*\\btype=\"module\"[^>]*\\bsrc=\"/assets/index\\.js\"[^>]*>"
                                + "|<script[^>]*\\bsrc=\"/assets/index\\.js\"[^>]*\\btype=\"module\"[^>]*>");
    }

    @Test
    void indexHtml_ShouldHaveFaviconLink() throws Exception {
        String html = readClasspathResource("templates/index.html");
        // Verify both rel="icon" and href="/favicon.svg" are on the same <link> tag
        assertThat(html)
                .containsPattern(
                        "<link[^>]*\\brel=\"icon\"[^>]*\\bhref=\"/favicon\\.svg\"[^>]*>"
                                + "|<link[^>]*\\bhref=\"/favicon\\.svg\"[^>]*\\brel=\"icon\"[^>]*>");
    }

    @Test
    void indexHtml_ShouldHaveClosingHtmlTag() throws Exception {
        String html = readClasspathResource("templates/index.html");
        assertThat(html.stripTrailing()).endsWith("</html>");
    }

    @Test
    void indexHtml_ShouldHaveHeadAndBodySections() throws Exception {
        String html = readClasspathResource("templates/index.html");
        assertThat(html)
                .contains("<head>")
                .contains("</head>")
                .contains("<body>")
                .contains("</body>");
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private static JsonNode parseDbJson() throws Exception {
        String content = readClasspathResource("db.json");
        return JSON_MAPPER.readTree(content);
    }

    private static String readClasspathResource(String path) throws IOException {
        try (InputStream is =
                TestResourcesStructureTest.class.getClassLoader().getResourceAsStream(path)) {
            assertThat(is).as("classpath resource '%s' must exist", path).isNotNull();
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
