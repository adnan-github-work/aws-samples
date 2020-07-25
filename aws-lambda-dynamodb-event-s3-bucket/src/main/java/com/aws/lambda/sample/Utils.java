package com.aws.lambda.sample;

import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

public class Utils {

    public static String getParamFromList(List<Parameter> parameterList, String paramName){
        Optional<String> filteredParam = parameterList.stream()
                        .filter(parameter -> paramName.equals(parameter.getName()))
                        .findFirst()
                        .map(Parameter::getValue);

        return filteredParam.orElse(null);
    }


    public static String getFormattedCurrentDate(String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        TimeZone london = TimeZone.getTimeZone("Europe/London");
        long now = System.currentTimeMillis();
        return sdf.format(new Date(now + london.getOffset(now)));
    }
}