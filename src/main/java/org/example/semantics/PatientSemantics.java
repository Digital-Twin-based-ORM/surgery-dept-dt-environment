package org.example.semantics;

import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.RdfClass;
import io.github.webbasedwodt.model.ontology.rdf.RdfUnSubjectedTriple;
import io.github.webbasedwodt.model.ontology.rdf.RdfUriResource;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class PatientSemantics implements DigitalTwinSemantics {

    /* TODO how to manage multiple rdf resources, like Patient and Observation
     *  or Practitioner not directly mapped to a DT, but withing the same patient DT
     */
    RdfClass type = new RdfClass(URI.create("https://www.hl7.org/fhir/patient.html"));

    public void setElement(RdfClass element) {

    }

    @Override
    public List<RdfClass> getDigitalTwinTypes() {
        return null;
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        return Optional.empty();
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {
        return Optional.empty();
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(DigitalTwinStateAction digitalTwinStateAction) {
        return Optional.empty();
    }

    @Override
    public Optional<List<RdfUnSubjectedTriple>> mapData(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        return Optional.empty();
    }

    @Override
    public Optional<List<RdfUnSubjectedTriple>> mapData(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {
        return Optional.empty();
    }
}
