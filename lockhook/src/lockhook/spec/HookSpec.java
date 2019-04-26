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

import lockhook.Method;
import lockhook.TransformResult;

public class HookSpec {
    private String mName;
    private boolean mPreTraceEnabled;
    private boolean mPostTraceEnabled;

    private Method mSyncMethodStart;
    private Method mSyncMethodEnd;

    private Method mPreEnter;
    private Method mPostEnter;
    private Method mPreExit;
    private Method mPostExit;

    private Patterns mIgnoreClasses = new Patterns(/*defaultMatch=*/ false);
    private Patterns mTargetClasses = new Patterns(/*defaultMatch=*/ true);

    private Patterns mInstanceLockTypes = new Patterns(/*defaultMatch=*/ true);
    private Patterns mClassLockTypes = new Patterns(/*defaultMatch=*/ true);

    private Patterns mIgnoreAnnotations = new Patterns(/*defaultMatch=*/ false);

    private final TransformResult mResult;

    private HookSpec() {
        mResult = new TransformResult(this);
    }

    public String getName() {
        return mName;
    }

    public boolean isPreTraceEnabled() {
        return mPreTraceEnabled;
    }

    public boolean isPostTraceEnabled() {
        return mPostTraceEnabled;
    }

    public Method getSyncMethodStart() {
        return mSyncMethodStart;
    }

    public Method getSyncMethodEnd() {
        return mSyncMethodEnd;
    }

    public Method getPreEnter() {
        return mPreEnter;
    }

    public Method getPostEnter() {
        return mPostEnter;
    }

    public Method getPreExit() {
        return mPreExit;
    }

    public Method getPostExit() {
        return mPostExit;
    }

    public boolean isTargetClass(String className) {
        if (mIgnoreClasses.matches(className)) {
            return false;
        }
        return mTargetClasses.matches(className);
    }

    public boolean isIgnoreAnnotation(String name) {
        return mIgnoreAnnotations.matches(name);
    }

    public boolean isInstanceLockType(String name) {
        return mInstanceLockTypes.matches(name);
    }

    public boolean isClassLockType(String name) {
        return mClassLockTypes.matches(name);
    }

    public TransformResult getResult() {
        return mResult;
    }

    public static final class HookSpecBuilder {
        private String mName = "";
        private boolean mPreTraceEnabled;
        private boolean mPostTraceEnabled;
        private Method mSyncMethodStart;
        private Method mSyncMethodEnd;
        private Method mPreEnter;
        private Method mPostEnter;
        private Method mPreExit;
        private Method mPostExit;
        private final Patterns mIgnoreClasses = new Patterns(/*defaultMatch=*/ false);
        private final Patterns mTargetClasses = new Patterns(/*defaultMatch=*/ true);
        private final Patterns mInstanceLockTypes = new Patterns(/*defaultMatch=*/ true);
        private final Patterns mClassLockTypes = new Patterns(/*defaultMatch=*/ true);
        private final Patterns mIgnoreAnnotations = new Patterns(/*defaultMatch=*/ false);

        private HookSpecBuilder() {
        }

        public static HookSpecBuilder aHookSpec() {
            return new HookSpecBuilder();
        }

        public HookSpecBuilder withName(String name) {
            this.mName = name;
            return this;
        }

        public HookSpecBuilder withPreTraceEnabled(boolean enabled) {
            this.mPreTraceEnabled = enabled;
            return this;
        }

        public HookSpecBuilder withPostTraceEnabled(boolean enabled) {
            this.mPostTraceEnabled = enabled;
            return this;
        }

        public HookSpecBuilder withSyncMethodStart(Method syncMethodStart) {
            this.mSyncMethodStart = syncMethodStart;
            return this;
        }

        public HookSpecBuilder withSyncMethodEnd(Method SyncMethodEnd) {
            this.mSyncMethodEnd = SyncMethodEnd;
            return this;
        }

        public HookSpecBuilder withPreEnter(Method PreEnter) {
            this.mPreEnter = PreEnter;
            return this;
        }

        public HookSpecBuilder withPostEnter(Method PostEnter) {
            this.mPostEnter = PostEnter;
            return this;
        }

        public HookSpecBuilder withPreExit(Method PreExit) {
            this.mPreExit = PreExit;
            return this;
        }

        public HookSpecBuilder withPostExit(Method PostExit) {
            this.mPostExit = PostExit;
            return this;
        }

        public HookSpecBuilder withIgnoreClass(String IgnoreClass) {
            this.mIgnoreClasses.add(IgnoreClass);
            return this;
        }

        public HookSpecBuilder withTargetClass(String TargetClass) {
            this.mTargetClasses.add(TargetClass);
            return this;
        }

        public HookSpecBuilder withInstanceLockType(String InstanceLockType) {
            this.mInstanceLockTypes.add(InstanceLockType);
            return this;
        }

        public HookSpecBuilder withClassLockType(String ClassLockType) {
            this.mClassLockTypes.add(ClassLockType);
            return this;
        }

        public HookSpecBuilder withIgnoreAnnotation(String IgnoreAnnotation) {
            this.mIgnoreAnnotations.add("L" + IgnoreAnnotation + ";");
            return this;
        }

        public HookSpec build() {
            HookSpec hookSpec = new HookSpec();
            hookSpec.mName = this.mName;
            hookSpec.mPreTraceEnabled = this.mPreTraceEnabled;
            hookSpec.mPostTraceEnabled = this.mPostTraceEnabled;
            hookSpec.mSyncMethodStart = this.mSyncMethodStart;
            hookSpec.mTargetClasses = this.mTargetClasses;
            hookSpec.mPostEnter = this.mPostEnter;
            hookSpec.mClassLockTypes = this.mClassLockTypes;
            hookSpec.mPreEnter = this.mPreEnter;
            hookSpec.mInstanceLockTypes = this.mInstanceLockTypes;
            hookSpec.mIgnoreClasses = this.mIgnoreClasses;
            hookSpec.mPreExit = this.mPreExit;
            hookSpec.mSyncMethodEnd = this.mSyncMethodEnd;
            hookSpec.mPostExit = this.mPostExit;
            hookSpec.mIgnoreAnnotations = this.mIgnoreAnnotations;
            return hookSpec;
        }
    }
}