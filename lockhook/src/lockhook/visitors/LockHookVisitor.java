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
package lockhook.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import lockhook.Method;
import lockhook.TransformResult;
import lockhook.spec.Config;
import lockhook.spec.HookSpec;

/**
 * Insert pre and post lock hook calls.
 */
public class LockHookVisitor extends ClassVisitor {
    public static final int ASM_VERSION = Opcodes.ASM6;

    public static final String METHOD_SIGNATURE = "(Ljava/lang/Object;)V";

    private final Config mConfig;

    private final HookSpec mHookSpec;

    /** Class name */
    private String mClassName;

    /** Class type */
    private Type mClassType;

    /** Whether this class should be transformed. */
    private boolean mShouldBeTransformed;

    /** Whether this class has a "NoLockHook" annotation or not. */
    private boolean mHasIgnoreAnnotation;

    private final TransformResult mResult;

    private int mTransformedMethodCount;

    /** Return a new instance (optionally connected with {@link TraceClassVisitor}). */
    public static ClassVisitor getVisitor(ClassVisitor chain, Config config, HookSpec hookSpec) {

        // Inject TraceClassVisitor for debugging.
        if (hookSpec.isPostTraceEnabled()) {
            chain = new TraceClassVisitor(chain, new PrintWriter(System.out));
        }

        ClassVisitor ret = new LockHookVisitor(chain, config, hookSpec);

        // Inject TraceClassVisitor for debugging.
        if (hookSpec.isPreTraceEnabled()) {
            ret = new TraceClassVisitor(ret, new PrintWriter(System.out));
        }

        return ret;
    }

    /** Real constructor. */
    private LockHookVisitor(ClassVisitor chain, Config config, HookSpec hookSpec) {
        super(ASM_VERSION, chain);
        mConfig = config;
        mHookSpec = hookSpec;
        mResult = hookSpec.getResult();
    }

    private void log(String message) {
        System.out.println(mHookSpec.getName() + ": " + message);
    }

    private void log(String format, Object... args) {
        System.out.println(mHookSpec.getName() + ": " + String.format(format, args));
    }

    private void verbose(String message) {
        if (mConfig.isVerbose()) {
            log(message);
        }
    }

    private void verbose(String format, Object... args) {
        if (mConfig.isVerbose()) {
            log(format, args);
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        mClassName = name;
        mClassType = Type.getType("L" + mClassName + ";");
        mShouldBeTransformed = mHookSpec.isTargetClass(name);

        verbose("Class: %s", name);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    /** Check if the class has a "non hook" annotation or not. */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        verbose("    Annotation: %s", desc);

        if (mHookSpec.isIgnoreAnnotation(desc)) {
            mHasIgnoreAnnotation = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    /** @return true if this class needs to be transformed. */
    private boolean shouldBeTransformed() {
        return mShouldBeTransformed && !mHasIgnoreAnnotation;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (!shouldBeTransformed()) {
            return mv;
        } else {
            verbose("  Method: %s", name);

            mResult.incrementTransformedMethodCount();
            mTransformedMethodCount++;

            return new LockHookMethodVisitor(mv, access, name, desc);
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        mResult.incrementTransformedClassCount();

        if (mConfig.isVerbose()) {
            if (shouldBeTransformed()) {
                log("Class %s, %d method(s) transformed.", mClassName, mTransformedMethodCount);
            } else {
                log("Class %s, skipped.", mClassName);
            }
        }
    }

    /**
     * Represents a try-catch block.
     */
    private static class TryCatchBlock {
        public final Label start;
        public final Label end;
        public final Label handler;
        public final String type;

        /** Whether the current instruction is within this try-catch block. */
        public boolean in;

        private TryCatchBlock(Label start, Label end, Label handler, String type) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.type = type;
        }
    }

    private class LockHookMethodVisitor extends PrePostVisitInsnAdapter {
        /** Whether this method is static or not. */
        private final boolean mIsStatic;

        /** Whether this method is synchronized or not. */
        private final boolean mIsSynchronized;

        /** All try-catch blocks found in this method so far. */
        private final List<TryCatchBlock> mTryCatchBlocks = new ArrayList<>();

        /** Whether this method has a "NoLockHook" annotation or not. */
        private boolean mHasIgnoreAnnotation;

        /** Extend the max stack size by this value. */
        private int mExtraStack;

        /** Number of try-catch blocks we're currently in. */
        private int mActiveTryCatchBlockCount;

        /**
         * Label we set at the beginning of the method. We only create and use it when a method
         * start hook is injected.
         */
        private Label mLabelMethodStart;

        /** {@link AnalyzerAdapter} used to obtain the value types in the stack. */
        private final AnalyzerAdapter mAnalyzerAdapter;

        private int mOriginalFirstInstructionIndex;

        public LockHookMethodVisitor(MethodVisitor chain, int access, String name, String desc) {
            super(ASM_VERSION, new AnalyzerAdapter(mClassName, access, name, desc, chain));

            mIsSynchronized = (access & Opcodes.ACC_SYNCHRONIZED) != 0;
            mIsStatic = (access & Opcodes.ACC_STATIC) != 0;
            mAnalyzerAdapter = (AnalyzerAdapter) mv;
        }

        /** Whether this class should be transformed. */
        private boolean shouldBeTransformed() {
            return !mHasIgnoreAnnotation;
        }

        private Object getStackType(int index, String operation) {
            if (mAnalyzerAdapter.stack == null || mAnalyzerAdapter.stack.size() < index) {
                throw new IllegalStateException("Stack underflow for " + operation);
            }
            return mAnalyzerAdapter.stack.get(mAnalyzerAdapter.stack.size() - index - 1);
        }

        /** Ensure {@link #mExtraStack} is at lest {@code n}. */
        private void ensureExtraStack(int n) {
            if (mExtraStack < n) {
                mExtraStack = n;
            }
        }

        /**
         * If the method has an "no lock hook" annotation, set mHasIgnoreAnnotation.
         */
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            verbose("    Annotation: %s", desc);

            if (mHookSpec.isIgnoreAnnotation(desc)) {
                mHasIgnoreAnnotation = true;
            }
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // If it's a synchronized method, inject a hook call if needed.
            if (shouldBeTransformed() && mIsSynchronized
                    && (mHookSpec.getSyncMethodStart() != null)
                    && isSynchronizedMethodLockTarget()) {

                mLabelMethodStart = new Label();
                visitLabel(mLabelMethodStart);

                ensureExtraStack(1);

                if (mIsStatic) {
                    visitLdcInsn(mClassType);
                } else {
                    visitVarInsn(Opcodes.ALOAD, 0);
                }
                injectHookCall(mHookSpec.getSyncMethodStart());

                mOriginalFirstInstructionIndex = getCurrentInstructionIndex();
            }
        }

        /** Collect all try-catch blocks into {@link #mTryCatchBlocks}. */
        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            super.visitTryCatchBlock(start, end, handler, type);

            mTryCatchBlocks.add(new TryCatchBlock(start, end, handler, type));
        }

        /**
         * Does two tasks:
         * - Collect all labels at instruction at 0.
         * - Keep track of "active" try-catch blocks.
         */
        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);

            // Keeps track of which try-catch block is active.
            for (TryCatchBlock tcb : mTryCatchBlocks) {
                if (tcb.start.equals(label)) {
                    tcb.in = true;
                    mActiveTryCatchBlockCount++;
                } else if (tcb.end.equals(label)) {
                    tcb.in = false;
                    mActiveTryCatchBlockCount--;
                }
            }
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            super.visitLineNumber(line, start);

            // Copy the line number of the first instruction to our start label.
            // Ideally we want to use the line number of the function declaration, but
            // we can't get the line number of it.
            if ((mLabelMethodStart != null)
                    && (getCurrentInstructionIndex() == mOriginalFirstInstructionIndex)) {
                super.visitLineNumber(line, mLabelMethodStart);
            }
        }

