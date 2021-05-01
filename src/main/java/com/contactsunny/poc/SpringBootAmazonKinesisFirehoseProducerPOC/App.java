package com.contactsunny.poc.SpringBootAmazonKinesisFirehoseProducerPOC;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.PutRecordResult;
import com.amazonaws.services.kinesisfirehose.model.Record;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import java.nio.ByteBuffer;

@SpringBootApplication
public class App implements CommandLineRunner {

    @Value("${aws.auth.roleARN}")
    private String roleARN;

    @Value("${aws.auth.roleSessionName}")
    private String roleSessionName;

    @Value("${aws.kinesis.firehose.deliveryStream.name}")
    private String fireHoseDeliveryStreamName;



    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {




        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                                                    .withCredentials(new ProfileCredentialsProvider())
                                                    .withRegion(Regions.EU_WEST_1)
                                                    .build();

        //BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                                                    .withRoleArn(roleARN)
                                                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();
            
            // Create a BasicSessionCredentials object that contains the credentials you just retrieved.
            BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());


        AmazonKinesisFirehose firehoseClient = AmazonKinesisFirehoseClient.builder()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        JSONObject messageJson = new JSONObject();
        messageJson.put("key1", "We are testing Amazon Kinesis Firehose!");
        messageJson.put("integerKey", 123);
        messageJson.put("booleanKey", true);
        messageJson.put("anotherString", "This should work!");

        logger.info("Message to Firehose: " + messageJson.toString());

        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setDeliveryStreamName(fireHoseDeliveryStreamName);

        Record record = new Record().withData(ByteBuffer.wrap(messageJson.toString().getBytes()));
        putRecordRequest.setRecord(record);

        PutRecordResult putRecordResult = firehoseClient.putRecord(putRecordRequest);

        logger.info("Message record ID: " + putRecordResult.getRecordId());
    }
}
