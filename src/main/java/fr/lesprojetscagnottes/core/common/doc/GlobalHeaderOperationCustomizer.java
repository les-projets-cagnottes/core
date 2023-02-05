package fr.lesprojetscagnottes.core.common.doc;

import fr.lesprojetscagnottes.core.common.strings.AuthenticationConfigConstants;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

public class GlobalHeaderOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Parameter customHeaderVersion = new Parameter().in(ParameterIn.HEADER.toString()).name(AuthenticationConfigConstants.HEADER_STRING)
                .description("Bearer Token").schema(new StringSchema()).example("Bearer <>").required(true);
        operation.addParametersItem(customHeaderVersion);
        return operation;
    }

}