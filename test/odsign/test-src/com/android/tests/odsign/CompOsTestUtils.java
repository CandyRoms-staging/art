/*
 * Copyright 2022 The Android Open Source Project
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

package com.android.tests.odsign;

import static com.google.common.truth.Truth.assertWithMessage;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.device.TestDevice;
import com.android.tradefed.util.CommandResult;

import java.util.concurrent.TimeUnit;

public class CompOsTestUtils {
    public static final String APEXDATA_DIR = "/data/misc/apexdata/com.android.compos";

    public static final String PENDING_ARTIFACTS_DIR =
            "/data/misc/apexdata/com.android.art/compos-pending";

    /** Maximum time for a slow VM like cuttlefish to boot and finish odrefresh. */
    // odrefresh overall timeout is currently 480s; add some generous padding for VM startup.
    private static final int VM_ODREFRESH_MAX_SECONDS = 480 + 60;

    /** Waiting time for the job to be scheduled after staging an APEX */
    private static final int JOB_CREATION_MAX_SECONDS = 5;

    /** Waiting time before starting to check odrefresh progress. */
    private static final int SECONDS_BEFORE_PROGRESS_CHECK = 30;

    /** Job ID of the pending compilation with staged APEXes. */
    private static final String JOB_ID = "5132251";

    private final ITestDevice mDevice;

    public CompOsTestUtils(ITestDevice device) {
        mDevice = device;
    }

    /**
     * Start CompOS compilation right away, and return once the job completes successfully.
     *
     * <p>Once the test APK is installed, a CompilationJob is (asynchronously) scheduled to run
     * when certain criteria are met, e.g. the device is charging and idle. Since we don't want
     * to wait in the test, here we start the job by ID as soon as it is scheduled.
     */
    public void runCompilationJobEarlyAndWait() throws Exception {
        waitForJobToBeScheduled();

        assertCommandSucceeds("cmd jobscheduler run android " + JOB_ID);
        // It takes time. Just don't spam.
        TimeUnit.SECONDS.sleep(SECONDS_BEFORE_PROGRESS_CHECK);
        // The job runs asynchronously. To wait until it completes.
        waitForJobExit(VM_ODREFRESH_MAX_SECONDS - SECONDS_BEFORE_PROGRESS_CHECK);
    }

    public String checksumDirectoryContentPartial(String path) throws Exception {
        // Sort by filename (second column) to make comparison easier.
        // Filter out compos.info* (which will be deleted at boot) and cache-info.xml
        // compos.info.signature since it's only generated by CompOS.
        return assertCommandSucceeds("cd " + path + "; find -type f -exec sha256sum {} \\;"
                + "| grep -v cache-info.xml | grep -v compos.info"
                + "| sort -k2");
    }

    private void waitForJobToBeScheduled()
            throws Exception {
        for (int i = 0; i < JOB_CREATION_MAX_SECONDS; i++) {
            CommandResult result = mDevice.executeShellV2Command(
                    "cmd jobscheduler get-job-state android " + JOB_ID);
            String state = result.getStdout().toString();
            if (state.startsWith("unknown")) {
                // The job hasn't been scheduled yet. So try again.
                TimeUnit.SECONDS.sleep(1);
            } else if (result.getExitCode() != 0) {
                fail("Failing due to unexpected job state: " + result);
            } else {
                // The job exists, which is all we care about here
                return;
            }
        }
        fail("Timed out waiting for the job to be scheduled");
    }

    private void waitForJobExit(int timeout) throws Exception {
        for (int i = 0; i < timeout; i++) {
            CommandResult result = mDevice.executeShellV2Command(
                    "cmd jobscheduler get-job-state android " + JOB_ID);
            String state = result.getStdout().toString();
            if (state.contains("ready") || state.contains("active")) {
                TimeUnit.SECONDS.sleep(1);
            } else if (state.startsWith("unknown")) {
                // Job has completed
                return;
            } else {
                fail("Failing due to unexpected job state: " + result);
            }
        }
        fail("Timed out waiting for the job to complete");
    }

    public void assumeCompOsPresent() throws Exception {
        // We have to have kernel support for a VM.
        assumeTrue("Need an actual TestDevice", mDevice instanceof TestDevice);
        TestDevice testDevice = (TestDevice) mDevice;
        assumeTrue("Requires VM support", testDevice.supportsMicrodroid());

        // And the CompOS APEX must be present.
        assumeTrue(mDevice.doesFileExist("/apex/com.android.compos/"));
    }

    public void assumeNotOnCuttlefish() throws Exception {
        String product = mDevice.getProperty("ro.build.product");
        assumeTrue(product != null && !product.startsWith("vsoc_"));
    }

    private String assertCommandSucceeds(String command) throws DeviceNotAvailableException {
        CommandResult result = mDevice.executeShellV2Command(command);
        assertWithMessage(result.toString()).that(result.getExitCode()).isEqualTo(0);
        return result.getStdout().trim();
    }
}