        /**
         * Inject MONITORENTER pre and post hooks if needed.
         */
        @Override
        public void monitorenter() {
            monitor(mHookSpec.getPreEnter(), mHookSpec.getPostEnter(), "MONITORENTER",
                    super::monitorenter);
        }

        /**
         * Inject MONITOREXIT pre and post hooks if needed.
         */
        @Override
        public void monitorexit() {
            monitor(mHookSpec.getPreExit(), mHookSpec.getPostExit(), "MONITOREXIT",
                    super::monitorexit);
        }

        private void monitor(Method pre, Method post, String opname, Runnable baseCall) {
            if (!shouldBeTransformed()) {
                baseCall.run();
                return;
            }

            final Object stack0 = getStackType(0, opname);
            verbose("        %s with %s", opname, stack0);

            // If it's not of a target type, don't inject.
            if (!mHookSpec.isInstanceLockType((String) stack0)) {
                baseCall.run();
                return;
            }

            final int n = (pre != null ? 1 : 0) + (post != null ? 1 : 0);

            ensureExtraStack(n);
            for (int i = 0; i < n; i++) {
                visitInsn(Opcodes.DUP);
            }

            if (pre != null) {
                injectHookCall(pre);
            }
            baseCall.run();
            if (post != null) {
                injectHookCall(post);
            }
        }

        /** Inject a post-hook call if needed. */
        @Override
        public void areturn(Type t) {
            beforeReturn();
            super.areturn(t);
        }

        /** If there's no try-catch block for this throw, inject a post-hook call if needed. */
        @Override
        public void athrow() {
            // Only inject a post call if it's not in any try-catch blocks.
            if (mActiveTryCatchBlockCount == 0) {
                beforeReturn();
            }
            super.athrow();
        }

        private boolean isSynchronizedMethodLockTarget() {
            if (mIsStatic) {
                return mHookSpec.isClassLockType(mClassName);
            } else {
                return mHookSpec.isInstanceLockType(mClassName);
            }
        }

        /**
         * Called before the instruction that escapes the current method.
         * If it's a synchronized method, insert a post hook call.
         */
        private void beforeReturn() {
            if (shouldBeTransformed() && mIsSynchronized
                    && (mHookSpec.getSyncMethodEnd() != null)
                    && isSynchronizedMethodLockTarget()) {
                ensureExtraStack(1);

                if (mIsStatic) {
                    visitLdcInsn(mClassType);
                } else {
                    visitVarInsn(Opcodes.ALOAD, 0);
                }
                injectHookCall(mHookSpec.getSyncMethodEnd());
            }
        }

        /** Inject a hook call. */
        private void injectHookCall(Method method) {
            visitMethodInsn(Opcodes.INVOKESTATIC, method.className, method.methodName,
                    METHOD_SIGNATURE, false);
        }

        /** Increase the max stack size by {@link #mExtraStack}. */
        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + mExtraStack, maxLocals);
        }
    }
}