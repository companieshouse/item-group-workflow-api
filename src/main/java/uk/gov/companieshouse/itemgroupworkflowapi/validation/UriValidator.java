package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;

public class UriValidator implements ConstraintValidator<Uri, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        var isValid = true;
        try {
            new URI(value.toString());
        } catch (URISyntaxException e) {
            isValid = false;
        }
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(value + " is not a valid URI.")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
