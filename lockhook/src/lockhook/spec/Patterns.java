/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package lockhook.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Holds multiple regexes.
 */
public class Patterns {
    private final List<Pattern> mPatterns = new ArrayList<>();
    private final boolean mDefaultMatch;

    public Patterns(boolean defaultMatch) {
        mDefaultMatch = defaultMatch;
    }

    public void add(String regex) {
        try {
            mPatterns.add(Pattern.compile(regex));
        } catch (IllegalArgumentException e) {
            throw new ConfigException("Invalid pattern \"" + regex + "\"", e);
        }
    }

    public boolean matches(String value) {
        if (mPatterns.size() == 0) {
            return mDefaultMatch;
        }
        for (Pattern p : mPatterns) {
            if (p.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return mPatterns.size();
    }
}
