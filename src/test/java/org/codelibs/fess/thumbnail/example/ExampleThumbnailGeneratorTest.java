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
package org.codelibs.fess.thumbnail.example;

import org.dbflute.utflute.lastaflute.LastaFluteTestCase;

public class ExampleThumbnailGeneratorTest extends LastaFluteTestCase {

    private ExampleThumbnailGenerator generator;

    @Override
    protected String prepareConfigFile() {
        return "test_app.xml";
    }

    @Override
    protected boolean isSuppressTestCaseTransaction() {
        return true;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        generator = new ExampleThumbnailGenerator();
    }

    public void test_init() {
        // Test initialization
        System.setProperty("lasta.env", "thumbnail");
        generator.init();
        assertTrue(generator.available);
    }

    public void test_init_notThumbnailEnv() {
        // Test that generator is not available in non-thumbnail environment
        System.setProperty("lasta.env", "web");
        generator.init();
        assertFalse(generator.available);
    }

    @Override
    public void tearDown() throws Exception {
        generator.destroy();
        super.tearDown();
    }
}
