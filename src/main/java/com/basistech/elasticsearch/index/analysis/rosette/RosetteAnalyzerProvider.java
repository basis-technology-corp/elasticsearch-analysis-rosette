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
import com.basistech.rlp.lucene.RLPAnalyzerDispatcher;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

/**
 *
 */
public class RosetteAnalyzerProvider extends AbstractIndexAnalyzerProvider<RLPAnalyzer> {

    private final RLPAnalyzer analyzer;

    @Inject
    public RosetteAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                   @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        String lang = settings.get("bt.lang", "eng");
        analyzer = RLPAnalyzerDispatcher.createAnalyzer(lang);
    }

    @Override
    public RLPAnalyzer get() {
        return this.analyzer;
    }
}
