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

import java.util.Random;

public class Target {
    private static final String LOCK_STRING = "LOCK_STRING";

    private static final Random sRandom = new Random();

    public void run() {
        System.out.println("- Instance method body.");

        System.out.println("---");
        synchronizedEmptyInstanceMethod();

        System.out.println("---");
        synchronizedEmptyStaticMethod();

        System.out.println("---");
        nonSynchronizedInstanceMethod_nohook();

        System.out.println("---");
        synchronizedInstanceMethod();

        System.out.println("---");
        nonSynchronizedStaticMethod_nohook();

        System.out.println("---");
        synchronizedStaticMethod();

        System.out.println("---");
        instanceMethodWithSync();

        System.out.println("---");
        staticMethodWithSync();

        System.out.println("---");
        synchronizedInstanceMethodWithTryFinally();

        System.out.println("---");
        synchronizedWithInnerLock_wrapper();

        synchronizedWithLoop();
        synchronizedWithLoop2();
        synchronizedWithLoop3();
        synchronizedWithLoopWithLocal();
    }

    private synchronized void synchronizedEmptyInstanceMethod() {
        // This still has a RETURN
    }

    private static synchronized void synchronizedEmptyStaticMethod() {
        // This still has a RETURN
    }

    @NoLockHook
    private void nonSynchronizedInstanceMethod_nohook() {
        synchronized (this) {
            System.out.println("- NON Synchronized instance method body.");
        }
    }

    private synchronized void synchronizedInstanceMethod() {
        System.out.println("- Synchronized instance method body.");
    }

    @NoLockHook
    private static void nonSynchronizedStaticMethod_nohook() {
        synchronized (Target.class) {
            System.out.println("- NON Synchronized static method body.");
        }
    }

    private static synchronized void synchronizedStaticMethod() {
        System.out.println("- Synchronized static method body.");
    }

    private void instanceMethodWithSync() {
        synchronized (LOCK_STRING) {
            System.out.println("- Instance method body with sync{}.");
        }
    }

    private static void staticMethodWithSync() {
        synchronized (LOCK_STRING) {
            System.out.println("- Static method body with sync{}.");
        }
    }

    private synchronized void synchronizedInstanceMethodWithTryFinally() {
        try {
            System.out.println("try1");
            System.out.println("- Synchronized instance method body.");
            synchronized (LOCK_STRING) {
                System.out.println("  - nest");
            }
            try {
                synchronized (LOCK_STRING) {
                    System.out.println("try2");
                }
            } finally {
                System.out.println("finally2");
            }
        } finally {
            System.out.println("finally1");
        }
    }

    private void synchronizedWithInnerLock_wrapper() {
        try {
            synchronizedWithInnerLock();
        } catch (Exception e) {
            System.out.println("synchronizedWithInnerLock_wrapper -- caught exception ");
            e.printStackTrace();
        }
    }

    private synchronized void synchronizedWithInnerLock() throws Exception {
        synchronized (LOCK_STRING) {
            System.out.println("- synchronizedWithInnerLock");

            throw new Exception("ok");
        }
    }

    @NoLockHook
    private static void exceptionTest() {
        System.out.println("1");
        try {
            System.out.println("2");
        } catch (RuntimeException e) {
            System.out.println("3");
        } finally {
            System.out.println("4");
        }
        System.out.println("5");
    }

    private static synchronized void synchronizedWithLoop() {
        do {
            System.out.println("loop: ");
        } while (sRandom.nextDouble() < 0.5);
    }

    private static void synchronizedWithLoop2() {
        synchronized (Target.class) {
            do {
                System.out.println("loop: ");
            } while (sRandom.nextDouble() < 0.5);
        }
    }

    private static void synchronizedWithLoop3() {
        do {
            synchronized (Target.class) {
                System.out.println("loop: ");
            }
        } while (sRandom.nextDouble() < 0.5);
    }

    private static synchronized void synchronizedWithLoopWithLocal() {
        int a;
        do {
            a = 0;
            do {
                int b = 1;
                System.out.println("loop: a=" + a + " b=" + b);
            } while (sRandom.nextDouble() < 0.5);
        } while (sRandom.nextDouble() < 0.5);
    }

    @Override
    public String toString() {
        return "Target{}";
    }
}
