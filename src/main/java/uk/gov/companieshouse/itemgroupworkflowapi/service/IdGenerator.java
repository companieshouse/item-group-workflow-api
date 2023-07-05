package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Calendar;

@Component
public class IdGenerator {

    private static final String ITEM_GROUP_CREATE_ID_PREFIX = "IG-";

    public String generateId() {
        var random = new SecureRandom();
        var values = new byte[4];
        random.nextBytes(values);
        var rand = String.format("%04d", random.nextInt(9999));
        var time = String.format("%08d", Calendar.getInstance().getTimeInMillis() / 100000L);
        var rawId = rand + time;
        var tranId = rawId.split("(?<=\\G.{6})");
        return ITEM_GROUP_CREATE_ID_PREFIX + String.join("-", tranId);
    }
}
