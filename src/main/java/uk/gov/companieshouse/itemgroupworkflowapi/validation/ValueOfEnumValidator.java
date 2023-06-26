package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
    private List<String> acceptedValues;

    @Override
    public void initialize(ValueOfEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::toString)
                .collect(toList());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        final boolean isValid = acceptedValues.contains(value.toString());
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("must be one of " + acceptedValues)
                    .addConstraintViolation();
        }
        return isValid;
    }
}
