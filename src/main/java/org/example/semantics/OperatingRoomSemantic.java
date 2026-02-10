package org.example.semantics;

import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.*;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import org.example.domain.model.fhir.CodeableConcept;
import org.example.domain.model.fhir.LocationType;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.dt.property.OperatingRoomProperties.OPERATING_ROOM_NAME;
import static org.example.physicalAdapter.MqttOperatingRoomPhysicalAdapter.LOCATION_TYPE;
import static org.example.semantics.CodeSystems.SNOMED_CT;
import static org.example.semantics.CodeSystems.SNOMED_URI_PREFIX;
import static org.example.utils.GlobalValues.BELONGS_TO_NAME;

public class OperatingRoomSemantic extends AbstractSemantic implements DigitalTwinSemantics {
    public static final String OPERATING_ROOM_SCTID = "225738002";
    private static final Map<String, RdfUriResource> PROPERTIES_DOMAIN_TAG = Map.of(
            OPERATING_ROOM_NAME, new RdfUriResource(URI.create("http://www.hl7.org/fhir/name")),
            LOCATION_TYPE, new RdfUriResource(URI.create("http://www.hl7.org/fhir/type"))
    );
    private static final Map<String, RdfUriResource> RELATIONSHIPS_DOMAIN_TAG = Map.of(
            BELONGS_TO_NAME, new RdfUriResource(URI.create("http://www.hl7.org/fhir/partOf"))
    );
    private static final Map<String, RdfUriResource> ACTIONS_DOMAIN_TAG = Map.of();

    @Override
    public List<RdfClass> getDigitalTwinTypes() {
        return List.of(new RdfClass(URI.create("http://www.hl7.org/fhir/Location")));
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
        System.out.println("OR SEMANTIC _ LOADING.....");
        switch (digitalTwinStateProperty.getKey()) {
            case OPERATING_ROOM_NAME:
                return Optional.of(List.of(
                                new RdfUnSubjectedTriple(
                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/name")),
                                        new RdfBlankNode("operating-room-name-string", List.of(
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                        new RdfLiteral<>(digitalTwinStateProperty.getValue())
                                                )
                                        ))
                                )
                        )
                );
            case LOCATION_TYPE:
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://hl7.org/fhir/type")),
                                new RdfBlankNode("location-type-definition", List.of(
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/coding")),
                                                new RdfBlankNode("location-type-coding", List.of(
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")),
                                                                new RdfUriResource(URI.create(SNOMED_URI_PREFIX + OPERATING_ROOM_SCTID))
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
                                                                                new RdfLiteral<>(OPERATING_ROOM_SCTID)
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
                return Optional.of(List.of(

                ));
        }
    }

    @Override
    protected Map<String, RdfUriResource> getRelationshipDomainTag() {
        return RELATIONSHIPS_DOMAIN_TAG;
    }
}
