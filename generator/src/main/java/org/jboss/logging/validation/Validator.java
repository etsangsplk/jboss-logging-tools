package org.jboss.logging.validation;

import org.jboss.logging.Annotations;
import org.jboss.logging.util.ElementHelper;
import org.jboss.logging.util.TransformationHelper;
import org.jboss.logging.validation.validator.BundleReturnTypeValidator;
import org.jboss.logging.validation.validator.LoggerReturnTypeValidator;
import org.jboss.logging.validation.validator.MessageAnnotationValidator;
import org.jboss.logging.validation.validator.MessageIdValidator;
import org.jboss.logging.validation.validator.MethodParameterValidator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class Validator {

    private final Annotations annotations;

    private final Types typeUtils;

    private final List<ElementValidator> validators;

    private Validator(final Types typeUtils, final Annotations annotations) {
        this.annotations = annotations;
        this.validators = new ArrayList<ElementValidator>();
        this.typeUtils = typeUtils;
    }

    public static Validator buildValidator(final ProcessingEnvironment pev, final Annotations annotations) {

        Validator validator = new Validator(pev.getTypeUtils(), annotations);

        //Add validators
        validator.addElementValidator(new BundleReturnTypeValidator());
        validator.addElementValidator(new LoggerReturnTypeValidator());
        validator.addElementValidator(new MessageAnnotationValidator());
        validator.addElementValidator(new MethodParameterValidator());
        validator.addElementValidator(new MessageIdValidator());

        return validator;
    }

    /**
     * Validates the collection of elements and returns validation messages.
     *
     * @param typeElements the elements to validate.
     *
     * @return the collection of validator messages.
     */
    public Collection<ValidationErrorMessage> validate(final Collection<? extends TypeElement> typeElements) {

        Collection<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>();

        for (TypeElement element : typeElements) {
            try {

                Collection<ExecutableElement> elementMethods = ElementHelper.getInterfaceMethods(element, typeUtils, null);

                for (ElementValidator validator : validators) {
                    errorMessages.addAll(validator.validate(element, elementMethods, annotations));
                }
            } catch (Exception e) {
                errorMessages.add(new ValidationErrorMessage(element, TransformationHelper.stackTraceToString(e)));
            }
        }

        return errorMessages;
    }

    /**
     * Adds an element validator to validate the elements with.
     *
     * @param elementValidator the element validator.
     */
    public void addElementValidator(final ElementValidator elementValidator) {
        this.validators.add(elementValidator);
    }
}
