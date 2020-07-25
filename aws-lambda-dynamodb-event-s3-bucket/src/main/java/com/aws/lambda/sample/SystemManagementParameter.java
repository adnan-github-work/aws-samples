package com.aws.lambda.sample;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

import java.util.List;

public class SystemManagementParameter {

    protected List<Parameter> getSystemManagementParameter() {
        GetParametersRequest getParametersRequest = new GetParametersRequest().withNames(Constants.DYNAMO_DB_TABLE, Constants.S3BUCKET_NAME, Constants.OUTPUT_JSON).withWithDecryption(true);
        AWSSimpleSystemsManagement ssmclient = AWSSimpleSystemsManagementClientBuilder.defaultClient();
        GetParametersResult parameterResult = ssmclient.getParameters(getParametersRequest);
        return parameterResult.getParameters();
    }
}