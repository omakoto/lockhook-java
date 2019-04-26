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
package lockhooktesttarget;

public class SampleMethods {
    private final Object lock1 = new Object();

    public void simpleWithFrame() {
        do {
            Utils.foo();
        } while (Utils.condition());
    }

    public synchronized void simpleSynchronizedWithFrame() {
        do {
            Utils.foo();
        } while (Utils.condition());
    }

    public static void simpleStaticWithFrame() {
        do {
            Utils.foo();
        } while (Utils.condition());
    }

    public static synchronized void simpleStaticSynchronizedWithFrame() {
        do {
            Utils.foo();
        } while (Utils.condition());
    }

    public synchronized void synchronizedWithLock() {
        Hook.post(this);
        synchronized (lock1) {
            Utils.foo();
        }
    }

    public void empty() {
        // This has a return at least.
/*
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "empty", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(27, l0);
            mv.visitInsn(Opcodes.RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Llockhooktesttarget/SampleMethods;", null, l0, l1, 0);
            mv.visitMaxs(0, 1);
            mv.visitEnd();

 */
    }
}
