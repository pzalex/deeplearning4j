/*-
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.text.tokenization.tokenizerfactory;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.text.annotator.SentenceAnnotator;
import org.deeplearning4j.text.annotator.TokenizerAnnotator;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizer.UimaTokenizer;
import org.deeplearning4j.text.uima.UimaResource;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Uses a uima {@link AnalysisEngine} to 
 * tokenize text.
 *
 *
 * @author Adam Gibson
 *
 */
public class UimaTokenizerFactory implements TokenizerFactory {

    private UimaResource uimaResource;
    private boolean checkForLabel;
    private static AnalysisEngine defaultAnalysisEngine;
    private String token_name;
    private TokenPreProcess preProcess;

    private static final Logger log = LoggerFactory.getLogger(UimaTokenizerFactory.class);

    public UimaTokenizerFactory() throws ResourceInitializationException {
        this(defaultAnalysisEngine(), true);
    }

    public UimaTokenizerFactory(UimaResource resource) {
        this(resource, true);
    }

    public UimaTokenizerFactory(UimaResource resource, String token_class) {
        this.uimaResource = resource;
        this.checkForLabel = true;
        this.token_name = token_class;
    }

    public UimaTokenizerFactory(AnalysisEngine tokenizer) {
        this(tokenizer, true);
    }

    public UimaTokenizerFactory(UimaResource resource, boolean checkForLabel) {
        this.uimaResource = resource;
        this.checkForLabel = checkForLabel;
    }

    public UimaTokenizerFactory(boolean checkForLabel) throws ResourceInitializationException {
        this(defaultAnalysisEngine(), checkForLabel);
    }

    public UimaTokenizerFactory(AnalysisEngine tokenizer, boolean checkForLabel) {
        super();
        this.checkForLabel = checkForLabel;
        try {
            this.uimaResource = new UimaResource(tokenizer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tokenizer create(String toTokenize) {
        log.debug("Tokenizing string");
        if (toTokenize == null) {
            throw new IllegalArgumentException("Unable to proceed; on sentence to tokenize");
        }
        Tokenizer ret = null;
        if (token_name != null) {
            ret = new UimaTokenizer(toTokenize, uimaResource, token_name);
        } else {
            ret = new UimaTokenizer(toTokenize, uimaResource, checkForLabel);
        }
        ret.setTokenPreProcessor(preProcess);
        return ret;
    }

    public UimaResource getUimaResource() {
        return uimaResource;
    }


    /**
     * Creates a tokenization,/stemming pipeline
     * @return a tokenization/stemming pipeline
     */
    public static AnalysisEngine defaultAnalysisEngine() {
        try {
            if (defaultAnalysisEngine == null) {
                defaultAnalysisEngine = AnalysisEngineFactory.createEngine(
                        AnalysisEngineFactory.createEngineDescription(SentenceAnnotator.getDescription(),
                                TokenizerAnnotator.getDescription()));
            }

            return defaultAnalysisEngine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


        /** Assumes InputStream is UTF-8
        */
    @Override
    public Tokenizer create(InputStream toTokenize) {
        log.debug("Tokenizing InputStream");
        if (toTokenize == null) {
            throw new IllegalArgumentException("Unable to proceed; null InputStream");
        }
        Tokenizer ret = null;
        if (token_name == null) {

            throw new UnsupportedOperationException();
        } else {
            ret = new UimaTokenizer(toTokenize,uimaResource,token_name);
        }
        ret.setTokenPreProcessor(preProcess);
        return ret;
    }

    @Override
    public void setTokenPreProcessor(TokenPreProcess preProcessor) {
        this.preProcess = preProcessor;
    }

    /**
     * Returns TokenPreProcessor set for this TokenizerFactory instance
     *
     * @return TokenPreProcessor instance, or null if no preprocessor was defined
     */
    @Override
    public TokenPreProcess getTokenPreProcessor() {
        return preProcess;
    }

}
