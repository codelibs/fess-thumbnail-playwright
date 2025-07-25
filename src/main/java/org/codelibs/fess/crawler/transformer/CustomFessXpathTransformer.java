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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codelibs.fess.crawler.entity.ResponseData;
import org.w3c.dom.Document;

/**
 * Custom XPath transformer for Fess that filters HTML content for thumbnail generation.
 * This transformer only processes HTML content and skips other MIME types to optimize
 * thumbnail generation performance.
 */
public class CustomFessXpathTransformer extends FessXpathTransformer {

    private static final Logger logger = LogManager.getLogger(CustomFessXpathTransformer.class);

    /**
     * Default constructor for CustomFessXpathTransformer.
     */
    public CustomFessXpathTransformer() {
        super();
    }

    @Override
    protected String getThumbnailUrl(final ResponseData responseData, final Document document) {
        final String url = responseData.getUrl();
        final String mimeType = responseData.getMimeType();
        if (!"text/html".equals(mimeType)) {
            if (logger.isDebugEnabled()) {
                logger.debug("mimetype is {}. skipped thumbnail generation for {}", mimeType, url);
            }
            return null;
        }
        return url;
    }
}
