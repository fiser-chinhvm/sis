/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.test;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.apache.sis.util.Static;
import org.apache.sis.util.ArgumentChecks;

import static org.junit.Assert.*;


/**
 * Miscellaneous utility methods for test cases.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.16)
 * @version 0.3
 * @module
 */
public final strictfp class TestUtilities extends Static {
    /**
     * Maximal time that {@code waitFoo()} methods can wait, in milliseconds.
     *
     * @see #waitForBlockedState(Thread)
     * @see #waitForGarbageCollection(Callable)
     */
    private static final int MAXIMAL_WAIT_TIME = 1000;

    /**
     * Date parser and formatter using the {@code "yyyy-MM-dd HH:mm:ss"} pattern
     * and UTC time zone.
     */
    private static final DateFormat dateFormat;
    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
    };

    /**
     * Do not allow instantiation of this class.
     */
    private TestUtilities() {
    }

    /**
     * Parses the date for the given string using the {@code "yyyy-MM-dd HH:mm:ss"} pattern
     * in UTC timezone.
     *
     * @param  date The date as a {@link String}.
     * @return The date as a {@link Date}.
     */
    public static Date date(final String date) {
        ArgumentChecks.ensureNonNull("date", date);
        try {
            synchronized (dateFormat) {
                return dateFormat.parse(date);
            }
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Formats the given date using the {@code "yyyy-MM-dd HH:mm:ss"} pattern in UTC timezone.
     *
     * @param  date The date to format.
     * @return The date as a {@link String}.
     */
    public static String format(final Date date) {
        ArgumentChecks.ensureNonNull("date", date);
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    /**
     * If the given failure is not null, re-thrown it as an {@link Error} or
     * {@link RuntimeException}. Otherwise do nothing.
     *
     * @param failure The exception to re-thrown if non-null.
     */
    public static void rethrownIfNotNull(final Throwable failure) {
        if (failure != null) {
            if (failure instanceof Error) {
                throw (Error) failure;
            }
            if (failure instanceof RuntimeException) {
                throw (RuntimeException) failure;
            }
            throw new UndeclaredThrowableException(failure);
        }
    }

    /**
     * Waits up to one second for the given thread to reach the
     * {@linkplain java.lang.Thread.State#BLOCKED blocked} or the
     * {@linkplain java.lang.Thread.State#WAITING waiting} state.
     *
     * @param  thread The thread to wait for blocked or waiting state.
     * @throws IllegalThreadStateException If the thread has terminated its execution,
     *         or has not reached the waiting or blocked state before the timeout.
     * @throws InterruptedException If this thread has been interrupted while waiting.
     */
    public static void waitForBlockedState(final Thread thread) throws IllegalThreadStateException, InterruptedException {
        int retry = MAXIMAL_WAIT_TIME / 5; // 5 shall be the same number than in the call to Thread.sleep.
        do {
            Thread.sleep(5);
            switch (thread.getState()) {
                case WAITING:
                case BLOCKED: return;
                case TERMINATED: throw new IllegalThreadStateException("The thread has completed execution.");
            }
        } while (--retry != 0);
        throw new IllegalThreadStateException("The thread is not in a blocked or waiting state.");
    }

    /**
     * Waits up to one second for the garbage collector to do its work. This method can be invoked
     * only if {@link TestConfiguration#allowGarbageCollectorDependentTests()} returns {@code true}.
     * <p>
     * Note that this method does not throw any exception if the given condition has not been
     * reached before the timeout. Instead, it is the caller responsibility to test the return
     * value. This method is designed that way because the caller can usually produce a more
     * accurate error message about which value has not been garbage collected as expected.
     *
     * @param  stopCondition A condition which return {@code true} if this method can stop waiting,
     *         or {@code false} if it needs to ask again for garbage collection.
     * @return {@code true} if the given condition has been meet, or {@code false} if we waited up
     *         to the timeout without meeting the given condition.
     * @throws InterruptedException If this thread has been interrupted while waiting.
     */
    public static boolean waitForGarbageCollection(final Callable<Boolean> stopCondition) throws InterruptedException {
        assertTrue("GC-dependent tests not allowed in this run.", TestConfiguration.allowGarbageCollectorDependentTests());
        int retry = MAXIMAL_WAIT_TIME / 50; // 50 shall be the same number than in the call to Thread.sleep.
        boolean stop;
        do {
            if (--retry == 0) {
                return false;
            }
            Thread.sleep(50);
            System.gc();
            try {
                stop = stopCondition.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        } while (!stop);
        return true;
    }
}
