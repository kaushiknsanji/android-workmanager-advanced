# WorkManager Advanced

**Blur-O-Matic** app built by following the instructions detailed in the Google Codelab **["Advanced WorkManager"][WorkManager Advanced Codelab]**. Original code by Google for this codelab can be referred [here][WorkManager Advanced Repository].

## What one will learn

* Creating custom `Configuration` for WorkManager
* Tagging WorkRequests to publish and obtain Progress `Data` for displaying in the UI
* Testing the Workers using WorkManager Testing library `work-testing` artifact 

## What is not taught

Advanced stuff like:
* Periodic Work requests
* Parallel Work requests
* Input Mergers
* CoroutineWorker
* Threading in Workers

## Getting Started

* Android Studio 3.6 or higher with updated SDK and Gradle.
* Android device or emulator running API level 14+.

### Prerequisites
* Familiarity with the Kotlin programming language, object-oriented design concepts and Android Development Fundamentals
* Basic layouts, widgets and [View Bindings](https://d.android.com/topic/libraries/view-binding)
* Some familiarity with Uris and File I/O
* Familiarity with [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) and [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
* Familiarity with the basics of `WorkManager` from the [codelab](https://codelabs.developers.google.com/codelabs/android-workmanager/index.html)

## Branches in this Repository

* **[starter-code-kotlin](https://github.com/kaushiknsanji/android-workmanager-advanced/tree/starter-code-kotlin)**
	* This is the Starter code for the [codelab][WorkManager Advanced Codelab], which is actually based on the [code](https://github.com/kaushiknsanji/android-workmanager-basics/tree/master) of the final version of the [basics codelab](https://codelabs.developers.google.com/codelabs/android-workmanager/index.html).
* **[master](https://github.com/kaushiknsanji/android-workmanager-advanced/tree/master)**
	* This contains the Solution for the [codelab][WorkManager Advanced Codelab].
    * In comparison with the original [repository][WorkManager Advanced Repository], this repository contains modified [TestUtils](https://github.com/kaushiknsanji/android-workmanager-advanced/blob/master/app/src/androidTest/java/com/example/background/workers/TestUtils.kt) with minor changes to maintain idiomatic Kotlin usage.

## License

Copyright 2018 Google, Inc.

All image and audio files (including *.png, *.jpg, *.svg, *.mp3, *.wav
and *.ogg) are licensed under the CC BY 4.0 license. All other files are
licensed under the Apache 2 license.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the LICENSE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.		

<!-- Reference Style Links are to be placed after this -->
[WorkManager Advanced Codelab]: https://codelabs.developers.google.com/codelabs/android-adv-workmanager/index.html
[WorkManager Advanced Repository]: https://github.com/googlecodelabs/android-workmanager/tree/advanced
