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

import java.util.ArrayList;
import java.util.List;

public class Hook {
    final static ThreadLocal<List<Object>> locks = ThreadLocal.withInitial(() -> new ArrayList<>());

    public static void syncStart(Object lock) {
        pre(lock);
    }

    public static void syncEnd(Object lock) {
        post(lock);
    }

    public static void preEnter(Object lock) {
        pre(lock);
    }

    public static void postEnter(Object lock) {
    }

    public static void preExit(Object lock) {
    }

    public static void postExit(Object lock) {
        post(lock);
    }

    public static void pre(Object lock) {
        System.out.println(Utils.getStackTrace("PreLock: " + lock));
        locks.get().add(lock);
    }

    public static void post(Object lock) {
        System.out.println(Utils.getStackTrace("PostLock: " + lock));

        final List<Object> list = locks.get();
        if (list.size() == 0) {
            System.out.println("*** Lock underflow!");
        } else {
            list.remove(list.size() - 1);
        }
    }

    public static void finalCheck() {
        final int remainingLocks = locks.get().size();
        if (remainingLocks > 0) {
            throw new IllegalStateException(
                    "*** Unbalanced lock detected! current level=" + remainingLocks);
        }
    }
}
