package uk.gov.companieshouse.itemgroupworkflowapi.util;

import uk.gov.companieshouse.itemgroupworkflowapi.model.TimestampedEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

/**
 * Test utility that verifies the timestamp fields on {@link TimestampedEntity} instances.
 */
public class TimestampedEntityVerifier {

    /**
     * the start of the test, prior to the invocation of the method under test
     */
    private LocalDateTime intervalStart;

    /**
     * the end of the test, after the invocation of the method under test
     */
    private LocalDateTime intervalEnd;

    /** Use to record the start of the test, rounded down to the closest millisecond, prior to the invocation of the method under test.
     * @return the recorded start timestamp
     */
    public LocalDateTime start() {
        intervalStart = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        return intervalStart;
    }

    /** Use to record the end of the test, rounded up to the closest millisecond, after the invocation of the method under test.
     * @return the recorded end timestamp
     */
    public LocalDateTime end() {
        intervalEnd = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).plusNanos(1000000);
        return intervalEnd;
    }

    /**
     * Verifies that the entity created at and updated at timestamps are within the expected interval
     * for the creation.
     * @param createdEntity the created entity
     */
    public void verifyCreationTimestampsWithinExecutionInterval(final TimestampedEntity createdEntity) {
        checkIntervalRecorded();

        assertThat(createdEntity.getCreatedAt().isAfter(intervalStart) ||
                createdEntity.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(createdEntity.getCreatedAt().isBefore(intervalEnd) ||
                createdEntity.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(createdEntity.getUpdatedAt().isAfter(intervalStart) ||
                createdEntity.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(createdEntity.getUpdatedAt().isBefore(intervalEnd) ||
                createdEntity.getUpdatedAt().isEqual(intervalEnd), is(true));
    }

    /**
     * Verifies that the updated entity updated at timestamp is within the expected interval
     * for the update.
     * @param updatedEntity the updated entity
     */
    public void verifyUpdatedAtTimestampWithinExecutionInterval(final TimestampedEntity updatedEntity) {
        checkIntervalRecorded();

        assertThat(updatedEntity.getUpdatedAt().isAfter(updatedEntity.getCreatedAt()) ||
                updatedEntity.getUpdatedAt().isEqual(updatedEntity.getCreatedAt()), is(true));

        assertThat(updatedEntity.getUpdatedAt().isAfter(intervalStart) ||
                updatedEntity.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(updatedEntity.getUpdatedAt().isBefore(intervalEnd) ||
                updatedEntity.getUpdatedAt().isEqual(intervalEnd), is(true));
    }

    /**
     * Checks that the interval start and end points have been recorded correctly.
     */
    private void checkIntervalRecorded() {
        assertThat("start() must already have been called at this point!", intervalStart, is(notNullValue()));
        assertThat("end() must already have been called at this point!", intervalEnd, is(notNullValue()));
    }

}
