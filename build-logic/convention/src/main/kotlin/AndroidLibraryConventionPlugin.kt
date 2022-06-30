/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            // Apply common plugins
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.gradle.android.cache-fix")
            }

            // Configure android { ... } block
            extensions.configure<LibraryExtension> {
                compileSdk = Constants.COMPILE_SDK

                defaultConfig {
                    minSdk = Constants.MIN_SDK
                    targetSdk = Constants.TARGET_SDK
                }

                compileOptions {
                    sourceCompatibility = Constants.JVM_TARGET
                    targetCompatibility = Constants.JVM_TARGET
                    isCoreLibraryDesugaringEnabled = true
                }

                (this as ExtensionAware).extensions.configure<KotlinJvmOptions>("kotlinOptions") {
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-opt-in=kotlin.RequiresOptIn",
                        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    )

                    jvmTarget = Constants.JVM_TARGET.toString()
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("coreLibraryDesugaring", libs.findLibrary("android.tools.desugar").get())
            }
        }
    }
}