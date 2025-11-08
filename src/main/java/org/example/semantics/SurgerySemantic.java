package org.example.semantics;

import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.*;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import org.example.domain.model.fhir.SurgeryStatus;
import org.example.domain.model.fhir.CodeableConcept;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.dt.property.SurgeryProperties.*;
import static org.example.physicalAdapter.MqttSurgeryPhysicalAdapter.STATUS_KEY;
import static org.example.semantics.CodeSystems.SNOMED_CT;
import static org.example.semantics.CodeSystems.SNOMED_URI_PREFIX;
import static org.example.utils.GlobalValues.EXECUTED_IN_RELATIONSHIP_NAME;
import static org.example.utils.GlobalValues.PATIENT_OPERATED_RELATIONSHIP_NAME;

/*

[
    {
        "key": "reason",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "suture_timestamp",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "code",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "execution_end_date",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "warnings",
        "value": [],
        "type": "java.util.ArrayList",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "admissionTime",
        "value": "2025-09-23T09:10",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "priority",
        "value": "C",
        "type": "org.example.domain.model.PriorityClass",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "execution_start_date",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "score",
        "value": 0,
        "type": "java.lang.Integer",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "last_event",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "incision_timestamp",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "creationTimestamp",
        "value": "2025-10-21T16:25:10.663274600",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "programmed_date",
        "value": "2025-09-23T09:30",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "category",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "is_cancelled",
        "value": false,
        "type": "java.lang.Boolean",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "status",
        "value": "",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    },
    {
        "key": "estimated_time",
        "value": "60",
        "type": "java.lang.String",
        "readable": true,
        "writable": true,
        "exposed": true
    }
]

 */

public class SurgerySemantic extends AbstractSemantic implements DigitalTwinSemantics {
    private static final Map<String, RdfUriResource> PROPERTIES_DOMAIN_TAG = Map.of(
            REASON_PROPERTY_KEY, new RdfUriResource(URI.create("http://hl7.org/fhir/reason")),
            CATEGORY_PROPERTY_KEY, new RdfUriResource(URI.create("http://hl7.org/fhir/category")),
            STATUS_KEY, new RdfUriResource(URI.create("http://hl7.org/fhir/status")),
            CODE_PROPERTY_KEY, new RdfUriResource(URI.create("http://hl7.org/fhir/code"))
    );
    private static final Map<String, RdfUriResource> RELATIONSHIPS_DOMAIN_TAG = Map.of(
            PATIENT_OPERATED_RELATIONSHIP_NAME, new RdfUriResource(URI.create("http://www.hl7.org/fhir/subject")),
            EXECUTED_IN_RELATIONSHIP_NAME, new RdfUriResource(URI.create("http://www.hl7.org/fhir/location"))
    );

