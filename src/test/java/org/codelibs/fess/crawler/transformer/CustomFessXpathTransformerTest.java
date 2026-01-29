/*
 * Copyright 2012-2025 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.crawler.transformer;

import org.codelibs.fess.crawler.entity.ResponseData;
import org.codelibs.fess.thumbnail.playwright.UnitTestCase;
import org.junit.jupiter.api.TestInfo;
import org.w3c.dom.Document;

/**
 * Comprehensive test cases for CustomFessXpathTransformer using Ultrathink approach.
 * Tests cover edge cases, error conditions, and various MIME type scenarios.
 */
public class CustomFessXpathTransformerTest extends UnitTestCase {

    private CustomFessXpathTransformer transformer;

    @Override
    protected boolean isSuppressTestCaseTransaction() {
        return true;
    }

    @Override
    protected void setUp(TestInfo testInfo) throws Exception {
        super.setUp(testInfo);
        transformer = new CustomFessXpathTransformer();
    }

    /**
     * Test getThumbnailUrl with HTML content - should return the URL.
     */
    public void test_getThumbnailUrl_htmlContent() {
        // Setup
        final String expectedUrl = "https://example.com/page.html";
        final ResponseData responseData = createResponseData(expectedUrl, "text/html");
        final Document document = null; // Not used in current implementation

        // Execute
        final String result = transformer.getThumbnailUrl(responseData, document);

        // Verify
        assertEquals("Should return URL for HTML content", expectedUrl, result);
    }

    /**
     * Test getThumbnailUrl with various non-HTML MIME types - should return null.
     */
    public void test_getThumbnailUrl_nonHtmlContent() {
        final String url = "https://example.com/file";
        final String[] nonHtmlMimeTypes = { "application/pdf", "image/jpeg", "image/png", "text/plain", "application/json", "text/css",
                "application/javascript", "video/mp4", "audio/mpeg" };

        for (String mimeType : nonHtmlMimeTypes) {
            // Setup
            final ResponseData responseData = createResponseData(url, mimeType);
            final Document document = null;

            // Execute
            final String result = transformer.getThumbnailUrl(responseData, document);

            // Verify
            assertNull("Should return null for MIME type: " + mimeType, result);
        }
    }

    /**
     * Test getThumbnailUrl with HTML MIME type variations - should return URL.
     */
    public void test_getThumbnailUrl_htmlMimeTypeVariations() {
        final String url = "https://example.com/page";
        final String[] htmlMimeTypes = { "text/html", "text/html; charset=UTF-8", "text/html; charset=ISO-8859-1" };

        for (String mimeType : htmlMimeTypes) {
            // Setup
            final ResponseData responseData = createResponseData(url, mimeType);
            final Document document = null;

            // Execute
            final String result = transformer.getThumbnailUrl(responseData, document);

            // Verify - Current implementation only checks exact match "text/html"
            if ("text/html".equals(mimeType)) {
                assertEquals("Should return URL for exact HTML MIME type", url, result);
            } else {
                assertNull("Current implementation requires exact 'text/html' match", result);
            }
        }
    }

    /**
     * Test getThumbnailUrl with null MIME type - should return null.
     */
    public void test_getThumbnailUrl_nullMimeType() {
        // Setup
        final String url = "https://example.com/page";
        final ResponseData responseData = createResponseData(url, null);
        final Document document = null;

        // Execute
        final String result = transformer.getThumbnailUrl(responseData, document);

        // Verify
        assertNull("Should return null for null MIME type", result);
    }

    /**
     * Test getThumbnailUrl with empty MIME type - should return null.
     */
    public void test_getThumbnailUrl_emptyMimeType() {
        // Setup
        final String url = "https://example.com/page";
        final ResponseData responseData = createResponseData(url, "");
        final Document document = null;

        // Execute
        final String result = transformer.getThumbnailUrl(responseData, document);

        // Verify
        assertNull("Should return null for empty MIME type", result);
    }

    /**
     * Test getThumbnailUrl with null ResponseData - should handle gracefully.
     */
    public void test_getThumbnailUrl_nullResponseData() {
        // Setup
        final ResponseData responseData = null;
        final Document document = null;

        // Execute & Verify
        try {
            transformer.getThumbnailUrl(responseData, document);
            fail("Should throw exception for null ResponseData");
        } catch (Exception e) {
            // Expected behavior - method should not handle null ResponseData
            assertTrue("Exception expected for null ResponseData", true);
        }
    }

    /**
     * Test getThumbnailUrl with case-sensitive MIME type - current implementation is case-sensitive.
     */
    public void test_getThumbnailUrl_caseSensitiveMimeType() {
        final String url = "https://example.com/page";
        final String[] caseMimeTypes = { "TEXT/HTML", "Text/Html", "text/HTML" };

        for (String mimeType : caseMimeTypes) {
            // Setup
            final ResponseData responseData = createResponseData(url, mimeType);
            final Document document = null;

            // Execute
            final String result = transformer.getThumbnailUrl(responseData, document);

            // Verify - Current implementation is case-sensitive
            assertNull("Should return null for case-different MIME type: " + mimeType, result);
        }
    }

    /**
     * Test constructor creates instance properly.
     */
    public void test_constructor() {
        // Execute
        final CustomFessXpathTransformer newTransformer = new CustomFessXpathTransformer();

        // Verify
        assertNotNull("Constructor should create instance", newTransformer);
        assertTrue("Should be instance of FessXpathTransformer", newTransformer instanceof FessXpathTransformer);
    }

    /**
     * Helper method to create ResponseData with specified URL and MIME type.
     */
    private ResponseData createResponseData(final String url, final String mimeType) {
        final ResponseData responseData = new ResponseData();
        responseData.setUrl(url);
        responseData.setMimeType(mimeType);
        return responseData;
    }
}
