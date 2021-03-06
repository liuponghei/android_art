/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package art;

public class Test1908 {
  public static void run() throws Exception {
    final Thread spinner = new Thread(() -> {
      int ret = nativeSpinAndResume(Thread.currentThread());
      if (ret != 13) {
        System.out.println("Got " + ret + " instead of JVMTI_ERROR_THREAD_NOT_SUSPENDED");
      }
    }, "Spinner");

    final Thread resumer = new Thread(() -> {
      String me = Thread.currentThread().getName();

      // wait for the other thread to start spinning.
      while (!isNativeThreadSpinning()) { }

      System.out.println(me + ": isNativeThreadSpinning() = " + isNativeThreadSpinning());
      System.out.println(me + ": isSuspended(spinner) = " + Suspension.isSuspended(spinner));

      // Suspend it from java.
      Suspension.suspend(spinner);

      System.out.println(me + ": Suspended spinner while native spinning");
      System.out.println(me + ": isNativeThreadSpinning() = " + isNativeThreadSpinning());
      System.out.println(me + ": isSuspended(spinner) = " + Suspension.isSuspended(spinner));

      System.out.println("Resuming other thread");
      nativeResume();
      waitForNativeResumeStarted();
      // Wait for the other thread to try to resume itself
      try { Thread.currentThread().sleep(1000); } catch (Exception e) {}

      System.out.println("other thread attempting self resume");
      System.out.println(me + ": isSuspended(spinner) = " + Suspension.isSuspended(spinner));

      System.out.println("real resume");
      Suspension.resume(spinner);
      waitForNativeResumeFinished();
      System.out.println("other thread resumed.");
    }, "Resumer");

    spinner.start();
    resumer.start();

    spinner.join();
    resumer.join();
  }

  public static native int nativeSpinAndResume(Thread cur);
  public static native void nativeResume();
  public static native boolean isNativeThreadSpinning();
  public static native void waitForNativeResumeFinished();
  public static native void waitForNativeResumeStarted();
}
