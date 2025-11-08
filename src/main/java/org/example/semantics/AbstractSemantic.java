package org.example.semantics;

import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.RdfIndividual;
import io.github.webbasedwodt.model.ontology.rdf.RdfProperty;
import io.github.webbasedwodt.model.ontology.rdf.RdfUnSubjectedTriple;
import io.github.webbasedwodt.model.ontology.rdf.RdfUriResource;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractSemantic implements DigitalTwinSemantics {
    protected <T> Optional<T> getOptionalFromMap(final Map<String, T> map, final String key) {
        if (map.containsKey(key)) {
            return Optional.of(map.get(key));
        }
        return Optional.empty();
    }
    @Override
    public Optional<List<RdfUnSubjectedTriple>> mapData(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {
        return getOptionalFromMap(getRelationshipDomainTag(), digitalTwinStateRelationshipInstance.getRelationshipName()).map(uri ->
                List.of(
                        new RdfUnSubjectedTriple(
                                new RdfProperty(uri.getUri().get()),
                                new RdfIndividual(URI.create(digitalTwinStateRelationshipInstance.getTargetId().toString()))
                        )
                )
        );
    }

    protected abstract Map<String, RdfUriResource> getRelationshipDomainTag();
}
