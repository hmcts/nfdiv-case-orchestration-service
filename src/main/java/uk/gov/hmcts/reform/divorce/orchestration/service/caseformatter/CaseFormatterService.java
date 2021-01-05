package uk.gov.hmcts.reform.divorce.orchestration.service.caseformatter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.mapper.DivorceCaseToAosCaseMapper;
import uk.gov.hmcts.reform.divorce.mapper.DivorceCaseToDaCaseMapper;
import uk.gov.hmcts.reform.divorce.mapper.DivorceCaseToDnCaseMapper;
import uk.gov.hmcts.reform.divorce.mapper.DivorceCaseToDnClarificationMapper;
import uk.gov.hmcts.reform.divorce.model.DivorceCaseWrapper;
import uk.gov.hmcts.reform.divorce.model.ccd.AosCaseData;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.DaCaseData;
import uk.gov.hmcts.reform.divorce.model.ccd.DnCaseData;
import uk.gov.hmcts.reform.divorce.model.ccd.DnRefusalCaseData;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.service.DataMapTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.divorce.model.DocumentType.PETITION;

@Service
@RequiredArgsConstructor
public class CaseFormatterService {

    private static final String D8_DOCUMENTS_GENERATED_CCD_FIELD = "D8DocumentsGenerated";

    private final ObjectMapper objectMapper;
    private final DataMapTransformer dataMapTransformer;
    private final DivorceCaseToAosCaseMapper divorceCaseToAosCaseMapper;
    private final DivorceCaseToDnCaseMapper divorceCaseToDnCaseMapper;
    private final DivorceCaseToDnClarificationMapper divorceCaseToDnClarificationMapper;
    private final DivorceCaseToDaCaseMapper divorceCaseToDaCaseMapper;

    public Map<String, Object> transformToCCDFormat(Map<String, Object> divorceSessionMap) {
        return dataMapTransformer.transformDivorceCaseDataToCourtCaseData(divorceSessionMap);
    }

    public Map<String, Object> transformToDivorceSession(final Map<String, Object> coreCaseDataMap) {
        return dataMapTransformer.transformCoreCaseDataToDivorceCaseData(coreCaseDataMap);
    }

    public Map<String, Object> removeAllPetitionDocuments(final Map<String, Object> coreCaseData) {
        return removeAllDocumentsByType(coreCaseData, PETITION);
    }

    public Map<String, Object> removeAllDocumentsByType(final Map<String, Object> coreCaseData, final String documentType) {

        if (coreCaseData == null) {
            throw new IllegalArgumentException("Existing case data must not be null.");
        }

        final Map<String, Object> coreCaseDataCopy = new HashMap<>(coreCaseData);

        final List<CollectionMember<Document>> allDocuments =
            objectMapper.convertValue(coreCaseData.get(D8_DOCUMENTS_GENERATED_CCD_FIELD),
                new TypeReference<>() {
                });

        if (isNotEmpty(allDocuments)) {
            allDocuments.removeIf(documents -> isDocumentType(documents, documentType));
            coreCaseDataCopy.replace(D8_DOCUMENTS_GENERATED_CCD_FIELD, allDocuments);
        }

        return coreCaseDataCopy;
    }

    public Map<String, Object> getAosCaseData(final Map<String, Object> divorceSessionMap) {
        final DivorceSession divorceSession = objectMapper.convertValue(divorceSessionMap, DivorceSession.class);
        final AosCaseData aosCaseData = divorceCaseToAosCaseMapper.divorceCaseDataToAosCaseData(divorceSession);
        return objectMapper.convertValue(aosCaseData, new TypeReference<>() {
        });
    }

    public Map<String, Object> getDnCaseData(final Map<String, Object> divorceSessionMap) {
        final DivorceSession divorceSession = objectMapper.convertValue(divorceSessionMap, DivorceSession.class);
        final DnCaseData dnCaseData = divorceCaseToDnCaseMapper.divorceCaseDataToDnCaseData(divorceSession);
        return objectMapper.convertValue(dnCaseData, new TypeReference<>() {
        });
    }

    public Map<String, Object> getDnClarificationCaseData(final Map<String, Object> divorceCaseWrapperMap) {
        final DivorceCaseWrapper divorceCaseWrapper = objectMapper.convertValue(divorceCaseWrapperMap, DivorceCaseWrapper.class);
        final DnRefusalCaseData dnRefusalCaseData = divorceCaseToDnClarificationMapper.divorceCaseDataToDnCaseData(divorceCaseWrapper);
        return objectMapper.convertValue(dnRefusalCaseData, new TypeReference<>() {
        });
    }

    public DaCaseData getDaCaseData(DivorceSession divorceSession) {
        return divorceCaseToDaCaseMapper.divorceCaseDataToDaCaseData(divorceSession);
    }

    private boolean isDocumentType(CollectionMember<Document> document, String documentType) {
        return document.getValue().getDocumentType().equalsIgnoreCase(documentType);
    }
}
