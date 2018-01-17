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

package org.deeplearning4j.text.tokenization.tokenizer;

import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.token.type.Token;
import org.deeplearning4j.text.uima.UimaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tokenizer based on the passed in analysis engine
 * @author Adam Gibson
 *
 */
public class UimaTokenizer implements Tokenizer {

    private List<String> tokens;
    private int index;
    private static final Logger log = LoggerFactory.getLogger(UimaTokenizer.class);
    private boolean checkForLabel;
    private TokenPreProcess preProcess;
    private Class tokenClass;

     /**
     * Tokenizes using org.cleartk.token.type.Token
     */
    public UimaTokenizer(String tokens, UimaResource resource, boolean checkForLabel) {

        this.checkForLabel = checkForLabel;
        this.tokens = new ArrayList<>();
        try {
            CAS cas = resource.process(tokens);

            Collection<Token> tokenList = JCasUtil.select(cas.getJCas(), Token.class);

            for (Token t : tokenList) {

                if (!checkForLabel || valid(t.getCoveredText()))
                    if (t.getLemma() != null)
                        this.tokens.add(t.getLemma());
                    else if (t.getStem() != null)
                        this.tokens.add(t.getStem());
                    else
                        this.tokens.add(t.getCoveredText());
            }
            resource.release(cas);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /** Tokenizes on the basis of UIMA annotations tokens
    */
    public UimaTokenizer(String tokentext,UimaResource resource,String token_classname) {
    
        this.tokens = new ArrayList<>();
        log.debug("Called UIMA String Tokenzier constructor to get tokens of type " + token_classname);
        try {
            tokenClass = Class.forName(token_classname);
            CAS cas = resource.process(tokentext);
            for (Object some_token : JCasUtil.select(cas.getJCas(), tokenClass)) {
                    Annotation atoken = (Annotation) some_token;
                    this.tokens.add(atoken.getCoveredText());
            }
            if (this.tokens.size() == 0) {
                log.warn("Failed to get any " + token_classname + " tokens.");
            }
            resource.release(cas);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /** Tokenizes on the basis of text covered by org.apache.uima.jcas.tcas.Annotation
    * Should handle org.apache.ctakes.typesystem.type.syntax.BaseToken
    */
    public UimaTokenizer(InputStream tokenstream,UimaResource resource,String token_classname) {
    
        this.checkForLabel = checkForLabel;
        this.tokens = new ArrayList<>();
        log.debug("Called correct uimatokenizer constructor to get " + token_classname);
        try {
            tokenClass = Class.forName(token_classname);
            CAS cas = resource.process(tokenstream);
            for (Object some_token : JCasUtil.select(cas.getJCas(), tokenClass)) {
                Annotation atoken = (Annotation) some_token;
                this.tokens.add(atoken.getCoveredText());
            }
            if (this.tokens.size() == 0) {
                log.warn("Failed to get any " + token_classname + " tokens.");
            }
            resource.release(cas);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private boolean valid(String check) {
        return !(check.matches("<[A-Z]+>") || check.matches("</[A-Z]+>"));
    }



    @Override
    public boolean hasMoreTokens() {
        return index < tokens.size();
    }

    @Override
    public int countTokens() {
        return tokens.size();
    }

    @Override
    public String nextToken() {
        String ret = tokens.get(index);
        index++;
        if (preProcess != null) {
            ret = preProcess.preProcess(ret);
        }
        log.debug("UimaTokenizer:nextToken() of:" + ret);
        return ret;
    }

    @Override
    public List<String> getTokens() {
        List<String> tokens = new ArrayList<>();
        while (hasMoreTokens()) {
            tokens.add(nextToken());
        }
        return tokens;
    }

    @Override
    public void setTokenPreProcessor(TokenPreProcess tokenPreProcessor) {
       this.preProcess = tokenPreProcessor;
    }



}
