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

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllClassStructure {
    final Map<String, ClassNode> mAllClasses = new HashMap<>();

    public boolean addClass(ClassNode cn) {
        if (mAllClasses.containsKey(cn)) {
            return false;
        }
        mAllClasses.put(cn.name, cn);
        return true;
    }

    public void dump() {
        for (String name : mAllClasses.keySet()) {
            System.out.println("Clsas " + name);

            final ClassNode cn = mAllClasses.get(name);
            for (TypeAnnotationNode an : ensureNonNull(cn.visibleTypeAnnotations)) {
                System.out.println("    TypeAnnotation " + an.desc);
                // TODO What's this??
            }
            for (AnnotationNode an : ensureNonNull(cn.visibleAnnotations)) {
                System.out.println("    Annotation " + an.desc);

                if (an.values == null) {
                    continue;
                }
//                for (int i = 0; i < an.values.size() - 1; i += 2) {
//                    System.out.println("      : " + an.values.get(i));
//                    System.out.println("       -> " + an.values.get(i + 1));
//                }
                System.out.println(new Yaml().dump(an.values));
            }
        }
    }

    public static <T> List<T> ensureNonNull(List<T> list) {
        return list != null ? list : Collections.EMPTY_LIST;
    }
}
