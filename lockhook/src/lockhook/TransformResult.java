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
package lockhook;

import lockhook.spec.HookSpec;

public class TransformResult {
    private final HookSpec mHookSpec;

    private int mTransformedClassCount;
    private int mTransformedMethodCount;

    public TransformResult(HookSpec hookSpec) {
        mHookSpec = hookSpec;
    }

    public HookSpec getHookSpec() {
        return mHookSpec;
    }

    public int getTransformedClassCount() {
        return mTransformedClassCount;
    }

    public int getTransformedMethodCount() {
        return mTransformedMethodCount;
    }

    public void incrementTransformedClassCount() {
        mTransformedClassCount++;
    }

    public void incrementTransformedMethodCount() {
        mTransformedMethodCount++;
    }

    @Override
    public String toString() {
        return "TransformResult: Spec name=" + mHookSpec.getName()
                + ", " + mTransformedClassCount + " classe(s) " +
                + mTransformedMethodCount + " method(s) transformed";
    }
}
