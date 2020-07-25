package com.aws.lambda.sample;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

import static com.aws.lambda.sample.Utils.getParamFromList;

/**
 * Class which will handle the db stream and validate the json and upload the valid json to the S3 bucket
 */
public class DynamoDBEventHandler implements RequestHandler<DynamodbEvent, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBEventHandler.class);

    static List<Parameter> parameters;

    /**
     * Request handler method which intercept the stream and validate the json
     *
     * @param dynamodbEvent
     * @param context
     * @return
     */

    @Override
    public String handleRequest(DynamodbEvent dynamodbEvent, Context context) {
        parameters = new SystemManagementParameter().getSystemManagementParameter();
        LOGGER.info("DynamoDBEventHandler handling request {}", dynamodbEvent.getRecords());
        List<DynamodbEvent.DynamodbStreamRecord> records = dynamodbEvent.getRecords();
        if (records != null) {
            for (DynamodbEvent.DynamodbStreamRecord record : records) {
                StringBuilder sb = new StringBuilder();
                sb.append("EventName=").append(record.getEventName());
                StreamRecord dynamoDB = record.getDynamodb();
                try {
                    if (dynamoDB != null) {
                        if (dynamoDB.getNewImage() != null) {
                            if (dynamoDB.getNewImage().containsKey("payload")) {
                                String payloadFromDB = dynamoDB.getNewImage().get("payload").getS();

                                // validate and transform the data from dynamo db
                                //String jsonFile = validateAndTransformData(new JSONObject(payloadFromDB));

                                if (payloadFromDB != null && payloadFromDB.length() > 0) {
                                    uploadObject(payloadFromDB);
                                } else {
                                    LOGGER.info("The content is blank hence there is no file upload");
                                }

                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else {
            JSONObject obj = getBucket();
            if (obj != null){
                obj.put("updated", Utils.getFormattedCurrentDate("yyyy-MM-dd HH:mm:ss"));
                uploadObject(obj.toString());
            }
        }
        return "OK";
    }

    /**
     * Upload the object to the S3 bucket
     *
     * @param jsonFromDB
     */
    private void uploadObject(String jsonFromDB) {

        Regions clientRegion = Regions.EU_WEST_2;
        String bucketName = getParamFromList(parameters, Constants.S3BUCKET_NAME);
        String stringObjKeyName = getParamFromList((List)DynamoDBEventHandler.parameters, Constants.OUTPUT_JSON);
        String jsonContent = jsonFromDB;
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/json");
            InputStream jsonStream = new ByteArrayInputStream(jsonContent.getBytes());
            s3Client.putObject(bucketName, stringObjKeyName, jsonStream, metadata);
        } catch (Exception exception) {
            LOGGER.info("Exception occurred while uploading file to S3 bucket" + exception);
            throw exception;
        }
    }

    /**
     * get the object from the S3 bucket
     *
     */
    private JSONObject getBucket() {

        Regions clientRegion = Regions.EU_WEST_2;
        String bucketName = getParamFromList(parameters, Constants.S3BUCKET_NAME);
        String stringObjKeyName = getParamFromList((List)DynamoDBEventHandler.parameters, Constants.OUTPUT_JSON);
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();
            S3Object s3Object = s3Client.getObject(bucketName, stringObjKeyName);
            InputStream inputStream = s3Object.getObjectContent();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            return new JSONObject(responseStrBuilder.toString());
        } catch (IOException e) {
            LOGGER.info("Exception occurred while uploading file to S3 bucket" + e);
        } catch (JSONException e) {
            LOGGER.info("Exception occurred while uploading file to S3 bucket" + e);
        } catch (Exception exception) {
            LOGGER.info("Exception occurred while uploading file to S3 bucket" + exception);
            throw exception;
        }
        return null;
    }

}
