package org.example.semantics;

import io.github.webbasedwodt.model.ontology.rdf.*;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import org.example.domain.model.PatientNominative;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.utils.GlobalValues.LOCATED_IN_RELATIONSHIP_NAME;
import static org.example.utils.GlobalValues.SURGERY_RELATIONSHIP_NAME;

public class PatientSemantic extends AbstractSemantic {

    private static final Map<String, RdfUriResource> PROPERTIES_DOMAIN_TAG = Map.of(
            "name", new RdfUriResource(URI.create("http://www.hl7.org/fhir/HumanName")),
            "gender", new RdfUriResource(URI.create("http://www.hl7.org/fhir/code")),
            "identifier", new RdfUriResource(URI.create("http://www.hl7.org/fhir/Identifier")),
            "birthDate", new RdfUriResource(URI.create("http://www.hl7.org/fhir/date"))
    );

    private static final Map<String, RdfUriResource> RELATIONSHIPS_DOMAIN_TAG = Map.of();

    private static final Map<String, RdfUriResource> ACTIONS_DOMAIN_TAG = Map.of(
            "addSurgery", new RdfUriResource(URI.create("https://purl.org/mao/onto/AddSurgery"))
    );

    @Override
    public List<RdfClass> getDigitalTwinTypes() {
        return List.of(new RdfClass(URI.create("https://www.hl7.org/fhir/Patient")));
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
            case "name":
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://www.hl7.org/fhir/name")),
                                new RdfBlankNode("patient-name", List.of(
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/given")),
                                                // maybe RdfBlankNode instead of directly the RdfLiteral to better describe the property name and surname?
                                                new RdfBlankNode("patient-identifier-name-value", List.of(
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                new RdfLiteral<>(((PatientNominative)digitalTwinStateProperty.getValue()).getName())
                                                        )
                                                ))
                                        ),
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/family")),
                                                new RdfBlankNode("patient-identifier-surname-value", List.of(
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                new RdfLiteral<>(((PatientNominative)digitalTwinStateProperty.getValue()).getSurname())
                                                        )
                                                ))
                                        )
                                ))
                        )
                ));
                ////////////////////////////////
            case "gender":
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://www.hl7.org/fhir/gender")),
                                new RdfLiteral<>(digitalTwinStateProperty.getValue().toString())
                        )
                ));
                ////////////////////////////////
            case "birthDate":
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://www.hl7.org/fhir/birthDate")),
                                new RdfLiteral<>(((LocalDate)digitalTwinStateProperty.getValue()).format(DateTimeFormatter.ISO_LOCAL_DATE))
                        )
                ));
                ////////////////////////////////
            case "identifier":
                return Optional.of(List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(URI.create("http://www.hl7.org/fhir/identifier")),
                                new RdfBlankNode("patient-identifier-value", List.of(
                                        new RdfUnSubjectedTriple(
                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/value")),
                                                new RdfBlankNode("identifier-value", List.of(
                                                        new RdfUnSubjectedTriple(
                                                                new RdfProperty(URI.create("http://www.hl7.org/fhir/v")),
                                                                new RdfLiteral<>(digitalTwinStateProperty.getValue().toString())
                                                        )
                                                ))
                                        )
                                ))
                        )
                ));
            default:
                return Optional.empty();
        }
    }

    @Override
    protected Map<String, RdfUriResource> getRelationshipDomainTag() {
        return RELATIONSHIPS_DOMAIN_TAG;
    }
}
