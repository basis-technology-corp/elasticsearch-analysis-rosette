/******************************************************************************
 ** Copyright (c) 2013 Basis Technology Corporation 
 ** Permission is hereby granted, free of charge, to any person obtaining a 
 ** copy of this software and associated documentation files (the "Software"), 
 ** to deal in the Software without restriction, including without limitation 
 ** the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 ** and/or sell copies of the Software, and to permit persons to whom the 
 ** Software is furnished to do so, subject to the following conditions: 
 **
 ** The above copyright notice and this permission notice shall be included in 
 ** all copies or substantial portions of the Software.
 ** 
 ** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 ** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 ** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 ** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 ** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 ** FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 ** DEALINGS IN THE SOFTWARE.
 **
 ** Except as contained in this notice, the name(s) of the above copyright 
 ** holders shall not  be used in advertising or otherwise to promote the sale, 
 ** use or other dealings in this Software without prior written authorization.
 ******************************************************************************/

package com.basistech.elasticsearch.index.analysis.rosette;

import com.basistech.rlp.lucene.RLPAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.hamcrest.MatcherAssert;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;

import static java.lang.System.out;
import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import static org.hamcrest.Matchers.instanceOf;

/**
 *
 */
public class SimpleRosetteAnalysisTests {

    private AnalysisService getAnalysisService() {
        Index index = new Index("test");

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(EMPTY_SETTINGS),
                new EnvironmentModule(new Environment(EMPTY_SETTINGS)), new IndicesAnalysisModule()).createInjector();
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, EMPTY_SETTINGS),
                new IndexNameModule(index),
                new AnalysisModule(EMPTY_SETTINGS, parentInjector.getInstance(IndicesAnalysisService.class))
                        .addProcessor(new RosetteAnalysisBinderProcessor()))
                .createChildInjector(parentInjector);

        return injector.getInstance(AnalysisService.class);

    }

    public static void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        Assert.assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            String s = termAttr.toString();
            //out.printf("Output Token %2d: %s%n", i, s);
            Assert.assertTrue(i < expected.length, "got extra term: " + s);
            Assert.assertEquals(termAttr.toString(), expected[i], "expected different term at index " + i);
            i++;
        }
        Assert.assertEquals(i, expected.length, "not all tokens produced");
    }

    @Test
    public void testRLPAnalysis() throws Exception {
        String testString = "My hovercraft is full of eels.";
        //out.printf("Input String: %s%n", testString);
        AnalysisService analysisService = getAnalysisService();
        NamedAnalyzer englishAnalyzer = analysisService.analyzer("rosette");
        MatcherAssert.assertThat(englishAnalyzer.analyzer(), instanceOf(RLPAnalyzer.class));
        assertSimpleTSOutput(englishAnalyzer.tokenStream("test", new StringReader(testString)), new String[]{"hovercraft", "full", "eel"});
    }

}
