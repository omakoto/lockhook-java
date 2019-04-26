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

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lockhook.Method;
import lockhook.spec.HookSpec.HookSpecBuilder;

/**
 * TODO Rename to Context or something?
 */
public class Config {
    private String mInFile;
    private String mOutFile;
    private boolean mVerbose;
    private boolean mQuiet;
    private boolean mCheckOutput = true;

    private final List<HookSpec> mHookSpecList = new ArrayList<>();

    private final List<String> mErrors = new ArrayList<>();

    public Config() {
    }

    public String getInFile() {
        return mInFile;
    }

    public void setInFile(String inFile) {
        if (mInFile != null) {
            throw new ConfigException("Input file already specified");
        }
        mInFile = inFile;
    }

    public String getOutFile() {
        return mOutFile;
    }

    public void setOutFile(String outFile) {
        if (mOutFile != null) {
            throw new ConfigException("Output file already specified");
        }
        mOutFile = outFile;
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
        if (verbose) {
            mCheckOutput = true; // Also enable output validation.
        }
    }

    public boolean isQuiet() {
        return mQuiet;
    }

    public void setQuiet(boolean quiet) {
        mQuiet = quiet;
    }

    public boolean checkOutput() {
        return mCheckOutput;
    }

    public void setCheckOutput(boolean checkOutput) {
        mCheckOutput = checkOutput;
    }

    public List<HookSpec> getHookSpecList() {
        return Collections.unmodifiableList(mHookSpecList);
    }

    public int getErrorCount() {
        return mErrors.size();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(mErrors);
    }

    public void addError(String message) {
        mErrors.add(message);
    }

    public void validate() {
        if (mInFile == null || mOutFile == null) {
            throw new ConfigException("Both -i and -o must be specified");
        }
        if (mHookSpecList.size() == 0) {
            throw new ConfigException("At least one hook spec must be specified");
        }
    }

    public static Config parseArgs(String[] args) {
        final Config config = new Config();
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if ("-i".equals(arg)) {
                i++;
                config.setInFile(args[i]);
            } else if ("-o".equals(arg)) {
                i++;
                config.setOutFile(args[i]);
            } else if ("--spec".equals(arg)) {
                i++;
                config.loadYamlSpec(args[i]);
            } else if ("-v".equals(arg)) {
                config.setVerbose(true);
            } else if ("-q".equals(arg)) {
                config.setQuiet(true);
            } else if ("-d".equals(arg)) {
                config.setCheckOutput(true);
            } else if ("--no-check".equals(arg)) {
                config.setCheckOutput(false);
            } else {
                throw new ConfigException("Unknown argument: " + arg);
            }
        }

        config.validate();
        return config;
    }

    public void loadYamlSpec(String filename) {
        final Yaml yaml = new Yaml();
        try {
            for (Object o : yaml.loadAll(new FileInputStream(new File(filename)))) {
                final Map<String, Object> map = (Map<String, Object>) o;

                final HookSpecBuilder b = HookSpecBuilder.aHookSpec();

                for (String key : map.keySet()) {
                    // Process no arg options first.
                    switch (key) {
                        case "trace":
                        case "post_trace":
                            b.withPostTraceEnabled(true);
                            continue;

                        case "pre_trace":
                            b.withPreTraceEnabled(true);
                            continue;
                    }

                    final Object value = map.get(key);
                    if (value == null) {
                        continue;
                    }

                    final Consumer<Consumer<String>> listArg = s ->{
                        boolean invalid = false;
                        if (value instanceof Map) {
                            invalid = true;
                        } else if (value instanceof List) {
                            for (Object v : (List<?>) value) {
                                s.accept(v.toString());
                            }
                        } else {
                            s.accept(value.toString());
                        }
                        if (invalid) {
                            throw new ConfigException(
                                    "\"" + key + "\" takes a string or a string list, but was "
                                            + "\"" + value + "\" of type " + value.getClass());
                        }
                    };

                    final Supplier<String> ensureStringValue = () -> {
                        if ((value instanceof Map) || (value instanceof List)) {
                            throw new ConfigException(
                                    "\"" + key + "\" takes a string, but was "
                                            + "\"" + value + "\" of type " + value.getClass());
                        }
                        return value.toString();
                    };

                    switch (key) {
                        case "name":
                            listArg.accept(b::withName);
                            break;
                        case "class":
                            listArg.accept(b::withTargetClass);
                            break;
                        case "ignore":
                            listArg.accept(b::withIgnoreClass);
                            break;
                        case "ignore_annotation":
                            listArg.accept(b::withIgnoreAnnotation);
                            break;
                        case "instance_lock_types":
                            listArg.accept(b::withInstanceLockType);
                            break;
                        case "class_lock_types":
                            listArg.accept(b::withClassLockType);
                            break;

                        case "sync_method_start":
                            b.withSyncMethodStart(Method.parse(ensureStringValue.get()));
                            break;
                        case "sync_method_end":
                            b.withSyncMethodEnd(Method.parse(ensureStringValue.get()));
                            break;

                        case "pre_enter":
                            b.withPreEnter(Method.parse(ensureStringValue.get()));
                            break;
                        case "post_enter":
                            b.withPostEnter(Method.parse(ensureStringValue.get()));
                            break;

                            case "pre_exit":
                            b.withPreExit(Method.parse(ensureStringValue.get()));
                            break;
                        case "post_exit":
                            b.withPostExit(Method.parse(ensureStringValue.get()));
                            break;
                        default:
                            throw new ConfigException("Unknown key \"" + key + "\"");
                    }
                }
                mHookSpecList.add(b.build());
            }
        } catch (FileNotFoundException e) {
            throw new ConfigException("Unable to load file " + filename, e);
        }

    }
}