    private static final Map<String, RdfUriResource> ACTIONS_DOMAIN_TAG = Map.of();
    @Override
    public List<RdfClass> getDigitalTwinTypes() {
        return List.of(new RdfClass(URI.create("http://www.hl7.org/fhir/Procedure")));
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        return getOptionalFromMap(PROPERTIES_DOMAIN_TAG, digitalTwinStateProperty.getKey());
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {
        return getOptionalFromMap(RELATIONSHIPS_DOMAIN_TAG, digitalTwinStateRelationship.getName());
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(DigitalTwinStateAction digitalTwinStateAction) {
        return getOptionalFromMap(ACTIONS_DOMAIN_TAG, digitalTwinStateAction.getKey());
    }

    @Override
    public Optional<List<RdfUnSubjectedTriple>> mapData(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        switch (digitalTwinStateProperty.getKey()) {
            case REASON_PROPERTY_KEY:
                return Optional.of(List.of(
                                new RdfUnSubjectedTriple(
                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/reason")),
                                        new RdfBlankNode("procedure-reason-reason", List.of(
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/concept")),
                                                        new RdfBlankNode("procedure-reason-concept", List.of(
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/coding")),
                                                                        new RdfBlankNode("procedure-reason-coding", List.of(
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")),
                                                                                        new RdfUriResource(URI.create(SNOMED_URI_PREFIX + ((CodeableConcept) digitalTwinStateProperty.getValue()).getCode()))
                                                                                ),
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/system")),
                                                                                        new RdfBlankNode("procedure-reason-system", List.of(
                                                                                                new RdfUnSubjectedTriple(
                                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                                        new RdfUriResource(URI.create(SNOMED_CT))
                                                                                                )
                                                                                        ))
                                                                                ),
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/code")),
                                                                                        new RdfBlankNode("procedure-reason-coding-value", List.of(
                                                                                                new RdfUnSubjectedTriple(
                                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                                        new RdfLiteral<>(((CodeableConcept) digitalTwinStateProperty.getValue()).getCode())
                                                                                                )
                                                                                        ))
                                                                                )
                                                                        )
                                                                        )
                                                                ),
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/text")),
                                                                        new RdfBlankNode("procedure-reason-string-description", List.of(
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                        new RdfLiteral<>(((CodeableConcept) digitalTwinStateProperty.getValue()).getDescription())
                                                                                )
                                                                        ))
                                                                )
                                                        )
                                                        )
                                                )
                                        )
                                        )
                                )
                        )
                );

            case CATEGORY_PROPERTY_KEY:
                return Optional.of(List.of(
                                new RdfUnSubjectedTriple(
                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/category")),
                                        new RdfBlankNode("procedure-category", List.of(
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/coding")),
                                                        new RdfBlankNode("procedure-category-coding", List.of(
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")),
                                                                        new RdfUriResource(URI.create(SNOMED_URI_PREFIX + ((CodeableConcept) digitalTwinStateProperty.getValue()).getCode()))
                                                                ),
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/system")),
                                                                        new RdfBlankNode("procedure-category-system", List.of(
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                        new RdfUriResource(URI.create(SNOMED_CT))
                                                                                )
                                                                        ))
                                                                ),
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/code")),
                                                                        new RdfBlankNode("procedure-category-coding-value", List.of(
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                        new RdfLiteral<>(((CodeableConcept) digitalTwinStateProperty.getValue()).getCode())
                                                                                )
                                                                        ))
                                                                )
                                                        )
                                                        )
                                                ),
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/text")),
                                                        new RdfBlankNode("procedure-category-string-description", List.of(
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                        new RdfLiteral<>(((CodeableConcept) digitalTwinStateProperty.getValue()).getDescription())
                                                                )
                                                        ))
                                                )
                                        ))
                                )
                        )
                );

            case STATUS_KEY:
                return Optional.of(List.of(
                                new RdfUnSubjectedTriple(
                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/status")),
                                        new RdfBlankNode("procedure-status-string", List.of(
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                        new RdfLiteral<>(((SurgeryStatus)digitalTwinStateProperty.getValue()).code)
                                                )
                                        ))
                                )
                        )
                );
            case CODE_PROPERTY_KEY:
                return Optional.of(List.of(
                                new RdfUnSubjectedTriple(
                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/code")),
                                        new RdfBlankNode("", List.of(
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/coding")),
                                                        new RdfBlankNode("procedure-code-coding", List.of(
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")),
                                                                        new RdfUriResource(URI.create(SNOMED_URI_PREFIX + ((CodeableConcept) digitalTwinStateProperty.getValue()).getCode()))
                                                                ),
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/system")),
                                                                        new RdfBlankNode("procedure-code-system", List.of(
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                        new RdfUriResource(URI.create(SNOMED_CT))
                                                                                )
                                                                        ))
                                                                ),
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/code")),
                                                                        new RdfBlankNode("procedure-code-coding-value", List.of(
                                                                                new RdfUnSubjectedTriple(
                                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                        new RdfLiteral<>(((CodeableConcept) digitalTwinStateProperty.getValue()).getCode())
                                                                                )
                                                                        ))
                                                                )
                                                        )
                                                        )
                                                ),
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/text")),
                                                        new RdfBlankNode("procedure-category-string-description", List.of(
                                                                new RdfUnSubjectedTriple(
                                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                        new RdfLiteral<>(((CodeableConcept) digitalTwinStateProperty.getValue()).getDescription())
                                                                )
                                                        ))
                                                )
                                        ))
                                )
                        )
                );

            default:
                return Optional.empty();
        }
    }

    @Override
    protected Map<String, RdfUriResource> getRelationshipDomainTag() {
        return RELATIONSHIPS_DOMAIN_TAG;
    }
}
