# Spring Boot Amazon Kinesis Firehose Delivery Stream Producer POC

{
  "cloudwatch.emitMetrics": true,
  "kinesis.endpoint": "kinesis.us-east-2.amazonaws.com",
  "firehose.endpoint": "firehose.us-east-2.amazonaws.com",
  "awsAccessKeyId": "AKIA3M4TGOMV3IDUCODY",
  "awsSecretAccessKey": "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",

  "flows": [
    {
      "filePattern": "/var/log/gpg/*.json*",
      "kinesisStream": "myfirehose",
      "partitionKeyOption": "RANDOM",
      "initialPosition": "START_OF_FILE"
    }
  ]
}

