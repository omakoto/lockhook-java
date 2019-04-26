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

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.InstructionAdapter;

/**
 * Extend {@link InstructionAdapter} and provide pre and post instruction callbacks.
 */
public class PrePostVisitInsnAdapter extends InstructionAdapter {
    private int mIndex;
    private int mVisitLevel;

    public PrePostVisitInsnAdapter(int api, MethodVisitor mv) {
        super(api, mv);
    }

    /** Index of the current instruction */
    public int getCurrentInstructionIndex() {
        return mIndex;
    }

    private void doPreInsn() {
        mVisitLevel++;
        if (mVisitLevel == 1) {
            preInsn();
        }
    }

    private void doPostInsn() {
        mVisitLevel--;
        if (mVisitLevel == 0) {
            postInsn();
        }
        mIndex++;
    }

    /** Called before each instruction visit method. */
    protected void preInsn() {
    }

    /** Called after each instruction visit method. */
    protected void postInsn() {
    }

    @Override
    public void visitInsn(int opcode) {
        doPreInsn();
        super.visitInsn(opcode);
        doPostInsn();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        doPreInsn();
        super.visitIntInsn(opcode, operand);
        doPostInsn();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        doPreInsn();
        super.visitVarInsn(opcode, var);
        doPostInsn();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        doPreInsn();
        super.visitTypeInsn(opcode, type);
        doPostInsn();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        doPreInsn();
        super.visitFieldInsn(opcode, owner, name, desc);
        doPostInsn();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        doPreInsn();
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        doPostInsn();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        doPreInsn();
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        doPostInsn();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        doPreInsn();
        super.visitJumpInsn(opcode, label);
        doPostInsn();
    }

    @Override
    public void visitLdcInsn(Object cst) {
        doPreInsn();
        super.visitLdcInsn(cst);
        doPostInsn();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        doPreInsn();
        super.visitIincInsn(var, increment);
        doPostInsn();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        doPreInsn();
        super.visitTableSwitchInsn(min, max, dflt, labels);
        doPostInsn();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        doPreInsn();
        super.visitLookupSwitchInsn(dflt, keys, labels);
        doPostInsn();
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        doPreInsn();
        super.visitMultiANewArrayInsn(desc, dims);
        doPostInsn();
    }
}
