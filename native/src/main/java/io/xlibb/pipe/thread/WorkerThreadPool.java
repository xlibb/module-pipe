/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.xlibb.pipe.thread;

import java.util.concurrent.ThreadFactory;

/**
 * Class to hold the static cachedThreadPool for the Pipe package.
 */
public class WorkerThreadPool {
    private static final String THREAD_NAME = "bal-pipe-thread";
    public static final String BALLERINA_SQL_MAX_POOL_SIZE = "BALLERINA_SQL_MAX_POOL_SIZE";
    public static final int MAX_POOL_SIZE = Integer.parseInt(
            System.getenv(BALLERINA_SQL_MAX_POOL_SIZE) != null ? System.getenv(BALLERINA_SQL_MAX_POOL_SIZE) : "50"
    );

    private WorkerThreadPool() {
    }

    public static class PipeThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread ballerinaPipe = new Thread(runnable);
            ballerinaPipe.setName(THREAD_NAME);
            return ballerinaPipe;
        }
    }
}
