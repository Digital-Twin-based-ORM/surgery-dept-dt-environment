package org.example.semantics;

import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.*;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import org.example.domain.model.fhir.CodeableConcept;
import org.example.domain.model.fhir.LocationType;
import org.example.physicalAdapter.MqttOperatingRoomPhysicalAdapter;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.DEPARTMENT_NAME;
import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.LOCATION_TYPE;
import static org.example.semantics.CodeSystems.SNOMED_CT;
import static org.example.semantics.CodeSystems.SNOMED_URI_PREFIX;
import static org.example.utils.GlobalValues.OPERATING_ROOMS_NAME;
import static org.example.utils.GlobalValues.SUPERVISE_SURGERY_NAME;

public class SurgeryDepSemantic extends AbstractSemantic implements DigitalTwinSemantics {
    public static final String SURGICAL_DEPARTMENT_SCTID = "309967005";
    private static final Map<String, RdfUriResource> PROPERTIES_DOMAIN_TAG = Map.of(
            DEPARTMENT_NAME, new RdfUriResource(URI.create("http://hl7.org/fhir/name")),
            LOCATION_TYPE, new RdfUriResource(URI.create("http://hl7.org/fhir/type"))
    );
    private static final Map<String, RdfUriResource> RELATIONSHIPS_DOMAIN_TAG = Map.of(
            SUPERVISE_SURGERY_NAME,new RdfUriResource(URI.create("http://hl7.org/fhir/v")),
            OPERATING_ROOMS_NAME, new RdfUriResource(URI.create("http://hl7.org/fhir/v"))
    );
    private static final Map<String, RdfUriResource> ACTIONS_DOMAIN_TAG = Map.of();

    @Override
    protected Map<String, RdfUriResource> getRelationshipDomainTag() {
        return RELATIONSHIPS_DOMAIN_TAG;
    }

    @Override
    public List<RdfClass> getDigitalTwinTypes() {
        return List.of(
                new RdfClass(URI.create("http://hl7.org/fhir/Location"))
            );
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
        System.out.println("DEP SEMANTIC _ LOADING.....");
        switch (digitalTwinStateProperty.getKey()) {
            case DEPARTMENT_NAME:
                return Optional.of(List.of(
                                new RdfUnSubjectedTriple(
                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/name")),
                                        new RdfBlankNode("department-name-string", List.of(
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                        new RdfLiteral<>(digitalTwinStateProperty.getValue())
                                                )
                                        ))
                                )
                        )
                );
            case MqttOperatingRoomPhysicalAdapter.LOCATION_TYPE:
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://hl7.org/fhir/type")),
                                new RdfBlankNode("location-type-definition", List.of(
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/coding")),
                                                new RdfBlankNode("location-type-coding", List.of(
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")),
                                                                new RdfUriResource(URI.create(SNOMED_URI_PREFIX + SURGICAL_DEPARTMENT_SCTID))
                                                        ),
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/system")),
                                                                new RdfBlankNode("location-type-system", List.of(
                                                                        new RdfUnSubjectedTriple(
                                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                new RdfUriResource(URI.create(SNOMED_CT))
                                                                        )
                                                                ))
                                                        ),
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/code")),
                                                                new RdfBlankNode("location-type-coding-value", List.of(
                                                                        new RdfUnSubjectedTriple(
                                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                new RdfLiteral<>(SURGICAL_DEPARTMENT_SCTID)
                                                                        )
                                                                ))
                                                        )
                                                )
                                                )
                                        ),
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/coding")),
                                                new RdfBlankNode("location-type-coding-hl7", List.of(
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/code")),
                                                                new RdfBlankNode("location-type-coding-value-hl7", List.of(
                                                                        new RdfUnSubjectedTriple(
                                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                new RdfLiteral<>(((LocationType)digitalTwinStateProperty.getValue()).code)
                                                                        )
                                                                ))
                                                        )
                                                )
                                                )
                                        )
                                )
                                )
                        )
                ));

            default:
                return Optional.of(List.of());
        }
    }
}
