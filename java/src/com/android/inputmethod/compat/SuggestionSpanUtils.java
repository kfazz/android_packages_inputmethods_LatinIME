/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.compat;

import com.android.inputmethod.latin.SuggestedWords;
import com.android.inputmethod.latin.SuggestionSpanPickedNotificationReceiver;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Locale;

public class SuggestionSpanUtils {
    public static final String ACTION_SUGGESTION_PICKED =
            "android.text.style.SUGGESTION_PICKED";
    public static final String SUGGESTION_SPAN_PICKED_AFTER = "after";
    public static final String SUGGESTION_SPAN_PICKED_BEFORE = "before";
    public static final String SUGGESTION_SPAN_PICKED_HASHCODE = "hashcode";
    public static final int SUGGESTION_MAX_SIZE = 5;

    private static final Class<?> CLASS_SuggestionSpan = CompatUtils
            .getClass("android.text.style.SuggestionSpan");
    private static final Class<?>[] INPUT_TYPE_SuggestionSpan = new Class<?>[] {
            Context.class, Locale.class, String[].class, int.class, Class.class };
    private static final Constructor<?> CONSTRUCTOR_SuggestionSpan = CompatUtils
            .getConstructor(CLASS_SuggestionSpan, INPUT_TYPE_SuggestionSpan);
    public static final boolean SUGGESTION_SPAN_IS_SUPPORTED;
    static {
        SUGGESTION_SPAN_IS_SUPPORTED =
                CLASS_SuggestionSpan != null && CONSTRUCTOR_SuggestionSpan != null;
    }

    public static CharSequence getTextWithSuggestionSpan(Context context,
            CharSequence suggestion, SuggestedWords suggestedWords) {
        if (TextUtils.isEmpty(suggestion) || CONSTRUCTOR_SuggestionSpan == null
                || suggestedWords == null || suggestedWords.size() == 0) {
            return suggestion;
        }

        final Spannable spannable;
        if (suggestion instanceof Spannable) {
            spannable = (Spannable) suggestion;
        } else {
            spannable = new SpannableString(suggestion);
        }
        final ArrayList<String> suggestionsList = new ArrayList<String>();
        for (int i = 0; i < suggestedWords.size(); ++i) {
            if (suggestionsList.size() >= SUGGESTION_MAX_SIZE) {
                break;
            }
            final CharSequence word = suggestedWords.getWord(i);
            if (!TextUtils.equals(suggestion, word)) {
                suggestionsList.add(word.toString());
            }
        }

        final Object[] args =
                { context, null, suggestionsList.toArray(new String[suggestionsList.size()]), 0,
                        (Class<?>) SuggestionSpanPickedNotificationReceiver.class };
        final Object ss = CompatUtils.newInstance(CONSTRUCTOR_SuggestionSpan, args);
        if (ss == null) {
            return suggestion;
        }
        spannable.setSpan(ss, 0, suggestion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}