package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class ExceptionRecordToCaseTransformer {

    public Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord) {
        Map<String, String> ocrToCCDMapping = getOcrToCCDMapping();

        Map<String, Object> ccdTransformedFields = exceptionRecord.getOcrDataFields().stream()
                .collect(Collectors.toMap(
                    ocrDataField -> ocrToCCDMapping.get(ocrDataField.getName()), OcrDataField::getValue
                ));

        ccdTransformedFields = runPostMappingTransformation(ccdTransformedFields);

        return ccdTransformedFields;
    }

    abstract Map<String, Object> runPostMappingTransformation(Map<String, Object> ccdTransformedFields);

    abstract Map<String, String> getOcrToCCDMapping();
}