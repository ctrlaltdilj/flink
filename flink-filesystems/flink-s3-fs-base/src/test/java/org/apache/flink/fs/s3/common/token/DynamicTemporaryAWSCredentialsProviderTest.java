/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.fs.s3.common.token;

import org.apache.flink.util.InstantiationUtil;

import org.apache.hadoop.fs.s3a.auth.NoAwsCredentialsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.services.sts.model.Credentials;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests for {@link DynamicTemporaryAWSCredentialsProvider}. */
class DynamicTemporaryAWSCredentialsProviderTest {

    private static final String ACCESS_KEY_ID = "testAccessKeyId";
    private static final String SECRET_ACCESS_KEY = "testSecretAccessKey";
    private static final String SESSION_TOKEN = "testSessionToken";

    @BeforeEach
    void beforeEach() {
        AbstractS3DelegationTokenReceiver.credentials = null;
    }

    @AfterEach
    void afterEach() {
        AbstractS3DelegationTokenReceiver.credentials = null;
    }

    @Test
    void getCredentialsShouldThrowExceptionWhenNoCredentials() {
        DynamicTemporaryAWSCredentialsProvider provider =
                new DynamicTemporaryAWSCredentialsProvider();

        assertThatThrownBy(provider::resolveCredentials)
                .isInstanceOf(NoAwsCredentialsException.class);
    }

    @Test
    void getCredentialsShouldStoreCredentialsWhenCredentialsProvided() throws Exception {
        DynamicTemporaryAWSCredentialsProvider provider =
                new DynamicTemporaryAWSCredentialsProvider();
        Credentials credentials =
                Credentials.builder()
                        .accessKeyId(ACCESS_KEY_ID)
                        .secretAccessKey(SECRET_ACCESS_KEY)
                        .sessionToken(SESSION_TOKEN)
                        .build();
        AbstractS3DelegationTokenReceiver receiver =
                new AbstractS3DelegationTokenReceiver() {
                    @Override
                    public String serviceName() {
                        return "s3";
                    }
                };

        receiver.onNewTokensObtained(InstantiationUtil.serializeObject(credentials));
        AwsSessionCredentials returnedCredentials =
                (AwsSessionCredentials) provider.resolveCredentials();
        assertThat(returnedCredentials.accessKeyId()).isEqualTo(credentials.accessKeyId());
        assertThat(returnedCredentials.secretAccessKey()).isEqualTo(credentials.secretAccessKey());
        assertThat(returnedCredentials.sessionToken()).isEqualTo(credentials.sessionToken());
    }
}
