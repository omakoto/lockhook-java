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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lockhook.spec.Config;
import lockhook.spec.ConfigException;
import lockhook.spec.HookSpec;
import lockhook.visitors.AllClassStructure;
import lockhook.visitors.LockHookVisitor;

public class Main {
    public static void main(String[] args) throws IOException {
        final Config config;

        try {
            config = Config.parseArgs(args);
        } catch (ConfigException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        final AllClassStructure allClasses = readStructure(config);

        if (config.isVerbose()) {
            allClasses.dump();
        }

        transform(config);

        for (String error : config.getErrors()) {
            System.err.println(error);
        }
        if (config.getErrors().size() > 0) {
            System.exit(2);
        }

        // Print the result.
        if (!config.isQuiet()) {
            for (HookSpec hookSpec : config.getHookSpecList()) {
                System.out.println(hookSpec.getResult());
            }
        }
    }

    private static AllClassStructure readStructure(Config config) throws IOException {
        if (!config.isQuiet()) {
            System.out.println("Reading annotations...");
        }
        final long start = System.currentTimeMillis();

        final AllClassStructure allClasses = new AllClassStructure();

        try (final ZipFile zipSrc = new ZipFile(config.getInFile())) {
            final Enumeration<? extends ZipEntry> srcEntries = zipSrc.entries();
            while (srcEntries.hasMoreElements()) {
                final ZipEntry entry = srcEntries.nextElement();
                try (BufferedInputStream bis =
                        new BufferedInputStream(zipSrc.getInputStream(entry))) {
                    if (entry.getName().endsWith(".class")) {
                        final ClassReader cr = new ClassReader(bis);
                        final ClassNode cn = new ClassNode();
                        cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                                | ClassReader.SKIP_FRAMES);

                        if (!allClasses.addClass(cn)) {
                            config.addError("Class \"" + cn.name + "\" appeared multiple times.");
                        }
                    } else {
                        while (bis.available() > 0) {
                            bis.skip(1024 * 1024);
                        }
                    }
                }
            }
        }
        final long end = System.currentTimeMillis();
        if (!config.isQuiet()) {
            System.out.println(String.format("Done reading annotations in %.1f second(s).",
                    (end - start) / 1000.0));
        }
        return allClasses;
    }

    private static void transform(Config config) throws IOException {

        // Do the transformation.
        try (final ZipFile zipSrc = new ZipFile(config.getInFile())) {
            try (final ZipOutputStream zos =
                         new ZipOutputStream(new FileOutputStream(config.getOutFile()))) {

                final Enumeration<? extends ZipEntry> srcEntries = zipSrc.entries();
                while (srcEntries.hasMoreElements()) {
                    final ZipEntry entry = srcEntries.nextElement();
                    final ZipEntry newEntry = new ZipEntry(entry.getName());
                    zos.putNextEntry(newEntry);
                    try (BufferedInputStream bis =
                                 new BufferedInputStream(zipSrc.getInputStream(entry))) {

                        if (entry.getName().endsWith(".class")) {
                            transformSingleClass(bis, zos, config);
                        } else {
                            while (bis.available() > 0) {
                                zos.write(bis.read());
                            }
                        }
                        zos.closeEntry();
                    }
                }
                zos.finish();
            }
        }
    }

    private static void transformSingleClass(InputStream in, OutputStream out, Config config)
            throws IOException {
        final ClassReader cr = new ClassReader(in);
        final ClassWriter cw = new ClassWriter(0);

        ClassVisitor next = cw;

        // To apply the transforms in the declared order, reverse the list.
        final ArrayList<HookSpec> specList = new ArrayList<>(config.getHookSpecList());
        Collections.reverse(specList);

        for (HookSpec hookSpec : specList) {
            if (config.checkOutput()) {
                next = new CheckClassAdapter(next);
            }
            next = LockHookVisitor.getVisitor(next, config, hookSpec);
        }
        cr.accept(next, ClassReader.EXPAND_FRAMES);
        byte[] data = cw.toByteArray();
        out.write(data);
    }
}
