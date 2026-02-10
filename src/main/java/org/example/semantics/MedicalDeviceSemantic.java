package org.example.semantics;

import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.*;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import org.example.domain.model.fhir.DeviceStatus;
import org.example.domain.model.fhir.SurgeryStatus;
import org.example.domain.model.fhir.CodeableConcept;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.physicalAdapter.MqttVSMPhysicalAdapter.*;
import static org.example.semantics.CodeSystems.SNOMED_CT;
import static org.example.semantics.CodeSystems.SNOMED_URI_PREFIX;

public class MedicalDeviceSemantic extends AbstractSemantic implements DigitalTwinSemantics {
    public static final String HEART_RATE_SCTID = "86184003";
    private static final Map<String, RdfUriResource> PROPERTIES_DOMAIN_TAG = Map.of(
            HEART_RATE, new RdfUriResource(URI.create("http://hl7.org/fhir/type")),
            SERIAL_CODE, new RdfUriResource(URI.create("http://hl7.org/fhir/serialNumber")),
            DEVICE_STATUS, new RdfUriResource(URI.create("http://hl7.org/fhir/status"))
    );
    private static final Map<String, RdfUriResource> RELATIONSHIPS_DOMAIN_TAG = Map.of(

    );
    private static final Map<String, RdfUriResource> ACTIONS_DOMAIN_TAG = Map.of();
    @Override
    public List<RdfClass> getDigitalTwinTypes() {
        return List.of(new RdfClass(URI.create("http://hl7.org/fhir/Device")));
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
        System.out.println("VSM SEMANTIC _ LOADING.....");
        switch (digitalTwinStateProperty.getKey()) {

            case HEART_RATE:
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://hl7.org/fhir/type")),
                                new RdfBlankNode("device-type-definition", List.of(
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/coding")),
                                                new RdfBlankNode("device-type-coding", List.of(
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")),
                                                                new RdfUriResource(URI.create(SNOMED_URI_PREFIX + HEART_RATE_SCTID))
                                                        ),
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/system")),
                                                                new RdfBlankNode("device-type-system", List.of(
                                                                        new RdfUnSubjectedTriple(
                                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                new RdfUriResource(URI.create(SNOMED_CT))
                                                                        )
                                                                ))
                                                        ),
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/code")),
                                                                new RdfBlankNode("device-type-coding-value", List.of(
                                                                        new RdfUnSubjectedTriple(
                                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                                new RdfLiteral<>(HEART_RATE_SCTID)
                                                                        )
                                                                ))
                                                        )
                                                )
                                                )
                                        )
                                ))
                        )
                ));

            case SERIAL_CODE:
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://www.hl7.org/fhir/text")),
                                new RdfBlankNode("device-serial-number-string-description", List.of(
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                new RdfLiteral<>((String)digitalTwinStateProperty.getValue())
                                        )
                                ))
                            )
                        )
                );

            case DEVICE_STATUS:
                return Optional.of(List.of(
                                new RdfUnSubjectedTriple(
                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/status")),
                                        new RdfBlankNode("device-status-string", List.of(
                                                new RdfUnSubjectedTriple(
                                                        new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                        new RdfLiteral<>(((DeviceStatus)digitalTwinStateProperty.getValue()).code)
                                                )
                                        ))
                                )
                        )
                );

            default:
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create(digitalTwinStateProperty.getKey())),
                                new RdfBlankNode("value", List.of(
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/value")),
                                                new RdfLiteral<>(digitalTwinStateProperty.getValue().toString())
                                        )
                                ))
                        )
                ));
        }
    }

    @Override
    protected Map<String, RdfUriResource> getRelationshipDomainTag() {
        return RELATIONSHIPS_DOMAIN_TAG;
    }
}
